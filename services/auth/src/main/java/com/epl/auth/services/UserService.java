package com.epl.auth.services;

import com.epl.auth.entities.AppUserEntity;
import com.epl.auth.entities.UserAttribute;
import com.epl.auth.entities.UserCredentialEntity;
import com.epl.auth.entities.UserTenantGrantEntity;
import com.epl.auth.mappers.UserMappers;
import com.epl.auth.repositories.AppUserRepository;
import com.epl.auth.repositories.CustomerRepository;
import com.epl.auth.repositories.RoleRepository;
import com.epl.auth.repositories.UserCredentialRepository;
import com.epl.auth.repositories.UserTenantGrantRepository;
import com.epl.auth.v1.ChangePasswordRequest;
import com.epl.auth.v1.CreateUserRequest;
import com.epl.auth.v1.ListUsersRequest;
import com.epl.auth.v1.ListUsersResponse;
import com.epl.auth.v1.TenantGrant;
import com.epl.auth.v1.User;
import com.epl.auth.v1.UserType;
import com.epl.messaging.Outbox;
import com.epl.service.core.Errors;
import com.epl.service.core.Pages;
import com.epl.service.core.RequestContext;
import io.grpc.Status;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import static com.epl.service.core.Protos.localDate;
import static com.epl.service.core.Protos.nullIfEmpty;
import static com.epl.service.core.Protos.uuid;

@Service
public class UserService {

    private static final int MIN_PASSWORD_LENGTH = 12;

    private final AppUserRepository userRepository;
    private final UserCredentialRepository credentialRepository;
    private final UserTenantGrantRepository grantRepository;
    private final RoleRepository roleRepository;
    private final CustomerRepository customerRepository;
    private final CompanyService companyService;
    private final WarehouseService warehouseService;
    private final CustomerService customerService;
    private final PasswordEncoder passwordEncoder;
    private final Outbox outbox;

    public UserService(AppUserRepository userRepository, UserCredentialRepository credentialRepository,
                       UserTenantGrantRepository grantRepository, RoleRepository roleRepository,
                       CustomerRepository customerRepository, CompanyService companyService,
                       WarehouseService warehouseService, CustomerService customerService,
                       PasswordEncoder passwordEncoder, Outbox outbox) {
        this.userRepository = userRepository;
        this.credentialRepository = credentialRepository;
        this.grantRepository = grantRepository;
        this.roleRepository = roleRepository;
        this.customerRepository = customerRepository;
        this.companyService = companyService;
        this.warehouseService = warehouseService;
        this.customerService = customerService;
        this.passwordEncoder = passwordEncoder;
        this.outbox = outbox;
    }

    @Transactional
    public User create(CreateUserRequest request) {
        var user = request.getUser();
        var username = Errors.required(user.getUsername(), "username");
        if (userRepository.existsByUsernameIgnoreCase(username)) {
            throw Status.ALREADY_EXISTS.withDescription("User " + username + " already exists").asRuntimeException();
        }
        var entity = new AppUserEntity();
        entity.setUsername(username);
        entity.setUserType(UserMappers.userType(user.getType()));
        apply(entity, user);
        entity = userRepository.saveAndFlush(entity);

        var credential = new UserCredentialEntity();
        credential.setUserId(entity.getId());
        credential.setPasswordHash(hash(request.getInitialPassword()));
        credential.setHashAlgorithm("BCRYPT");
        credential.setMustChange(true);
        credential.setUpdatedAt(Instant.now());
        credentialRepository.save(credential);
        return UserMappers.user(entity);
    }

    @Transactional(readOnly = true)
    public User get(String id) {
        return UserMappers.user(find(id));
    }

    @Transactional(readOnly = true)
    public ListUsersResponse list(ListUsersRequest request) {
        var query = request.getQuery().isBlank() ? "" : "%" + request.getQuery().trim().toLowerCase(Locale.ROOT) + "%";
        var type = request.getType() == UserType.USER_TYPE_UNSPECIFIED ? "" : UserMappers.userType(request.getType());
        var page = userRepository.search(query, type, Pages.pageable(request.getPage(), Sort.by("username")));
        return ListUsersResponse.newBuilder()
                .addAllUsers(page.map(UserMappers::user).getContent())
                .setPage(Pages.response(page))
                .build();
    }

    /** Username and type are fixed; disabling a user bumps token_version (every issued JWT becomes invalid). */
    @Transactional
    public User update(User request) {
        var entity = find(request.getId());
        Errors.checkVersion(entity.getVersion(), request.getAudit().getVersion());
        var previousStatus = entity.getStatus();
        apply(entity, request);
        if (!previousStatus.equals(entity.getStatus())) {
            entity.setTokenVersion(entity.getTokenVersion() + 1);
        }
        return UserMappers.user(userRepository.saveAndFlush(entity));
    }

    /** Credentials, grants, attributes and refresh tokens go with the user (ON DELETE CASCADE). */
    @Transactional
    public void delete(String id, int version) {
        var entity = find(id);
        Errors.checkVersion(entity.getVersion(), version);
        userRepository.delete(entity);
        userRepository.flush();
    }

    @Transactional
    public void changePassword(ChangePasswordRequest request) {
        var user = find(request.getUserId());
        var credential = credentialRepository.findById(user.getId())
                .orElseThrow(() -> Errors.failedPrecondition("User " + user.getUsername() + " has no password"));
        if (!passwordEncoder.matches(request.getCurrentPassword(), credential.getPasswordHash())) {
            throw Status.PERMISSION_DENIED.withDescription("Current password is wrong").asRuntimeException();
        }
        credential.setPasswordHash(hash(request.getNewPassword()));
        credential.setMustChange(false);
        credential.setUpdatedAt(Instant.now());
        user.setTokenVersion(user.getTokenVersion() + 1);
    }

    // grants ------------------------------------------------------------------

    @Transactional
    public TenantGrant grant(TenantGrant request) {
        var user = find(request.getUserId());
        var roleCode = Errors.required(request.getRoleCode(), "role_code");
        if (!roleRepository.existsById(roleCode)) {
            throw Errors.notFound("Role", roleCode);
        }
        var grant = new UserTenantGrantEntity();
        grant.setUserId(user.getId());
        grant.setCompany(companyService.find(request.getCompanyCode()));
        if (!request.getWarehouseCode().isBlank()) {
            grant.setWarehouse(warehouseService.find(request.getCompanyCode(), request.getWarehouseCode()));
        }
        if (!request.getCustomerCode().isBlank()) {
            grant.setCustomer(customerService.find(request.getCompanyCode(), request.getCustomerCode()));
        }
        grant.setRoleCode(roleCode);
        grant.setValidFrom(Optional.ofNullable(localDate(request.getValidFrom())).orElse(LocalDate.now()));
        grant.setValidTo(localDate(request.getValidTo()));
        grant = grantRepository.saveAndFlush(grant);
        grantsChanged(user);
        return UserMappers.grant(grant);
    }

    @Transactional
    public void revokeGrant(String grantId) {
        var grant = grantRepository.findById(uuid(grantId, "grant_id"))
                .orElseThrow(() -> Errors.notFound("Grant", grantId));
        grantRepository.delete(grant);
        userRepository.findById(grant.getUserId()).ifPresent(this::grantsChanged);
    }

    /** Tokens issued before the change stop working; services caching the user's grants drop them. */
    private void grantsChanged(AppUserEntity user) {
        user.setTokenVersion(user.getTokenVersion() + 1);
        outbox.append("user.grants.changed", "user", user.getId().toString(),
                Map.of("userId", user.getId().toString(), "tokenVersion", user.getTokenVersion()));
    }

    @Transactional(readOnly = true)
    public List<TenantGrant> listGrants(String userId) {
        return grantRepository.findAllByUserIdOrderByValidFrom(uuid(userId, "user_id"))
                .stream().map(UserMappers::grant).toList();
    }

    // -------------------------------------------------------------------------

    private AppUserEntity find(String id) {
        return userRepository.findById(uuid(id, "id")).orElseThrow(() -> Errors.notFound("User", id));
    }

    private void apply(AppUserEntity entity, User request) {
        entity.setEmail(nullIfEmpty(request.getEmail()));
        entity.setDisplayName(Errors.required(request.getDisplayName(), "display_name"));
        entity.setStatus(UserMappers.userStatus(request.getStatus()));
        entity.setHomeCompany(request.getHomeCompanyCode().isBlank() ? null : companyService.find(request.getHomeCompanyCode()));
        entity.setHomeCustomer(request.getHomeCustomerCode().isBlank() ? null
                : customerRepository.findByCompanyCodeAndCode(request.getHomeCompanyCode(), request.getHomeCustomerCode())
                .orElseThrow(() -> Errors.notFound("Customer", request.getHomeCompanyCode() + "/" + request.getHomeCustomerCode())));
        if ("CUSTOMER".equals(entity.getUserType()) && entity.getHomeCustomer() == null) {
            throw Errors.invalid("home_customer_code is required for CUSTOMER users");
        }
        entity.setDepartmentCode(nullIfEmpty(request.getDepartmentCode()));
        entity.setLocale(request.getLocale().isBlank() ? "en" : request.getLocale());
        entity.setTimeZone(nullIfEmpty(request.getTimeZone()));

        var by = Optional.ofNullable(RequestContext.current().user()).orElse("system");
        entity.getAttributes().keySet().retainAll(request.getAttributesMap().keySet());
        request.getAttributesMap().forEach((key, value) -> {
            var attribute = entity.getAttributes().get(key);
            if (attribute == null) {
                entity.getAttributes().put(key, new UserAttribute(value, by));
            } else if (!value.equals(attribute.getText())) {
                attribute.setText(value);
                attribute.setUpdatedAt(Instant.now());
                attribute.setUpdatedBy(by);
            }
        });
    }

    private String hash(String password) {
        if (password == null || password.length() < MIN_PASSWORD_LENGTH) {
            throw Errors.invalid("Password must have at least " + MIN_PASSWORD_LENGTH + " characters");
        }
        return passwordEncoder.encode(password);
    }
}

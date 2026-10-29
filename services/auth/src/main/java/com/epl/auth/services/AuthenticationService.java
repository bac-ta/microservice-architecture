package com.epl.auth.services;

import com.epl.auth.configurations.AuthProperties;
import com.epl.auth.entities.AppUserEntity;
import com.epl.auth.entities.LoginAuditEntity;
import com.epl.auth.entities.RefreshTokenEntity;
import com.epl.auth.entities.UserCredentialEntity;
import com.epl.auth.entities.UserTenantGrantEntity;
import com.epl.auth.repositories.AppUserRepository;
import com.epl.auth.repositories.LoginAuditRepository;
import com.epl.auth.repositories.RefreshTokenRepository;
import com.epl.auth.repositories.UserCredentialRepository;
import com.epl.auth.repositories.UserTenantGrantRepository;
import com.epl.auth.v1.LoginRequest;
import com.epl.auth.v1.LogoutRequest;
import com.epl.auth.v1.RefreshRequest;
import com.epl.auth.v1.TokenResponse;
import com.epl.service.core.Errors;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jwt.JWTClaimsSet;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeSet;
import java.util.UUID;

/**
 * AuthService: password login, refresh token rotation and logout (database/README.md, "Authentication: JWT").
 * <p>
 * Failures are answered with UNAUTHENTICATED without telling whether the username exists. They are
 * thrown inside the transaction on purpose: noRollbackFor keeps the failed-login counter, the
 * lockout and the audit rows.
 */
@RequiredArgsConstructor
@Service
public class AuthenticationService {

    private static final ObjectMapper JSON = new ObjectMapper();
    private static final String INVALID_CREDENTIALS = "Invalid username or password";
    private static final String INVALID_REFRESH_TOKEN = "The session has expired; sign in again";
    private static final int REFRESH_TOKEN_BYTES = 32;

    private final AppUserRepository userRepository;
    private final UserCredentialRepository credentialRepository;
    private final UserTenantGrantRepository grantRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final LoginAuditRepository auditRepository;
    private final SigningKeyService signingKeyService;
    private final PasswordEncoder passwordEncoder;
    private final JdbcClient jdbc;
    private final AuthProperties authProperties;
    private final SecureRandom random = new SecureRandom();
    /** Compared against when the username is unknown, so the answer takes as long as a wrong password. */
    private String dummyHash;

    @PostConstruct
    void init() {
        dummyHash = passwordEncoder.encode(UUID.randomUUID().toString());
    }

    @Transactional(noRollbackFor = StatusRuntimeException.class)
    public TokenResponse login(LoginRequest request) {
        var username = Errors.required(request.getUsername(), "username");
        var client = client(request.getClientId(), "password");
        var now = Instant.now();

        var user = userRepository.findByUsernameIgnoreCase(username).orElse(null);
        var credential = user == null ? null : credentialRepository.findById(user.getId()).orElse(null);
        if (user == null || credential == null) {
            passwordEncoder.matches(request.getPassword(), dummyHash);
            audit(username, user, client.id(), "LOGIN_FAILED");
            throw unauthenticated(INVALID_CREDENTIALS);
        }
        if (user.getLockedUntil() != null && user.getLockedUntil().isAfter(now)) {
            audit(username, user, client.id(), "LOGIN_FAILED");
            throw unauthenticated("Too many failed sign-ins; try again later");
        }
        if (!passwordEncoder.matches(request.getPassword(), credential.getPasswordHash())) {
            var failed = user.getFailedLoginCount() + 1;
            if (failed >= token().maxFailedLogins()) {
                userRepository.recordFailedLogin(user.getId(), 0, now.plus(token().lockDuration()));
                audit(username, user, client.id(), "LOCKED");
            } else {
                userRepository.recordFailedLogin(user.getId(), failed, null);
                audit(username, user, client.id(), "LOGIN_FAILED");
            }
            throw unauthenticated(INVALID_CREDENTIALS);
        }
        checkActive(user, client.id());

        userRepository.recordLogin(user.getId(), now);
        audit(user.getUsername(), user, client.id(), "LOGIN_OK");
        var mustChange = credential.isMustChange() || (credential.getExpiresAt() != null && credential.getExpiresAt().isBefore(now));
        return tokens(user, client, UUID.randomUUID(), now, null, mustChange);
    }

    @Transactional(noRollbackFor = StatusRuntimeException.class)
    public TokenResponse refresh(RefreshRequest request) {
        var client = client(request.getClientId(), "refresh_token");
        var now = Instant.now();
        var token = refreshTokenRepository.findByTokenHash(hash(Errors.required(request.getRefreshToken(), "refresh_token")))
                .orElseThrow(() -> unauthenticated(INVALID_REFRESH_TOKEN));
        var user = userRepository.findById(token.getUserId()).orElseThrow(() -> unauthenticated(INVALID_REFRESH_TOKEN));

        if (token.getRevokedAt() != null) {
            // an old token came back: someone else may hold the family, end it
            refreshTokenRepository.revokeFamily(token.getFamilyId(), now);
            audit(user.getUsername(), user, client.id(), "REUSE_DETECTED");
            throw unauthenticated(INVALID_REFRESH_TOKEN);
        }
        if (token.getExpiresAt().isBefore(now) || !token.getClientId().equals(client.id())) {
            throw unauthenticated(INVALID_REFRESH_TOKEN);
        }
        if (user.getLockedUntil() != null && user.getLockedUntil().isAfter(now)) {
            throw unauthenticated(INVALID_REFRESH_TOKEN);
        }
        checkActive(user, client.id());

        var mustChange = credentialRepository.findById(user.getId()).map(UserCredentialEntity::isMustChange).orElse(false);
        token.setRevokedAt(now);
        audit(user.getUsername(), user, client.id(), "REFRESH");
        return tokens(user, client, token.getFamilyId(), now, token, mustChange);
    }

    /** Ends the session of the refresh token (its family), or every session of the user; an unknown token is ignored. */
    @Transactional
    public void logout(LogoutRequest request) {
        if (request.getRefreshToken().isBlank()) {
            return;
        }
        var now = Instant.now();
        refreshTokenRepository.findByTokenHash(hash(request.getRefreshToken())).ifPresent(token -> {
            if (request.getAllSessions()) {
                refreshTokenRepository.revokeAllOfUser(token.getUserId(), now);
                userRepository.bumpTokenVersion(token.getUserId());
            } else {
                refreshTokenRepository.revokeFamily(token.getFamilyId(), now);
            }
            var user = userRepository.findById(token.getUserId()).orElse(null);
            audit(user == null ? "" : user.getUsername(), user, token.getClientId(), "LOGOUT");
        });
    }

    // -------------------------------------------------------------------------

    private TokenResponse tokens(AppUserEntity user, Client client, UUID familyId, Instant now,
                                 RefreshTokenEntity replaced, boolean mustChangePassword) {
        var refreshToken = randomToken();
        var entity = new RefreshTokenEntity();
        entity.setUserId(user.getId());
        entity.setFamilyId(familyId);
        entity.setTokenHash(hash(refreshToken));
        entity.setClientId(client.id());
        entity.setIssuedAt(now);
        entity.setExpiresAt(now.plus(client.refreshTokenTtl()));
        entity = refreshTokenRepository.save(entity);
        if (replaced != null) {
            replaced.setReplacedBy(entity.getId());
        }

        return TokenResponse.newBuilder()
                .setAccessToken(signingKeyService.sign(claims(user, now, client.accessTokenTtl())))
                .setRefreshToken(refreshToken)
                .setExpiresInSeconds((int) client.accessTokenTtl().toSeconds())
                .setTokenType("Bearer")
                .setMustChangePassword(mustChangePassword)
                .build();
    }

    /** Access token claims as documented in database/README.md. */
    private JWTClaimsSet claims(AppUserEntity user, Instant now, Duration ttl) {
        var attrs = new LinkedHashMap<String, Object>();
        user.getAttributes().forEach((key, attribute) -> {
            if (attribute.isInToken()) {
                attrs.put(key, JSON.convertValue(attribute.getValue(), Object.class));
            }
        });
        return new JWTClaimsSet.Builder()
                .issuer(token().issuer())
                .subject(user.getId().toString())
                .audience(token().audience())
                .issueTime(Date.from(now))
                .expirationTime(Date.from(now.plus(ttl)))
                .jwtID(UUID.randomUUID().toString())
                .claim("usr", user.getUsername())
                .claim("name", user.getDisplayName())
                .claim("typ", user.getUserType())
                .claim("tv", user.getTokenVersion())
                .claim("attrs", attrs)
                .claim("grants", grants(user.getId()))
                .build();
    }

    /** Grants valid today, one entry per scope: {"c": company, "w": warehouse, "u": customer, "r": [roles]}; "*" = all. */
    private List<Map<String, Object>> grants(UUID userId) {
        var today = LocalDate.now();
        var byScope = new LinkedHashMap<List<String>, TreeSet<String>>();
        for (UserTenantGrantEntity grant : grantRepository.findAllByUserIdOrderByValidFrom(userId)) {
            if (grant.getValidFrom().isAfter(today) || (grant.getValidTo() != null && grant.getValidTo().isBefore(today))) {
                continue;
            }
            var scope = List.of(grant.getCompany().getCode(),
                    grant.getWarehouse() == null ? "*" : grant.getWarehouse().getCode(),
                    grant.getCustomer() == null ? "*" : grant.getCustomer().getCode());
            byScope.computeIfAbsent(scope, k -> new TreeSet<>()).add(grant.getRoleCode());
        }
        var grants = new ArrayList<Map<String, Object>>();
        byScope.forEach((scope, roles) -> grants.add(Map.of(
                "c", scope.get(0), "w", scope.get(1), "u", scope.get(2), "r", List.copyOf(roles))));
        return grants;
    }

    private void checkActive(AppUserEntity user, String clientId) {
        if (!"ACTIVE".equals(user.getStatus())) {
            audit(user.getUsername(), user, clientId, "LOGIN_FAILED");
            throw Status.PERMISSION_DENIED.withDescription("The account is " + user.getStatus().toLowerCase(Locale.ROOT))
                    .asRuntimeException();
        }
    }

    private AuthProperties.Token token() {
        return authProperties.token();
    }

    private record Client(String id, Duration accessTokenTtl, Duration refreshTokenTtl) {
    }

    /** iam.oauth_client allowed to use {@code grant} (password, refresh_token). */
    private Client client(String clientId, String grant) {
        var id = Errors.required(clientId, "client_id");
        return jdbc.sql("""
                        select client_id,
                               extract(epoch from access_token_ttl)::bigint  as access_ttl,
                               extract(epoch from refresh_token_ttl)::bigint as refresh_ttl
                        from oauth_client
                        where client_id = :clientId and enabled and :grant = any (allowed_grants)
                        """)
                .param("clientId", id)
                .param("grant", grant)
                .query((rs, row) -> new Client(rs.getString("client_id"),
                        Duration.ofSeconds(rs.getLong("access_ttl")), Duration.ofSeconds(rs.getLong("refresh_ttl"))))
                .optional()
                .orElseThrow(() -> unauthenticated("Unknown client " + id));
    }

    private void audit(String username, AppUserEntity user, String clientId, String event) {
        auditRepository.save(new LoginAuditEntity(username, user == null ? null : user.getId(), clientId, event));
    }

    private String randomToken() {
        var bytes = new byte[REFRESH_TOKEN_BYTES];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private static byte[] hash(String token) {
        try {
            return MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    private static StatusRuntimeException unauthenticated(String message) {
        return Status.UNAUTHENTICATED.withDescription(message).asRuntimeException();
    }
}

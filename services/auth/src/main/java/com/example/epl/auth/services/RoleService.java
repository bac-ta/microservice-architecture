package com.example.epl.auth.services;

import com.example.epl.auth.entities.RoleEntity;
import com.example.epl.auth.mappers.UserMappers;
import com.example.epl.auth.repositories.RoleRepository;
import com.example.epl.auth.repositories.UserTenantGrantRepository;
import com.example.epl.auth.v1.Role;
import com.example.service.core.Errors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.regex.Pattern;

import static com.example.service.core.Protos.nullIfEmpty;

@Service
public class RoleService {

    private static final Pattern CODE = Pattern.compile("^[A-Z][A-Z0-9_]{1,49}$");

    private final RoleRepository roleRepository;
    private final UserTenantGrantRepository grantRepository;

    public RoleService(RoleRepository roleRepository, UserTenantGrantRepository grantRepository) {
        this.roleRepository = roleRepository;
        this.grantRepository = grantRepository;
    }

    @Transactional(readOnly = true)
    public List<Role> list() {
        return roleRepository.findAllByOrderByCode().stream().map(UserMappers::role).toList();
    }

    @Transactional
    public Role upsert(Role request) {
        var code = Errors.required(request.getCode(), "code");
        if (!CODE.matcher(code).matches()) {
            throw Errors.invalid("code must be UPPER_SNAKE, e.g. WMS_OPERATOR");
        }
        var entity = roleRepository.findById(code).orElseGet(() -> {
            var role = new RoleEntity();
            role.setCode(code);
            return role;
        });
        entity.setName(Errors.required(request.getName(), "name"));
        entity.setDescription(nullIfEmpty(request.getDescription()));
        return UserMappers.role(roleRepository.saveAndFlush(entity));
    }

    @Transactional
    public void delete(String code) {
        var entity = roleRepository.findById(Errors.required(code, "code"))
                .orElseThrow(() -> Errors.notFound("Role", code));
        if (grantRepository.existsByRoleCode(code)) {
            throw Errors.failedPrecondition("Role " + code + " is still granted to users");
        }
        roleRepository.delete(entity);
    }
}

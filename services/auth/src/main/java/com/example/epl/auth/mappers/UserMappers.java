package com.example.epl.auth.mappers;

import com.example.epl.auth.entities.AppUserEntity;
import com.example.epl.auth.entities.RoleEntity;
import com.example.epl.auth.entities.UserTenantGrantEntity;
import com.example.epl.auth.v1.Role;
import com.example.epl.auth.v1.TenantGrant;
import com.example.epl.auth.v1.User;
import com.example.epl.auth.v1.UserStatus;
import com.example.epl.auth.v1.UserType;
import com.example.service.core.Errors;

import static com.example.service.core.Protos.audit;
import static com.example.service.core.Protos.date;
import static com.example.service.core.Protos.emptyIfNull;
import static com.example.service.core.Protos.text;
import static com.example.service.core.Protos.timestamp;

/** Users, grants and roles <-> proto messages. Enums are stored without their proto prefix (INTERNAL). */
public final class UserMappers {

    private UserMappers() {
    }

    public static User user(AppUserEntity entity) {
        var builder = User.newBuilder()
                .setId(text(entity.getId()))
                .setUsername(entity.getUsername())
                .setEmail(emptyIfNull(entity.getEmail()))
                .setDisplayName(entity.getDisplayName())
                .setType(UserType.valueOf("USER_TYPE_" + entity.getUserType()))
                .setStatus(UserStatus.valueOf("USER_STATUS_" + entity.getStatus()))
                .setHomeCompanyCode(entity.getHomeCompany() == null ? "" : entity.getHomeCompany().getCode())
                .setHomeCustomerCode(entity.getHomeCustomer() == null ? "" : entity.getHomeCustomer().getCode())
                .setDepartmentCode(emptyIfNull(entity.getDepartmentCode()))
                .setLocale(entity.getLocale())
                .setTimeZone(emptyIfNull(entity.getTimeZone()))
                .setLastLoginAt(timestamp(entity.getLastLoginAt()))
                .setAudit(audit(entity));
        entity.getAttributes().forEach((key, attribute) -> builder.putAttributes(key, attribute.getText()));
        return builder.build();
    }

    public static String userType(UserType type) {
        if (type == UserType.USER_TYPE_UNSPECIFIED || type == UserType.UNRECOGNIZED) {
            throw Errors.invalid("type is required");
        }
        return type.name().substring("USER_TYPE_".length());
    }

    /** UNSPECIFIED = ACTIVE */
    public static String userStatus(UserStatus status) {
        if (status == UserStatus.USER_STATUS_UNSPECIFIED || status == UserStatus.UNRECOGNIZED) {
            return "ACTIVE";
        }
        return status.name().substring("USER_STATUS_".length());
    }

    public static TenantGrant grant(UserTenantGrantEntity entity) {
        return TenantGrant.newBuilder()
                .setId(text(entity.getId()))
                .setUserId(text(entity.getUserId()))
                .setCompanyCode(entity.getCompany().getCode())
                .setWarehouseCode(entity.getWarehouse() == null ? "" : entity.getWarehouse().getCode())
                .setCustomerCode(entity.getCustomer() == null ? "" : entity.getCustomer().getCode())
                .setRoleCode(entity.getRoleCode())
                .setValidFrom(date(entity.getValidFrom()))
                .setValidTo(date(entity.getValidTo()))
                .build();
    }

    public static Role role(RoleEntity entity) {
        return Role.newBuilder()
                .setCode(entity.getCode())
                .setName(entity.getName())
                .setDescription(emptyIfNull(entity.getDescription()))
                .build();
    }
}

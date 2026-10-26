package com.epl.auth;

import com.epl.auth.v1.ChangePasswordRequest;
import com.epl.auth.v1.Company;
import com.epl.auth.v1.CreateUserRequest;
import com.epl.auth.v1.DeleteRoleRequest;
import com.epl.auth.v1.DeleteUserRequest;
import com.epl.auth.v1.GetUserRequest;
import com.epl.auth.v1.GrantTenantRequest;
import com.epl.auth.v1.ListGrantsRequest;
import com.epl.auth.v1.ListUsersRequest;
import com.epl.auth.v1.RevokeGrantRequest;
import com.epl.auth.v1.Role;
import com.epl.auth.v1.RoleServiceGrpc;
import com.epl.auth.v1.TenantGrant;
import com.epl.auth.v1.TenantServiceGrpc;
import com.epl.auth.v1.User;
import com.epl.auth.v1.UserServiceGrpc;
import com.epl.auth.v1.UserStatus;
import com.epl.auth.v1.UserType;
import com.epl.common.v1.PageRequest;
import com.epl.service.core.test.EplTestDatabase;
import com.epl.service.core.test.GrpcTestServer;
import com.epl.service.core.test.GrpcTestServerConfiguration;
import com.google.protobuf.Empty;
import io.grpc.Status;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Testcontainers;

import static com.epl.auth.TenantRegistryTests.code;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Testcontainers(disabledWithoutDocker = true)
@Import(GrpcTestServerConfiguration.class)
class UserServiceTests {

    @DynamicPropertySource
    static void database(DynamicPropertyRegistry registry) {
        EplTestDatabase.register(registry, "iam");
    }

    @Autowired
    private GrpcTestServer grpc;

    private UserServiceGrpc.UserServiceBlockingStub users;
    private RoleServiceGrpc.RoleServiceBlockingStub roles;

    @BeforeEach
    void stubs() {
        users = UserServiceGrpc.newBlockingStub(grpc.channel(null, null, null));
        roles = RoleServiceGrpc.newBlockingStub(grpc.channel(null, null, null));
    }

    @Test
    void userLifecycleWithGrants() {
        var tenants = TenantServiceGrpc.newBlockingStub(grpc.channel(null, null, null));
        tenants.createCompany(Company.newBuilder().setCode("U100").setName("User company").build());

        var created = users.createUser(CreateUserRequest.newBuilder()
                .setUser(User.newBuilder().setUsername("jdoe").setDisplayName("John Doe").setType(UserType.USER_TYPE_INTERNAL)
                        .setHomeCompanyCode("U100").putAttributes("max_adjust_qty", "500"))
                .setInitialPassword("correct horse battery").build());
        assertThat(created.getStatus()).isEqualTo(UserStatus.USER_STATUS_ACTIVE);
        assertThat(created.getAttributesMap()).containsEntry("max_adjust_qty", "500");

        var updated = users.updateUser(created.toBuilder().setDisplayName("John D.").clearAttributes().putAttributes("dept", "OPS").build());
        assertThat(updated.getDisplayName()).isEqualTo("John D.");
        assertThat(users.getUser(GetUserRequest.newBuilder().setId(created.getId()).build()).getAttributesMap())
                .containsExactlyEntriesOf(java.util.Map.of("dept", "OPS"));

        var page = users.listUsers(ListUsersRequest.newBuilder().setQuery("JDO").setPage(PageRequest.newBuilder().setPageSize(10)).build());
        assertThat(page.getUsersList()).extracting("username").containsExactly("jdoe");

        users.changePassword(ChangePasswordRequest.newBuilder().setUserId(created.getId())
                .setCurrentPassword("correct horse battery").setNewPassword("another long password").build());
        assertThatThrownBy(() -> users.changePassword(ChangePasswordRequest.newBuilder().setUserId(created.getId())
                .setCurrentPassword("wrong").setNewPassword("another long password").build()))
                .satisfies(e -> assertThat(code(e)).isEqualTo(Status.Code.PERMISSION_DENIED));

        var grant = users.grantTenant(GrantTenantRequest.newBuilder().setGrant(TenantGrant.newBuilder()
                .setUserId(created.getId()).setCompanyCode("U100").setRoleCode("WMS_OPERATOR")).build());
        assertThat(users.listGrants(ListGrantsRequest.newBuilder().setUserId(created.getId()).build()).getGrantsList())
                .extracting("roleCode").containsExactly("WMS_OPERATOR");
        users.revokeGrant(RevokeGrantRequest.newBuilder().setGrantId(grant.getId()).build());
        assertThat(users.listGrants(ListGrantsRequest.newBuilder().setUserId(created.getId()).build()).getGrantsList()).isEmpty();

        var current = users.getUser(GetUserRequest.newBuilder().setId(created.getId()).build());
        users.deleteUser(DeleteUserRequest.newBuilder().setId(created.getId()).setVersion(current.getAudit().getVersion()).build());
        assertThatThrownBy(() -> users.getUser(GetUserRequest.newBuilder().setId(created.getId()).build()))
                .satisfies(e -> assertThat(code(e)).isEqualTo(Status.Code.NOT_FOUND));
    }

    @Test
    void shortPasswordIsRejected() {
        assertThatThrownBy(() -> users.createUser(CreateUserRequest.newBuilder()
                .setUser(User.newBuilder().setUsername("short").setDisplayName("Short").setType(UserType.USER_TYPE_INTERNAL))
                .setInitialPassword("short").build()))
                .satisfies(e -> assertThat(code(e)).isEqualTo(Status.Code.INVALID_ARGUMENT));
    }

    @Test
    void roles() {
        roles.upsertRole(Role.newBuilder().setCode("TEST_ROLE").setName("Test").build());
        assertThat(roles.listRoles(Empty.getDefaultInstance()).getRolesList()).extracting("code").contains("TEST_ROLE", "WMS_OPERATOR");
        assertThat(roles.upsertRole(Role.newBuilder().setCode("TEST_ROLE").setName("Renamed").build()).getName()).isEqualTo("Renamed");
        roles.deleteRole(DeleteRoleRequest.newBuilder().setCode("TEST_ROLE").build());
        assertThat(roles.listRoles(Empty.getDefaultInstance()).getRolesList()).extracting("code").doesNotContain("TEST_ROLE");
    }
}

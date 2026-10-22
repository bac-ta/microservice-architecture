package com.example.epl.auth;

import com.example.epl.auth.v1.Company;
import com.example.epl.auth.v1.CreateTenantRequest;
import com.example.epl.auth.v1.DeleteCompanyRequest;
import com.example.epl.auth.v1.DeleteTenantRequest;
import com.example.epl.auth.v1.GetCompanyRequest;
import com.example.epl.auth.v1.GetTenantRequest;
import com.example.epl.auth.v1.ListTenantsRequest;
import com.example.epl.auth.v1.ResolveTenantRequest;
import com.example.epl.auth.v1.TenantServiceGrpc;
import com.example.epl.auth.v1.Warehouse;
import com.example.service.core.test.EplTestDatabase;
import com.example.service.core.test.GrpcTestServer;
import com.example.service.core.test.GrpcTestServerConfiguration;
import com.google.protobuf.Empty;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Testcontainers(disabledWithoutDocker = true)
@Import(GrpcTestServerConfiguration.class)
class TenantRegistryTests {

    @DynamicPropertySource
    static void database(DynamicPropertyRegistry registry) {
        EplTestDatabase.register(registry, "iam");
    }

    @Autowired
    private GrpcTestServer grpc;

    private TenantServiceGrpc.TenantServiceBlockingStub tenants;

    @BeforeEach
    void stub() {
        tenants = TenantServiceGrpc.newBlockingStub(grpc.channel(null, null, null));
    }

    @Test
    void companyWarehouseTenantLifecycle() {
        var company = tenants.createCompany(Company.newBuilder().setCode("T100").setName("Test company").setTimeZone("Asia/Tokyo").build());
        assertThat(company.getId()).isNotEmpty();
        assertThat(company.getStatus()).isEqualTo("ACTIVE");
        assertThat(company.getAudit().getCreatedBy()).isEqualTo("tester");

        var renamed = tenants.updateCompany(company.toBuilder().setName("Renamed").build());
        assertThat(renamed.getName()).isEqualTo("Renamed");
        assertThat(renamed.getAudit().getVersion()).isEqualTo(company.getAudit().getVersion() + 1);
        assertThat(tenants.getCompany(GetCompanyRequest.newBuilder().setCode("T100").build()).getName()).isEqualTo("Renamed");

        // stale version
        assertThatThrownBy(() -> tenants.updateCompany(company.toBuilder().setName("Lost update").build()))
                .satisfies(e -> assertThat(code(e)).isEqualTo(Status.Code.ABORTED));

        var warehouse = tenants.createWarehouse(Warehouse.newBuilder()
                .setCompanyCode("T100").setCode("W1").setName("Warehouse 1").setTimeZone("Asia/Tokyo").build());
        assertThat(warehouse.getCompanyCode()).isEqualTo("T100");

        var tenant = tenants.createTenant(CreateTenantRequest.newBuilder()
                .setCompanyCode("T100").setWarehouseCode("W1").setCustomerCode("CU1").setCustomerName("Customer 1").build());
        assertThat(tenant.getStatus()).isEqualTo("ONBOARDING");
        assertThat(tenant.getCustomerName()).isEqualTo("Customer 1");
        assertThat(tenants.listTenants(ListTenantsRequest.newBuilder().setCompanyCode("T100").build()).getTenantsList())
                .extracting("customerCode").containsExactly("CU1");

        var active = tenants.updateTenant(tenant.toBuilder().setStatus("ACTIVE").build());
        assertThat(active.getStatus()).isEqualTo("ACTIVE");

        var resolved = tenants.resolveTenant(ResolveTenantRequest.newBuilder()
                .setCompanyCode("T100").setWarehouseCode("W1").setCustomerCode("CU1").build());
        assertThat(resolved.getTenantId()).isEqualTo(tenant.getId());
        assertThat(resolved.getWarehouseId()).isEqualTo(warehouse.getId());

        // a company with warehouses cannot be deleted
        assertThatThrownBy(() -> tenants.deleteCompany(DeleteCompanyRequest.newBuilder()
                .setCode("T100").setVersion(renamed.getAudit().getVersion()).build()))
                .satisfies(e -> assertThat(code(e)).isEqualTo(Status.Code.FAILED_PRECONDITION));

        tenants.deleteTenant(DeleteTenantRequest.newBuilder().setId(tenant.getId()).setVersion(active.getAudit().getVersion()).build());
        assertThatThrownBy(() -> tenants.getTenant(GetTenantRequest.newBuilder().setId(tenant.getId()).build()))
                .satisfies(e -> assertThat(code(e)).isEqualTo(Status.Code.NOT_FOUND));
    }

    @Test
    void duplicateCodeIsAlreadyExists() {
        tenants.createCompany(Company.newBuilder().setCode("T200").setName("One").build());
        assertThatThrownBy(() -> tenants.createCompany(Company.newBuilder().setCode("T200").setName("Two").build()))
                .satisfies(e -> assertThat(code(e)).isEqualTo(Status.Code.ALREADY_EXISTS));
        assertThat(tenants.listCompanies(Empty.getDefaultInstance()).getCompaniesList()).extracting("code").contains("T200");
    }

    @Test
    void missingFieldsAreInvalidArgument() {
        assertThatThrownBy(() -> tenants.createCompany(Company.newBuilder().setCode("T300").build()))
                .satisfies(e -> assertThat(code(e)).isEqualTo(Status.Code.INVALID_ARGUMENT));
    }

    static Status.Code code(Throwable e) {
        return ((StatusRuntimeException) e).getStatus().getCode();
    }
}

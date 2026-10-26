package com.epl.auth.grpc;

import com.epl.auth.services.CompanyService;
import com.epl.auth.services.CustomerService;
import com.epl.auth.services.TenantService;
import com.epl.auth.services.WarehouseService;
import com.epl.auth.v1.Company;
import com.epl.auth.v1.CreateTenantRequest;
import com.epl.auth.v1.Customer;
import com.epl.auth.v1.DeleteCompanyRequest;
import com.epl.auth.v1.DeleteCustomerRequest;
import com.epl.auth.v1.DeleteTenantRequest;
import com.epl.auth.v1.DeleteWarehouseRequest;
import com.epl.auth.v1.GetCompanyRequest;
import com.epl.auth.v1.GetCustomerRequest;
import com.epl.auth.v1.GetTenantRequest;
import com.epl.auth.v1.GetWarehouseRequest;
import com.epl.auth.v1.ListCompaniesResponse;
import com.epl.auth.v1.ListCustomersRequest;
import com.epl.auth.v1.ListCustomersResponse;
import com.epl.auth.v1.ListTenantsRequest;
import com.epl.auth.v1.ListTenantsResponse;
import com.epl.auth.v1.ListWarehousesRequest;
import com.epl.auth.v1.ListWarehousesResponse;
import com.epl.auth.v1.ResolveTenantRequest;
import com.epl.auth.v1.ResolvedTenant;
import com.epl.auth.v1.Tenant;
import com.epl.auth.v1.TenantServiceGrpc;
import com.epl.auth.v1.Warehouse;
import com.epl.grpc.server.models.annotations.GrpcServer;
import com.epl.service.core.GrpcCalls;
import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;

/**
 * gRPC endpoint epl.auth.v1.TenantService (contract: proto/src/main/proto/epl/auth/v1/auth.proto):
 * the tenant registry (company / warehouse / customer / tenant).
 */
@GrpcServer(TenantServiceGrpc.class)
public class TenantGrpcServer extends TenantServiceGrpc.TenantServiceImplBase {

    private final CompanyService companyService;
    private final WarehouseService warehouseService;
    private final CustomerService customerService;
    private final TenantService tenantService;

    public TenantGrpcServer(CompanyService companyService, WarehouseService warehouseService,
                            CustomerService customerService, TenantService tenantService) {
        this.companyService = companyService;
        this.warehouseService = warehouseService;
        this.customerService = customerService;
        this.tenantService = tenantService;
    }

    // companies ---------------------------------------------------------------

    @Override
    public void listCompanies(Empty request, StreamObserver<ListCompaniesResponse> responseObserver) {
        GrpcCalls.unary(responseObserver, () -> ListCompaniesResponse.newBuilder().addAllCompanies(companyService.list()).build());
    }

    @Override
    public void getCompany(GetCompanyRequest request, StreamObserver<Company> responseObserver) {
        GrpcCalls.unary(responseObserver, () -> companyService.get(request.getCode()));
    }

    @Override
    public void createCompany(Company request, StreamObserver<Company> responseObserver) {
        GrpcCalls.unary(responseObserver, () -> companyService.create(request));
    }

    @Override
    public void updateCompany(Company request, StreamObserver<Company> responseObserver) {
        GrpcCalls.unary(responseObserver, () -> companyService.update(request));
    }

    @Override
    public void deleteCompany(DeleteCompanyRequest request, StreamObserver<Empty> responseObserver) {
        GrpcCalls.unaryEmpty(responseObserver, () -> companyService.delete(request.getCode(), request.getVersion()));
    }

    // warehouses --------------------------------------------------------------

    @Override
    public void listWarehouses(ListWarehousesRequest request, StreamObserver<ListWarehousesResponse> responseObserver) {
        GrpcCalls.unary(responseObserver, () -> ListWarehousesResponse.newBuilder()
                .addAllWarehouses(warehouseService.list(request.getCompanyCode())).build());
    }

    @Override
    public void getWarehouse(GetWarehouseRequest request, StreamObserver<Warehouse> responseObserver) {
        GrpcCalls.unary(responseObserver, () -> warehouseService.get(request.getCompanyCode(), request.getCode()));
    }

    @Override
    public void createWarehouse(Warehouse request, StreamObserver<Warehouse> responseObserver) {
        GrpcCalls.unary(responseObserver, () -> warehouseService.create(request));
    }

    @Override
    public void updateWarehouse(Warehouse request, StreamObserver<Warehouse> responseObserver) {
        GrpcCalls.unary(responseObserver, () -> warehouseService.update(request));
    }

    @Override
    public void deleteWarehouse(DeleteWarehouseRequest request, StreamObserver<Empty> responseObserver) {
        GrpcCalls.unaryEmpty(responseObserver,
                () -> warehouseService.delete(request.getCompanyCode(), request.getCode(), request.getVersion()));
    }

    // customers ---------------------------------------------------------------

    @Override
    public void listCustomers(ListCustomersRequest request, StreamObserver<ListCustomersResponse> responseObserver) {
        GrpcCalls.unary(responseObserver, () -> ListCustomersResponse.newBuilder()
                .addAllCustomers(customerService.list(request.getCompanyCode())).build());
    }

    @Override
    public void getCustomer(GetCustomerRequest request, StreamObserver<Customer> responseObserver) {
        GrpcCalls.unary(responseObserver, () -> customerService.get(request.getCompanyCode(), request.getCode()));
    }

    @Override
    public void createCustomer(Customer request, StreamObserver<Customer> responseObserver) {
        GrpcCalls.unary(responseObserver, () -> customerService.create(request));
    }

    @Override
    public void updateCustomer(Customer request, StreamObserver<Customer> responseObserver) {
        GrpcCalls.unary(responseObserver, () -> customerService.update(request));
    }

    @Override
    public void deleteCustomer(DeleteCustomerRequest request, StreamObserver<Empty> responseObserver) {
        GrpcCalls.unaryEmpty(responseObserver,
                () -> customerService.delete(request.getCompanyCode(), request.getCode(), request.getVersion()));
    }

    // tenants -----------------------------------------------------------------

    @Override
    public void listTenants(ListTenantsRequest request, StreamObserver<ListTenantsResponse> responseObserver) {
        GrpcCalls.unary(responseObserver, () -> ListTenantsResponse.newBuilder()
                .addAllTenants(tenantService.list(request.getCompanyCode(), request.getWarehouseCode())).build());
    }

    @Override
    public void getTenant(GetTenantRequest request, StreamObserver<Tenant> responseObserver) {
        GrpcCalls.unary(responseObserver, () -> tenantService.get(request.getId()));
    }

    @Override
    public void createTenant(CreateTenantRequest request, StreamObserver<Tenant> responseObserver) {
        GrpcCalls.unary(responseObserver, () -> tenantService.create(request));
    }

    @Override
    public void updateTenant(Tenant request, StreamObserver<Tenant> responseObserver) {
        GrpcCalls.unary(responseObserver, () -> tenantService.update(request));
    }

    @Override
    public void deleteTenant(DeleteTenantRequest request, StreamObserver<Empty> responseObserver) {
        GrpcCalls.unaryEmpty(responseObserver, () -> tenantService.delete(request.getId(), request.getVersion()));
    }

    @Override
    public void resolveTenant(ResolveTenantRequest request, StreamObserver<ResolvedTenant> responseObserver) {
        GrpcCalls.unary(responseObserver, () -> tenantService.resolve(request));
    }
}

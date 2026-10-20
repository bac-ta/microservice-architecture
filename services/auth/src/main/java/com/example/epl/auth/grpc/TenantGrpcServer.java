package com.example.epl.auth.grpc;

import com.example.epl.auth.v1.CreateTenantRequest;
import com.example.epl.auth.v1.ListCompaniesResponse;
import com.example.epl.auth.v1.ListTenantsRequest;
import com.example.epl.auth.v1.ListTenantsResponse;
import com.example.epl.auth.v1.ListWarehousesRequest;
import com.example.epl.auth.v1.ListWarehousesResponse;
import com.example.epl.auth.v1.Tenant;
import com.example.epl.auth.v1.TenantServiceGrpc;
import com.example.grpc.server.models.annotations.GrpcServer;
import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;

/**
 * gRPC endpoint epl.auth.v1.TenantService (contract: proto/src/main/proto/epl/auth/v1/auth.proto).
 * Every RPC answers UNIMPLEMENTED until the legacy logic named in its Javadoc is ported.
 */
@GrpcServer(TenantServiceGrpc.class)
public class TenantGrpcServer extends TenantServiceGrpc.TenantServiceImplBase {

    /**
     * Legacy: TMNCUCMNCPNYMST, GWH_TM_WHS, GWH_TM_CUST (identity part)
     */
    @Override
    public void listCompanies(Empty request, StreamObserver<ListCompaniesResponse> responseObserver) {
        responseObserver.onError(NotPorted.error("epl.auth.v1.TenantService/ListCompanies", "TMNCUCMNCPNYMST, GWH_TM_WHS, GWH_TM_CUST (identity part)"));
    }

    /**
     * Legacy: new in EPL
     */
    @Override
    public void listWarehouses(ListWarehousesRequest request, StreamObserver<ListWarehousesResponse> responseObserver) {
        responseObserver.onError(NotPorted.error("epl.auth.v1.TenantService/ListWarehouses", "new in EPL"));
    }

    /**
     * Legacy: new in EPL
     */
    @Override
    public void listTenants(ListTenantsRequest request, StreamObserver<ListTenantsResponse> responseObserver) {
        responseObserver.onError(NotPorted.error("epl.auth.v1.TenantService/ListTenants", "new in EPL"));
    }

    /**
     * Legacy: GWH_TM_CPY tenant copy (onboarding by cloning a template tenant)
     */
    @Override
    public void createTenant(CreateTenantRequest request, StreamObserver<Tenant> responseObserver) {
        responseObserver.onError(NotPorted.error("epl.auth.v1.TenantService/CreateTenant", "GWH_TM_CPY tenant copy (onboarding by cloning a template tenant)"));
    }
}

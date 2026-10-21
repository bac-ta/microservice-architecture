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
 * Every RPC answers UNIMPLEMENTED, naming the screens that call it, until it is implemented.
 */
@GrpcServer(TenantServiceGrpc.class)
public class TenantGrpcServer extends TenantServiceGrpc.TenantServiceImplBase {

    @Override
    public void listCompanies(Empty request, StreamObserver<ListCompaniesResponse> responseObserver) {
        responseObserver.onError(NotPorted.error("epl.auth.v1.TenantService/ListCompanies", "EPL-SY-210W"));
    }

    @Override
    public void listWarehouses(ListWarehousesRequest request, StreamObserver<ListWarehousesResponse> responseObserver) {
        responseObserver.onError(NotPorted.error("epl.auth.v1.TenantService/ListWarehouses", "EPL-SY-210W"));
    }

    @Override
    public void listTenants(ListTenantsRequest request, StreamObserver<ListTenantsResponse> responseObserver) {
        responseObserver.onError(NotPorted.error("epl.auth.v1.TenantService/ListTenants", "EPL-SY-020W", "EPL-SY-020H", "EPL-SY-210W"));
    }

    @Override
    public void createTenant(CreateTenantRequest request, StreamObserver<Tenant> responseObserver) {
        responseObserver.onError(NotPorted.error("epl.auth.v1.TenantService/CreateTenant", "EPL-SY-220W"));
    }
}

package com.example.epl.auth.grpc;

import com.example.epl.auth.services.RoleService;
import com.example.epl.auth.v1.DeleteRoleRequest;
import com.example.epl.auth.v1.ListRolesResponse;
import com.example.epl.auth.v1.Role;
import com.example.epl.auth.v1.RoleServiceGrpc;
import com.example.grpc.server.models.annotations.GrpcServer;
import com.example.service.core.GrpcCalls;
import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;

/**
 * gRPC endpoint epl.auth.v1.RoleService (contract: proto/src/main/proto/epl/auth/v1/auth.proto).
 */
@GrpcServer(RoleServiceGrpc.class)
public class RoleGrpcServer extends RoleServiceGrpc.RoleServiceImplBase {

    private final RoleService roleService;

    public RoleGrpcServer(RoleService roleService) {
        this.roleService = roleService;
    }

    @Override
    public void listRoles(Empty request, StreamObserver<ListRolesResponse> responseObserver) {
        GrpcCalls.unary(responseObserver, () -> ListRolesResponse.newBuilder().addAllRoles(roleService.list()).build());
    }

    @Override
    public void upsertRole(Role request, StreamObserver<Role> responseObserver) {
        GrpcCalls.unary(responseObserver, () -> roleService.upsert(request));
    }

    @Override
    public void deleteRole(DeleteRoleRequest request, StreamObserver<Empty> responseObserver) {
        GrpcCalls.unaryEmpty(responseObserver, () -> roleService.delete(request.getCode()));
    }
}

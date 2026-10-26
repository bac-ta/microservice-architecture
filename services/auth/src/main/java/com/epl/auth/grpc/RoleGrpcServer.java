package com.epl.auth.grpc;

import com.epl.auth.services.RoleService;
import com.epl.auth.v1.DeleteRoleRequest;
import com.epl.auth.v1.ListRolesResponse;
import com.epl.auth.v1.Role;
import com.epl.auth.v1.RoleServiceGrpc;
import com.epl.grpc.server.models.annotations.GrpcServer;
import com.epl.service.core.GrpcCalls;
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

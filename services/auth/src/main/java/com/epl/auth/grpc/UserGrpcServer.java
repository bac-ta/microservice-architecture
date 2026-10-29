package com.epl.auth.grpc;

import com.epl.auth.services.UserService;
import com.epl.auth.v1.ChangePasswordRequest;
import com.epl.auth.v1.CreateUserRequest;
import com.epl.auth.v1.DeleteUserRequest;
import com.epl.auth.v1.GetUserRequest;
import com.epl.auth.v1.GrantTenantRequest;
import com.epl.auth.v1.ListGrantsRequest;
import com.epl.auth.v1.ListGrantsResponse;
import com.epl.auth.v1.ListUsersRequest;
import com.epl.auth.v1.ListUsersResponse;
import com.epl.auth.v1.RevokeGrantRequest;
import com.epl.auth.v1.TenantGrant;
import com.epl.auth.v1.User;
import com.epl.auth.v1.UserServiceGrpc;
import com.epl.grpc.server.models.annotations.GrpcServer;
import com.epl.service.core.GrpcCalls;
import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;

/**
 * gRPC endpoint epl.auth.v1.UserService (contract: proto/src/main/proto/epl/auth/v1/auth.proto).
 */
@RequiredArgsConstructor
@GrpcServer(UserServiceGrpc.class)
public class UserGrpcServer extends UserServiceGrpc.UserServiceImplBase {

    private final UserService userService;

    @Override
    public void createUser(CreateUserRequest request, StreamObserver<User> responseObserver) {
        GrpcCalls.unary(responseObserver, () -> userService.create(request));
    }

    @Override
    public void getUser(GetUserRequest request, StreamObserver<User> responseObserver) {
        GrpcCalls.unary(responseObserver, () -> userService.get(request.getId()));
    }

    @Override
    public void listUsers(ListUsersRequest request, StreamObserver<ListUsersResponse> responseObserver) {
        GrpcCalls.unary(responseObserver, () -> userService.list(request));
    }

    @Override
    public void updateUser(User request, StreamObserver<User> responseObserver) {
        GrpcCalls.unary(responseObserver, () -> userService.update(request));
    }

    @Override
    public void deleteUser(DeleteUserRequest request, StreamObserver<Empty> responseObserver) {
        GrpcCalls.unaryEmpty(responseObserver, () -> userService.delete(request.getId(), request.getVersion()));
    }

    @Override
    public void changePassword(ChangePasswordRequest request, StreamObserver<Empty> responseObserver) {
        GrpcCalls.unaryEmpty(responseObserver, () -> userService.changePassword(request));
    }

    @Override
    public void grantTenant(GrantTenantRequest request, StreamObserver<TenantGrant> responseObserver) {
        GrpcCalls.unary(responseObserver, () -> userService.grant(request.getGrant()));
    }

    @Override
    public void revokeGrant(RevokeGrantRequest request, StreamObserver<Empty> responseObserver) {
        GrpcCalls.unaryEmpty(responseObserver, () -> userService.revokeGrant(request.getGrantId()));
    }

    @Override
    public void listGrants(ListGrantsRequest request, StreamObserver<ListGrantsResponse> responseObserver) {
        GrpcCalls.unary(responseObserver, () -> ListGrantsResponse.newBuilder()
                .addAllGrants(userService.listGrants(request.getUserId())).build());
    }
}

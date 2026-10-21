package com.example.epl.auth.grpc;

import com.example.epl.auth.v1.ChangePasswordRequest;
import com.example.epl.auth.v1.CreateUserRequest;
import com.example.epl.auth.v1.GetUserRequest;
import com.example.epl.auth.v1.GrantTenantRequest;
import com.example.epl.auth.v1.ListGrantsRequest;
import com.example.epl.auth.v1.ListGrantsResponse;
import com.example.epl.auth.v1.ListUsersRequest;
import com.example.epl.auth.v1.ListUsersResponse;
import com.example.epl.auth.v1.RevokeGrantRequest;
import com.example.epl.auth.v1.TenantGrant;
import com.example.epl.auth.v1.User;
import com.example.epl.auth.v1.UserServiceGrpc;
import com.example.grpc.server.models.annotations.GrpcServer;
import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;

/**
 * gRPC endpoint epl.auth.v1.UserService (contract: proto/src/main/proto/epl/auth/v1/auth.proto).
 * Every RPC answers UNIMPLEMENTED, naming the screens that call it, until it is implemented.
 */
@GrpcServer(UserServiceGrpc.class)
public class UserGrpcServer extends UserServiceGrpc.UserServiceImplBase {

    @Override
    public void createUser(CreateUserRequest request, StreamObserver<User> responseObserver) {
        responseObserver.onError(NotPorted.error("epl.auth.v1.UserService/CreateUser", "EPL-SY-110W"));
    }

    @Override
    public void getUser(GetUserRequest request, StreamObserver<User> responseObserver) {
        responseObserver.onError(NotPorted.error("epl.auth.v1.UserService/GetUser", "EPL-SY-110W"));
    }

    @Override
    public void listUsers(ListUsersRequest request, StreamObserver<ListUsersResponse> responseObserver) {
        responseObserver.onError(NotPorted.error("epl.auth.v1.UserService/ListUsers", "EPL-SY-110W"));
    }

    @Override
    public void updateUser(User request, StreamObserver<User> responseObserver) {
        responseObserver.onError(NotPorted.error("epl.auth.v1.UserService/UpdateUser", "EPL-SY-110W"));
    }

    @Override
    public void changePassword(ChangePasswordRequest request, StreamObserver<Empty> responseObserver) {
        responseObserver.onError(NotPorted.error("epl.auth.v1.UserService/ChangePassword", "EPL-SY-130W"));
    }

    @Override
    public void grantTenant(GrantTenantRequest request, StreamObserver<TenantGrant> responseObserver) {
        responseObserver.onError(NotPorted.error("epl.auth.v1.UserService/GrantTenant", "EPL-SY-120W"));
    }

    @Override
    public void revokeGrant(RevokeGrantRequest request, StreamObserver<Empty> responseObserver) {
        responseObserver.onError(NotPorted.error("epl.auth.v1.UserService/RevokeGrant", "EPL-SY-120W"));
    }

    @Override
    public void listGrants(ListGrantsRequest request, StreamObserver<ListGrantsResponse> responseObserver) {
        responseObserver.onError(NotPorted.error("epl.auth.v1.UserService/ListGrants", "EPL-SY-120W"));
    }
}

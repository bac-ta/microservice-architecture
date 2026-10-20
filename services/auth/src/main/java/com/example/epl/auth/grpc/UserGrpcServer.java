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
 * Every RPC answers UNIMPLEMENTED until the legacy logic named in its Javadoc is ported.
 */
@GrpcServer(UserServiceGrpc.class)
public class UserGrpcServer extends UserServiceGrpc.UserServiceImplBase {

    /**
     * Legacy: TMNCUCMNUSERMST / TMNCUEXTUSERMST maintenance
     */
    @Override
    public void createUser(CreateUserRequest request, StreamObserver<User> responseObserver) {
        responseObserver.onError(NotPorted.error("epl.auth.v1.UserService/CreateUser", "TMNCUCMNUSERMST / TMNCUEXTUSERMST maintenance"));
    }

    /**
     * Legacy: new in EPL
     */
    @Override
    public void getUser(GetUserRequest request, StreamObserver<User> responseObserver) {
        responseObserver.onError(NotPorted.error("epl.auth.v1.UserService/GetUser", "new in EPL"));
    }

    /**
     * Legacy: new in EPL
     */
    @Override
    public void listUsers(ListUsersRequest request, StreamObserver<ListUsersResponse> responseObserver) {
        responseObserver.onError(NotPorted.error("epl.auth.v1.UserService/ListUsers", "new in EPL"));
    }

    /**
     * Legacy: new in EPL
     */
    @Override
    public void updateUser(User request, StreamObserver<User> responseObserver) {
        responseObserver.onError(NotPorted.error("epl.auth.v1.UserService/UpdateUser", "new in EPL"));
    }

    /**
     * Legacy: new in EPL
     */
    @Override
    public void changePassword(ChangePasswordRequest request, StreamObserver<Empty> responseObserver) {
        responseObserver.onError(NotPorted.error("epl.auth.v1.UserService/ChangePassword", "new in EPL"));
    }

    /**
     * Legacy: gwh_mpuah (GWH_TM_USAH user ↔ cpny/whs/cust + role group)
     */
    @Override
    public void grantTenant(GrantTenantRequest request, StreamObserver<TenantGrant> responseObserver) {
        responseObserver.onError(NotPorted.error("epl.auth.v1.UserService/GrantTenant", "gwh_mpuah (GWH_TM_USAH user ↔ cpny/whs/cust + role group)"));
    }

    /**
     * Legacy: new in EPL
     */
    @Override
    public void revokeGrant(RevokeGrantRequest request, StreamObserver<Empty> responseObserver) {
        responseObserver.onError(NotPorted.error("epl.auth.v1.UserService/RevokeGrant", "new in EPL"));
    }

    /**
     * Legacy: new in EPL
     */
    @Override
    public void listGrants(ListGrantsRequest request, StreamObserver<ListGrantsResponse> responseObserver) {
        responseObserver.onError(NotPorted.error("epl.auth.v1.UserService/ListGrants", "new in EPL"));
    }
}

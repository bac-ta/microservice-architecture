package com.epl.auth.grpc;

import com.epl.auth.v1.AuthServiceGrpc;
import com.epl.auth.v1.JwksResponse;
import com.epl.auth.v1.LoginRequest;
import com.epl.auth.v1.LogoutRequest;
import com.epl.auth.v1.RefreshRequest;
import com.epl.auth.v1.TokenResponse;
import com.epl.grpc.server.models.annotations.GrpcServer;
import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;

/**
 * gRPC endpoint epl.auth.v1.AuthService (contract: proto/src/main/proto/epl/auth/v1/auth.proto).
 * Every RPC answers UNIMPLEMENTED, naming the screens that call it, until it is implemented.
 */
@GrpcServer(AuthServiceGrpc.class)
public class AuthGrpcServer extends AuthServiceGrpc.AuthServiceImplBase {

    @Override
    public void login(LoginRequest request, StreamObserver<TokenResponse> responseObserver) {
        responseObserver.onError(NotPorted.error("epl.auth.v1.AuthService/Login", "EPL-SY-010W", "EPL-SY-010H"));
    }

    @Override
    public void refresh(RefreshRequest request, StreamObserver<TokenResponse> responseObserver) {
        responseObserver.onError(NotPorted.error("epl.auth.v1.AuthService/Refresh"));
    }

    @Override
    public void logout(LogoutRequest request, StreamObserver<Empty> responseObserver) {
        responseObserver.onError(NotPorted.error("epl.auth.v1.AuthService/Logout"));
    }

    @Override
    public void getJwks(Empty request, StreamObserver<JwksResponse> responseObserver) {
        responseObserver.onError(NotPorted.error("epl.auth.v1.AuthService/GetJwks"));
    }
}

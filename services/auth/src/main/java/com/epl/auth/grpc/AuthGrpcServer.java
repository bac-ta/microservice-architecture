package com.epl.auth.grpc;

import com.epl.auth.services.AuthenticationService;
import com.epl.auth.services.SigningKeyService;
import com.epl.auth.v1.AuthServiceGrpc;
import com.epl.auth.v1.JwksResponse;
import com.epl.auth.v1.LoginRequest;
import com.epl.auth.v1.LogoutRequest;
import com.epl.auth.v1.RefreshRequest;
import com.epl.auth.v1.TokenResponse;
import com.epl.grpc.server.models.annotations.GrpcServer;
import com.epl.service.core.GrpcCalls;
import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;

/**
 * gRPC endpoint epl.auth.v1.AuthService (contract: proto/src/main/proto/epl/auth/v1/auth.proto).
 * Login, Refresh, Logout and GetJwks are public at the gateway (no JWT needed).
 */
@RequiredArgsConstructor
@GrpcServer(AuthServiceGrpc.class)
public class AuthGrpcServer extends AuthServiceGrpc.AuthServiceImplBase {

    private final AuthenticationService authenticationService;
    private final SigningKeyService signingKeyService;

    @Override
    public void login(LoginRequest request, StreamObserver<TokenResponse> responseObserver) {
        GrpcCalls.unary(responseObserver, () -> authenticationService.login(request));
    }

    @Override
    public void refresh(RefreshRequest request, StreamObserver<TokenResponse> responseObserver) {
        GrpcCalls.unary(responseObserver, () -> authenticationService.refresh(request));
    }

    @Override
    public void logout(LogoutRequest request, StreamObserver<Empty> responseObserver) {
        GrpcCalls.unaryEmpty(responseObserver, () -> authenticationService.logout(request));
    }

    @Override
    public void getJwks(Empty request, StreamObserver<JwksResponse> responseObserver) {
        GrpcCalls.unary(responseObserver, () -> JwksResponse.newBuilder().setJwksJson(signingKeyService.jwksJson()).build());
    }
}

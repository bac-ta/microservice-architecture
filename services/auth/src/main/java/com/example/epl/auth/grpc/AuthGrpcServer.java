package com.example.epl.auth.grpc;

import com.example.epl.auth.v1.AuthServiceGrpc;
import com.example.epl.auth.v1.JwksResponse;
import com.example.epl.auth.v1.LoginRequest;
import com.example.epl.auth.v1.LogoutRequest;
import com.example.epl.auth.v1.RefreshRequest;
import com.example.epl.auth.v1.TokenResponse;
import com.example.grpc.server.models.annotations.GrpcServer;
import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;

/**
 * gRPC endpoint epl.auth.v1.AuthService (contract: proto/src/main/proto/epl/auth/v1/auth.proto).
 * Every RPC answers UNIMPLEMENTED until the legacy logic named in its Javadoc is ported.
 */
@GrpcServer(AuthServiceGrpc.class)
public class AuthGrpcServer extends AuthServiceGrpc.AuthServiceImplBase {

    /**
     * Legacy: AGS_PR_TOKEN_010 / ENCRYPTDECRYPT (AES token) — replaced by ES256 JWT
     */
    @Override
    public void login(LoginRequest request, StreamObserver<TokenResponse> responseObserver) {
        responseObserver.onError(NotPorted.error("epl.auth.v1.AuthService/Login", "AGS_PR_TOKEN_010 / ENCRYPTDECRYPT (AES token) — replaced by ES256 JWT"));
    }

    /**
     * Legacy: none (no refresh in GWH)
     */
    @Override
    public void refresh(RefreshRequest request, StreamObserver<TokenResponse> responseObserver) {
        responseObserver.onError(NotPorted.error("epl.auth.v1.AuthService/Refresh", "none (no refresh in GWH)"));
    }

    /**
     * Legacy: TJNCUEXTUSERINFO session end
     */
    @Override
    public void logout(LogoutRequest request, StreamObserver<Empty> responseObserver) {
        responseObserver.onError(NotPorted.error("epl.auth.v1.AuthService/Logout", "TJNCUEXTUSERINFO session end"));
    }

    /**
     * Legacy: new in EPL
     */
    @Override
    public void getJwks(Empty request, StreamObserver<JwksResponse> responseObserver) {
        responseObserver.onError(NotPorted.error("epl.auth.v1.AuthService/GetJwks", "new in EPL"));
    }
}

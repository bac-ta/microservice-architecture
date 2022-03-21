package com.entropy.authentication.mock;

import com.entropy.authentication.security.Interceptor.grpc.GrpcGlobal;
import io.grpc.Context;
import io.grpc.Contexts;
import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;

@SuppressWarnings("unused")
public class MockHeaderServerInterceptor implements ServerInterceptor {

    // Instance variables
    // ------------------------------------------------------------------------
    private final String loginInfo;

    // Constructors
    // ------------------------------------------------------------------------
    public MockHeaderServerInterceptor() {
        this.loginInfo = "user";
    }

    // Overrided methods
    // ------------------------------------------------------------------------
    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(ServerCall<ReqT, RespT> call, Metadata headers,
            ServerCallHandler<ReqT, RespT> next) {
        Context context = Context.current().withValue(GrpcGlobal.LOGIN_INFO, loginInfo);
        return Contexts.interceptCall(context, call, headers, next);
    }
}

package com.entropy.authentication.securities.Interceptors.grpc;

import io.grpc.Context;
import io.grpc.Contexts;
import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Cipher;

public class GrpcAuthServerInterceptor implements ServerInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(GrpcAuthServerInterceptor.class);

    private Cipher descrypter;

    private String key;

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(ServerCall<ReqT, RespT> call, Metadata headers,
                                                                 ServerCallHandler<ReqT, RespT> next) {

        String token = headers.get(GrpcGlobal.AUTH_TOKEN_METADATA);

        return Contexts.interceptCall(Context.current(), call, headers, next);
    }

    private String decrypt(String value) {
        return "test";
    }
}

package com.example.authentication.logging;

import com.example.authentication.models.RequestInfo;
import com.example.authentication.securities.interceptors.grpc.GrpcGlobal;
import io.grpc.ForwardingServerCallListener.SimpleForwardingServerCallListener;
import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import io.grpc.health.v1.HealthGrpc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GrpcServerLoggingInterceptor implements ServerInterceptor {

    private static final Logger LOGGER = LoggerFactory.getLogger(GrpcServerLoggingInterceptor.class);

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
            ServerCall<ReqT, RespT> call,
            Metadata headers,
            ServerCallHandler<ReqT, RespT> next) {

        // check request health check
        if (call.getMethodDescriptor().getFullMethodName().equals(HealthGrpc.getCheckMethod().getFullMethodName())) {
            // ignored health check
            return next.startCall(call, headers);
        }
        final String method = call.getMethodDescriptor().getFullMethodName();


        return new SimpleForwardingServerCallListener<>(next.startCall(call, headers)) {
            @Override
            public void onMessage(ReqT message) {
                final RequestInfo requestInfo = headers.get(GrpcGlobal.REQUEST_INFO_METADATA);
                LOGGER.info("[{}] RPC called. method: {}",
                        requestInfo != null ? requestInfo.getRequestId() : "empty",
                        method);
                super.onMessage(message);
            }
        };
    }
}

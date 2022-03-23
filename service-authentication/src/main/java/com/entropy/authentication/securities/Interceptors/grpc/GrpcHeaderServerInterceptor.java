package com.entropy.authentication.securities.Interceptors.grpc;

import com.entropy.authentication.models.RequestInfo;
import io.grpc.Context;
import io.grpc.Contexts;
import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;

public class GrpcHeaderServerInterceptor implements ServerInterceptor {

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(ServerCall<ReqT, RespT> call, Metadata headers,
                                                                 ServerCallHandler<ReqT, RespT> next) {

        // Request info
        RequestInfo requestInfo = headers.get(GrpcGlobal.REQUEST_INFO_METADATA);
        Context context = Context.current();
        if (requestInfo != null) {
            context = context.withValue(GrpcGlobal.REQUEST_INFO, requestInfo);
        }
        String loginInfo = "user";
        context = context.withValue(GrpcGlobal.LOGIN_INFO, loginInfo);

        if (requestInfo != null) {
            return Contexts.interceptCall(context, call, headers, next);
        }

        return next.startCall(call, headers);
    }
}

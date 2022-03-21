package com.entropy.authentication.security.Interceptor.grpc;

import com.entropy.authentication.models.RequestInfo;
import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ClientCall;
import io.grpc.ClientInterceptor;
import io.grpc.Context;
import io.grpc.ForwardingClientCall.SimpleForwardingClientCall;
import io.grpc.ForwardingClientCallListener;
import io.grpc.Metadata;
import io.grpc.MethodDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("Duplicates")
public class GrpcHeaderNoLoginClientInterceptor implements ClientInterceptor {

    // Logger
    private static final Logger logger = LoggerFactory.getLogger(GrpcHeaderNoLoginClientInterceptor.class);

    // Overrided methods
    // ------------------------------------------------------------------------
    @Override
    public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(MethodDescriptor<ReqT, RespT> method,
                                                               CallOptions call, Channel channel) {

        return new SimpleForwardingClientCall<ReqT, RespT>(channel.newCall(method, call)) {
            @Override
            public void start(Listener<RespT> responseListener, Metadata headers) {

                // リクエスト情報の転送
                RequestInfo requestInfo = GrpcGlobal.REQUEST_INFO.get(Context.current());
                if (requestInfo != null) {
                    headers.put(GrpcGlobal.REQUEST_INFO_METADATA, requestInfo);
                }

                super.start(new ForwardingClientCallListener.SimpleForwardingClientCallListener<RespT>(responseListener) {

                    @Override
                    public void onMessage(RespT message) {
                        if (logger.isInfoEnabled()) {
                            logger.info("RPC request. method: {}", method.getFullMethodName());
                        }
                        super.onMessage(message);
                    }

                    @Override
                    public void onHeaders(Metadata headers) {
                        super.onHeaders(headers);
                    }

                }, headers);
            }
        };
    }
}

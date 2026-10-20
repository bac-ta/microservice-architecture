package com.example.authentication.securities.interceptors.grpc;

import com.example.authentication.models.RequestInfo;
import com.example.authentication.utils.CurrentThreadExecutor;
import io.grpc.Context;
import io.grpc.Metadata;

import java.util.concurrent.Executor;

public final class GrpcGlobal {

    public static final Context.Key<String> LOGIN_INFO = Context.key("demo.authorize");
    public static final Metadata.Key<String> AUTH_TOKEN_METADATA = Metadata.Key.of("demo.auth.token",
            Metadata.ASCII_STRING_MARSHALLER);
    public static final Context.Key<RequestInfo> REQUEST_INFO = Context.key("demo.request");
    public static final Metadata.Key<RequestInfo> REQUEST_INFO_METADATA = Metadata.Key.of("demo.request-bin",
            new RequestInfoMarshaller());

    private GrpcGlobal() {
    }

    private static class RequestInfoMarshaller implements Metadata.BinaryMarshaller<RequestInfo> {
        @Override
        public byte[] toBytes(RequestInfo value) {
            return value.toBytes();
        }

        @Override
        public RequestInfo parseBytes(byte[] serialized) {
            return RequestInfo.newRequestInfo(serialized);
        }
    }

    public static Executor currentThreadContextExecutor() {
        return Context.currentContextExecutor(new CurrentThreadExecutor());
    }
}

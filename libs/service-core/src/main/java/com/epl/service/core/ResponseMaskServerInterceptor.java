package com.epl.service.core;

import com.google.protobuf.Message;
import io.grpc.Context;
import io.grpc.Contexts;
import io.grpc.ForwardingServerCall;
import io.grpc.Metadata;
import io.grpc.MethodDescriptor;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import io.grpc.Status;

/**
 * Reads the {@link ResponseMask} of the call from the metadata {@code x-field-mask}, attaches it to the gRPC
 * context and trims every response message to it. A path naming no field of the response answers INVALID_ARGUMENT.
 * Registered by {@link ServiceCoreAutoConfiguration#eplServerInterceptors}.
 */
public class ResponseMaskServerInterceptor implements ServerInterceptor {

    public static final Metadata.Key<String> FIELD_MASK = Metadata.Key.of("x-field-mask", Metadata.ASCII_STRING_MARSHALLER);

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(ServerCall<ReqT, RespT> call, Metadata headers,
                                                                 ServerCallHandler<ReqT, RespT> next) {
        ResponseMask mask;
        try {
            mask = ResponseMask.parse(headers.get(FIELD_MASK));
            if (mask.isAll()) {
                return next.startCall(call, headers);
            }
            if (call.getMethodDescriptor().getResponseMarshaller() instanceof MethodDescriptor.PrototypeMarshaller<?> marshaller
                    && marshaller.getMessagePrototype() instanceof Message prototype) {
                mask.validate(prototype.getDescriptorForType());
            }
        } catch (IllegalArgumentException e) {
            call.close(Status.INVALID_ARGUMENT.withDescription("x-field-mask: " + e.getMessage()), new Metadata());
            return new ServerCall.Listener<>() {
            };
        }

        var trimming = new ForwardingServerCall.SimpleForwardingServerCall<>(call) {
            @Override
            @SuppressWarnings("unchecked")
            public void sendMessage(RespT message) {
                super.sendMessage(message instanceof Message response ? (RespT) mask.trim(response) : message);
            }
        };
        return Contexts.interceptCall(mask.attach(Context.current()), trimming, headers, next);
    }
}

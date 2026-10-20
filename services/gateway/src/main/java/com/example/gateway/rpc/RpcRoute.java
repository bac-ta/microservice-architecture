package com.example.gateway.rpc;

import com.google.protobuf.Message;
import io.grpc.MethodDescriptor;
import io.grpc.MethodDescriptor.MethodType;

/**
 * A backend RPC reachable through the gateway.
 *
 * @param backend          backend that serves it (key of {@code epl.gateway.backends})
 * @param method           generated method descriptor, with protobuf marshallers
 * @param requestPrototype default instance of the request message, used to parse the JSON body
 */
public record RpcRoute(String backend, MethodDescriptor<Message, Message> method, Message requestPrototype) {

    @SuppressWarnings("unchecked")
    static RpcRoute of(String backend, MethodDescriptor<?, ?> method) {
        var typed = (MethodDescriptor<Message, Message>) method;
        var marshaller = (MethodDescriptor.PrototypeMarshaller<Message>) typed.getRequestMarshaller();
        return new RpcRoute(backend, typed, marshaller.getMessagePrototype());
    }

    public String fullMethodName() {
        return method.getFullMethodName();
    }

    public MethodType type() {
        return method.getType();
    }

    @SuppressWarnings("unchecked")
    public Message responsePrototype() {
        return ((MethodDescriptor.PrototypeMarshaller<Message>) method.getResponseMarshaller()).getMessagePrototype();
    }
}

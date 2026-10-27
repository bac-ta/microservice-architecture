package com.epl.service.core;

import com.google.protobuf.Empty;
import io.grpc.CallOptions;
import io.grpc.ClientInterceptors;
import io.grpc.ManagedChannel;
import io.grpc.Metadata;
import io.grpc.MethodDescriptor;
import io.grpc.Server;
import io.grpc.ServerInterceptors;
import io.grpc.ServerServiceDefinition;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import io.grpc.protobuf.ProtoUtils;
import io.grpc.stub.ClientCalls;
import io.grpc.stub.MetadataUtils;
import io.grpc.stub.ServerCalls;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CallLoggingServerInterceptorTests {

    private static final MethodDescriptor<Empty, Empty> OK = method("Ok");
    private static final MethodDescriptor<Empty, Empty> BROKEN = method("Broken");

    private final AtomicReference<String> requestIdSeen = new AtomicReference<>();
    private Server server;
    private ManagedChannel channel;

    @BeforeEach
    void start() throws IOException {
        var service = ServerServiceDefinition.builder("test.Test")
                .addMethod(OK, ServerCalls.asyncUnaryCall((request, observer) -> {
                    requestIdSeen.set(MDC.get(CallLoggingServerInterceptor.MDC_REQUEST_ID));
                    observer.onNext(Empty.getDefaultInstance());
                    observer.onCompleted();
                }))
                .addMethod(BROKEN, ServerCalls.asyncUnaryCall((request, observer) -> {
                    throw new IllegalStateException("secret detail");
                }))
                .build();
        server = InProcessServerBuilder.forName("call-logging").directExecutor()
                .addService(ServerInterceptors.intercept(service, new CallLoggingServerInterceptor()))
                .build().start();
        channel = InProcessChannelBuilder.forName("call-logging").directExecutor().build();
    }

    @AfterEach
    void stop() {
        channel.shutdownNow();
        server.shutdownNow();
    }

    @Test
    void requestIdIsInTheMdcDuringTheCall() {
        var headers = new Metadata();
        headers.put(CallLoggingServerInterceptor.REQUEST_ID, "req-123");
        var withId = ClientInterceptors.intercept(channel, MetadataUtils.newAttachHeadersInterceptor(headers));

        ClientCalls.blockingUnaryCall(withId, OK, CallOptions.DEFAULT, Empty.getDefaultInstance());

        assertThat(requestIdSeen.get()).isEqualTo("req-123");
        assertThat(MDC.get(CallLoggingServerInterceptor.MDC_REQUEST_ID)).isNull();
    }

    @Test
    void invalidRequestIdIsReplaced() {
        var headers = new Metadata();
        headers.put(CallLoggingServerInterceptor.REQUEST_ID, "bad id\twith spaces");
        var withId = ClientInterceptors.intercept(channel, MetadataUtils.newAttachHeadersInterceptor(headers));

        ClientCalls.blockingUnaryCall(withId, OK, CallOptions.DEFAULT, Empty.getDefaultInstance());

        assertThat(requestIdSeen.get()).isNotBlank().doesNotContain(" ");
    }

    @Test
    void escapedExceptionBecomesGenericInternal() {
        assertThatThrownBy(() -> ClientCalls.blockingUnaryCall(channel, BROKEN, CallOptions.DEFAULT,
                Empty.getDefaultInstance()))
                .isInstanceOfSatisfying(StatusRuntimeException.class, e -> {
                    assertThat(e.getStatus().getCode()).isEqualTo(Status.Code.INTERNAL);
                    assertThat(e.getStatus().getDescription()).doesNotContain("secret");
                });
    }

    private static MethodDescriptor<Empty, Empty> method(String name) {
        return MethodDescriptor.<Empty, Empty>newBuilder()
                .setType(MethodDescriptor.MethodType.UNARY)
                .setFullMethodName(MethodDescriptor.generateFullMethodName("test.Test", name))
                .setRequestMarshaller(ProtoUtils.marshaller(Empty.getDefaultInstance()))
                .setResponseMarshaller(ProtoUtils.marshaller(Empty.getDefaultInstance()))
                .build();
    }
}

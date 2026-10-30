package com.epl.service.core;

import com.epl.common.v1.Audit;
import com.epl.inbound.v1.Arrival;
import com.epl.inbound.v1.ArrivalLine;
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

import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ResponseMaskTests {

    private static final Arrival ARRIVAL = Arrival.newBuilder()
            .setArrivalNo("AR0001")
            .setStatus("PLANNED")
            .setAudit(Audit.newBuilder().setVersion(3).setCreatedBy("alice"))
            .addLines(ArrivalLine.newBuilder().setLineNo(1).setProductCode("P001").setStatus("PLANNED"))
            .addLines(ArrivalLine.newBuilder().setLineNo(2).setProductCode("P002").setStatus("PLANNED"))
            .build();

    private static final MethodDescriptor<Arrival, Arrival> GET = MethodDescriptor.<Arrival, Arrival>newBuilder()
            .setType(MethodDescriptor.MethodType.UNARY)
            .setFullMethodName("test.Test/Get")
            .setRequestMarshaller(ProtoUtils.marshaller(Arrival.getDefaultInstance()))
            .setResponseMarshaller(ProtoUtils.marshaller(Arrival.getDefaultInstance()))
            .build();

    private final AtomicReference<Boolean> linesLoaded = new AtomicReference<>();
    private Server server;
    private ManagedChannel channel;

    @BeforeEach
    void start() throws IOException {
        var service = ServerServiceDefinition.builder("test.Test")
                .addMethod(GET, ServerCalls.asyncUnaryCall((request, observer) -> {
                    linesLoaded.set(ResponseMask.current().includes("lines"));
                    observer.onNext(ARRIVAL);
                    observer.onCompleted();
                }))
                .build();
        server = InProcessServerBuilder.forName("response-mask").directExecutor()
                .addService(ServerInterceptors.intercept(service, new ResponseMaskServerInterceptor()))
                .build().start();
        channel = InProcessChannelBuilder.forName("response-mask").directExecutor().build();
    }

    @AfterEach
    void stop() {
        channel.shutdownNow();
        server.shutdownNow();
    }

    @Test
    void trimsNestedAndRepeatedFields() {
        var trimmed = ResponseMask.parse("arrivalNo, audit.version, lines.productCode").trim(ARRIVAL);

        assertThat(trimmed).isEqualTo(Arrival.newBuilder()
                .setArrivalNo("AR0001")
                .setAudit(Audit.newBuilder().setVersion(3))
                .addLines(ArrivalLine.newBuilder().setProductCode("P001"))
                .addLines(ArrivalLine.newBuilder().setProductCode("P002"))
                .build());
    }

    @Test
    void includesAField() {
        var mask = ResponseMask.parse("arrivalNo,lines.productCode,audit");

        assertThat(mask.includes("lines")).isTrue();
        assertThat(mask.includes("lines.productCode")).isTrue();
        assertThat(mask.includes("lines.status")).isFalse();
        // the whole audit is read
        assertThat(mask.includes("audit.version")).isTrue();
        assertThat(mask.includes("status")).isFalse();
        assertThat(ResponseMask.ALL.includes("anything")).isTrue();
    }

    @Test
    void unknownFieldIsRejected() {
        var mask = ResponseMask.parse("arrivalNo,nope");
        assertThatThrownBy(() -> mask.validate(Arrival.getDescriptor())).hasMessageContaining("has no field 'nope'");
        assertThatThrownBy(() -> ResponseMask.parse("arrivalNo.x").validate(Arrival.getDescriptor()))
                .hasMessageContaining("'arrivalNo' has no sub-fields");
        assertThatThrownBy(() -> ResponseMask.parse("lines..productCode")).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void callWithMaskGetsOnlyTheSelectedFieldsAndTheServiceSeesTheMask() {
        var response = call("arrivalNo,status");

        assertThat(response).isEqualTo(Arrival.newBuilder().setArrivalNo("AR0001").setStatus("PLANNED").build());
        assertThat(linesLoaded.get()).isFalse();
    }

    @Test
    void callWithoutMaskGetsEverything() {
        assertThat(call(null)).isEqualTo(ARRIVAL);
        assertThat(linesLoaded.get()).isTrue();
    }

    @Test
    void callWithInvalidMaskIsInvalidArgument() {
        assertThatThrownBy(() -> call("nope"))
                .isInstanceOf(StatusRuntimeException.class)
                .satisfies(e -> assertThat(((StatusRuntimeException) e).getStatus().getCode()).isEqualTo(Status.Code.INVALID_ARGUMENT));
    }

    private Arrival call(String fieldMask) {
        var headers = new Metadata();
        if (fieldMask != null) {
            headers.put(ResponseMaskServerInterceptor.FIELD_MASK, fieldMask);
        }
        var intercepted = io.grpc.ClientInterceptors.intercept(channel, MetadataUtils.newAttachHeadersInterceptor(headers));
        return ClientCalls.blockingUnaryCall(intercepted, GET, io.grpc.CallOptions.DEFAULT, Arrival.getDefaultInstance());
    }
}

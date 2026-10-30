package com.epl.inbound;

import com.epl.common.v1.Date;
import com.epl.common.v1.Decimal;
import com.epl.common.v1.Party;
import com.epl.common.v1.Quantity;
import com.epl.inbound.v1.Arrival;
import com.epl.inbound.v1.ArrivalLine;
import com.epl.inbound.v1.ArrivalServiceGrpc;
import com.epl.inbound.v1.CancelArrivalRequest;
import com.epl.inbound.v1.CreateArrivalRequest;
import com.epl.inbound.v1.DeleteArrivalRequest;
import com.epl.inbound.v1.GetArrivalRequest;
import com.epl.inbound.v1.SearchArrivalsRequest;
import com.epl.inbound.v1.UpdateArrivalRequest;
import com.epl.service.core.test.EplServiceTestConfiguration;
import com.epl.service.core.test.EplTestDatabase;
import com.epl.service.core.test.GrpcTestServer;
import com.epl.service.core.ResponseMaskServerInterceptor;
import io.grpc.Metadata;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.MetadataUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Testcontainers(disabledWithoutDocker = true)
@Import(EplServiceTestConfiguration.class)
class ArrivalCrudTests {

    @DynamicPropertySource
    static void database(DynamicPropertyRegistry registry) {
        EplTestDatabase.register(registry, "inb");
    }

    @Autowired
    private GrpcTestServer grpc;

    @Test
    void arrivalLifecycle() {
        var arrivals = ArrivalServiceGrpc.newBlockingStub(grpc.tenantChannel());
        var created = arrivals.createArrival(CreateArrivalRequest.newBuilder().setArrival(Arrival.newBuilder()
                .setScheduledDate(Date.newBuilder().setYear(2026).setMonth(11).setDay(2))
                .setTransactionKind("010").setInvoiceNo("INV-1")
                .setSupplier(Party.newBuilder().setCode("SUP1").addNames("Supplier One"))
                .addLines(line(0, "P001", "24"))
                .addLines(line(0, "P002", "12"))).build());
        assertThat(created.getArrivalNo()).startsWith("AR");
        assertThat(created.getLinesList()).extracting("lineNo").containsExactly(1, 2);
        assertThat(created.getSupplier().getNamesList()).containsExactly("Supplier One");
        assertThat(created.getSupplier().getCode()).isEqualTo("SUP1");

        // change line 2, drop line 1, add line 3
        var updated = arrivals.updateArrival(UpdateArrivalRequest.newBuilder().setArrival(created.toBuilder()
                .clearLines().addLines(line(2, "P002", "30")).addLines(line(3, "P003", "5"))).build());
        assertThat(updated.getLinesList()).extracting("lineNo").containsExactly(2, 3);
        assertThat(updated.getLines(0).getPlanned().getTotalPieces().getValue()).isEqualTo("30");

        assertThat(arrivals.searchArrivals(SearchArrivalsRequest.newBuilder().setProductCode("P003").build()).getArrivalsList())
                .extracting("arrivalNo").containsExactly(created.getArrivalNo());
        assertThat(arrivals.searchArrivals(SearchArrivalsRequest.newBuilder().setProductCode("P001").build()).getArrivalsList()).isEmpty();

        var cancelled = arrivals.cancelArrival(CancelArrivalRequest.newBuilder().setArrivalNo(created.getArrivalNo())
                .setReasonCode("01").setVersion(updated.getAudit().getVersion()).build());
        assertThat(cancelled.getStatus()).isEqualTo("CANCELLED");
        assertThatThrownBy(() -> arrivals.deleteArrival(DeleteArrivalRequest.newBuilder()
                .setArrivalNo(created.getArrivalNo()).setVersion(cancelled.getAudit().getVersion()).build()))
                .satisfies(e -> assertThat(code(e)).isEqualTo(Status.Code.FAILED_PRECONDITION));
    }

    @Test
    void deletePlannedArrival() {
        var arrivals = ArrivalServiceGrpc.newBlockingStub(grpc.tenantChannel());
        var created = arrivals.createArrival(CreateArrivalRequest.newBuilder().setArrival(Arrival.newBuilder()
                .setArrivalNo("MANUAL-1").setScheduledDate(Date.newBuilder().setYear(2026).setMonth(11).setDay(3))
                .setTransactionKind("010").addLines(line(1, "P001", "1"))).build());
        arrivals.deleteArrival(DeleteArrivalRequest.newBuilder().setArrivalNo("MANUAL-1").setVersion(created.getAudit().getVersion()).build());
        assertThatThrownBy(() -> arrivals.getArrival(GetArrivalRequest.newBuilder().setArrivalNo("MANUAL-1").build()))
                .satisfies(e -> assertThat(code(e)).isEqualTo(Status.Code.NOT_FOUND));
    }

    @Test
    void invalidArrivalsAreRejected() {
        var arrivals = ArrivalServiceGrpc.newBlockingStub(grpc.tenantChannel());
        var noLines = Arrival.newBuilder().setScheduledDate(Date.newBuilder().setYear(2026).setMonth(11).setDay(3)).setTransactionKind("010");
        assertThatThrownBy(() -> arrivals.createArrival(CreateArrivalRequest.newBuilder().setArrival(noLines).build()))
                .satisfies(e -> assertThat(code(e)).isEqualTo(Status.Code.INVALID_ARGUMENT));
        assertThatThrownBy(() -> arrivals.createArrival(CreateArrivalRequest.newBuilder()
                .setArrival(noLines.clone().addLines(line(1, "P001", "0"))).build()))
                .satisfies(e -> assertThat(code(e)).isEqualTo(Status.Code.INVALID_ARGUMENT));
    }

    @Test
    void getReadsOnlyTheSelectedFields() {
        var arrivals = ArrivalServiceGrpc.newBlockingStub(grpc.tenantChannel());
        var created = arrivals.createArrival(CreateArrivalRequest.newBuilder().setArrival(Arrival.newBuilder()
                .setScheduledDate(Date.newBuilder().setYear(2026).setMonth(11).setDay(3))
                .setTransactionKind("010")
                .addLines(line(0, "P001", "24"))).build());
        var get = GetArrivalRequest.newBuilder().setArrivalNo(created.getArrivalNo()).build();

        // header only: the lines are not loaded
        var header = withFieldMask(arrivals, "arrivalNo,status").getArrival(get);
        assertThat(header).isEqualTo(Arrival.newBuilder().setArrivalNo(created.getArrivalNo()).setStatus("PLANNED").build());

        var lines = withFieldMask(arrivals, "lines.productCode").getArrival(get);
        assertThat(lines.getLinesList()).extracting("productCode").containsExactly("P001");
        assertThat(lines.getArrivalNo()).isEmpty();
    }

    private static ArrivalServiceGrpc.ArrivalServiceBlockingStub withFieldMask(ArrivalServiceGrpc.ArrivalServiceBlockingStub stub, String paths) {
        var headers = new Metadata();
        headers.put(ResponseMaskServerInterceptor.FIELD_MASK, paths);
        return stub.withInterceptors(MetadataUtils.newAttachHeadersInterceptor(headers));
    }

    private static ArrivalLine line(int lineNo, String product, String totalPieces) {
        return ArrivalLine.newBuilder().setLineNo(lineNo).setProductCode(product)
                .setPlanned(Quantity.newBuilder().setTotalPieces(Decimal.newBuilder().setValue(totalPieces)))
                .build();
    }

    private static Status.Code code(Throwable e) {
        return ((StatusRuntimeException) e).getStatus().getCode();
    }
}

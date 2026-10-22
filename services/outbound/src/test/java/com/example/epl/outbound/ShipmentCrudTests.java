package com.example.epl.outbound;

import com.example.epl.common.v1.Address;
import com.example.epl.common.v1.Date;
import com.example.epl.common.v1.Decimal;
import com.example.epl.common.v1.Party;
import com.example.epl.common.v1.Quantity;
import com.example.epl.outbound.v1.CancelShipmentRequest;
import com.example.epl.outbound.v1.CreateShipmentRequest;
import com.example.epl.outbound.v1.DeleteShipmentRequest;
import com.example.epl.outbound.v1.GetShipmentRequest;
import com.example.epl.outbound.v1.SearchShipmentsRequest;
import com.example.epl.outbound.v1.Shipment;
import com.example.epl.outbound.v1.ShipmentLine;
import com.example.epl.outbound.v1.ShipmentServiceGrpc;
import com.example.epl.outbound.v1.UpdateShipmentRequest;
import com.example.service.core.test.EplServiceTestConfiguration;
import com.example.service.core.test.EplTestDatabase;
import com.example.service.core.test.GrpcTestServer;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
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
class ShipmentCrudTests {

    @DynamicPropertySource
    static void database(DynamicPropertyRegistry registry) {
        EplTestDatabase.register(registry, "outb");
    }

    @Autowired
    private GrpcTestServer grpc;

    @Test
    void shipmentLifecycle() {
        var shipments = ShipmentServiceGrpc.newBlockingStub(grpc.tenantChannel());
        var shipTo = Party.newBuilder().setCode("DEST1").addNames("Store 1")
                .setAddress(Address.newBuilder().addLines("1-2-3 Ginza").setCity("Tokyo").setCountryCode("JP"));
        var created = shipments.createShipment(CreateShipmentRequest.newBuilder().setShipment(Shipment.newBuilder()
                .setScheduledDate(Date.newBuilder().setYear(2026).setMonth(11).setDay(5)).setTransactionKind("020")
                .setShipTo(shipTo).setTotalWeight(Decimal.newBuilder().setValue("10.5"))
                .addLines(line(0, "P001", "6"))).build());
        assertThat(created.getShipmentNo()).isEqualTo("SH00000001");
        assertThat(created.getShipTo().getAddress().getCity()).isEqualTo("Tokyo");
        assertThat(created.getStatus()).isEqualTo("ORDERED");

        var updated = shipments.updateShipment(UpdateShipmentRequest.newBuilder()
                .setShipment(created.toBuilder().addLines(line(2, "P002", "3"))).build());
        assertThat(updated.getLinesList()).extracting("productCode").containsExactly("P001", "P002");

        assertThat(shipments.searchShipments(SearchShipmentsRequest.newBuilder().setShipToCode("DEST1").build()).getShipmentsList())
                .extracting("shipmentNo").containsExactly("SH00000001");

        var other = ShipmentServiceGrpc.newBlockingStub(grpc.channel("C001", "WH01", "CUST02"));
        assertThat(other.searchShipments(SearchShipmentsRequest.getDefaultInstance()).getShipmentsList()).isEmpty();

        var cancelled = shipments.cancelShipment(CancelShipmentRequest.newBuilder().setShipmentNo(created.getShipmentNo())
                .setReasonCode("01").setVersion(updated.getAudit().getVersion()).build());
        assertThat(cancelled.getLinesList()).extracting("status").containsOnly("CANCELLED");
    }

    @Test
    void deleteOrderedShipment() {
        var shipments = ShipmentServiceGrpc.newBlockingStub(grpc.tenantChannel());
        var created = shipments.createShipment(CreateShipmentRequest.newBuilder().setShipment(Shipment.newBuilder()
                .setShipmentNo("MANUAL-1").setScheduledDate(Date.newBuilder().setYear(2026).setMonth(11).setDay(5))
                .setTransactionKind("020").addLines(line(1, "P001", "1"))).build());
        assertThatThrownBy(() -> shipments.deleteShipment(DeleteShipmentRequest.newBuilder()
                .setShipmentNo("MANUAL-1").setVersion(created.getAudit().getVersion() + 1).build()))
                .satisfies(e -> assertThat(code(e)).isEqualTo(Status.Code.ABORTED));
        shipments.deleteShipment(DeleteShipmentRequest.newBuilder().setShipmentNo("MANUAL-1").setVersion(created.getAudit().getVersion()).build());
        assertThatThrownBy(() -> shipments.getShipment(GetShipmentRequest.newBuilder().setShipmentNo("MANUAL-1").build()))
                .satisfies(e -> assertThat(code(e)).isEqualTo(Status.Code.NOT_FOUND));
    }

    private static ShipmentLine line(int lineNo, String product, String totalPieces) {
        return ShipmentLine.newBuilder().setLineNo(lineNo).setProductCode(product)
                .setOrdered(Quantity.newBuilder().setTotalPieces(Decimal.newBuilder().setValue(totalPieces)))
                .build();
    }

    private static Status.Code code(Throwable e) {
        return ((StatusRuntimeException) e).getStatus().getCode();
    }
}

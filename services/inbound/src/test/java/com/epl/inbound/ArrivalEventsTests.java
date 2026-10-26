package com.epl.inbound;

import com.epl.common.v1.Date;
import com.epl.common.v1.Decimal;
import com.epl.common.v1.Quantity;
import com.epl.inbound.v1.Arrival;
import com.epl.inbound.v1.ArrivalLine;
import com.epl.inbound.v1.ArrivalServiceGrpc;
import com.epl.inbound.v1.CancelArrivalRequest;
import com.epl.inbound.v1.CreateArrivalRequest;
import com.epl.messaging.Event;
import com.epl.messaging.EventHeaders;
import com.epl.messaging.Inbox;
import com.epl.messaging.test.EplTestKafka;
import com.epl.service.core.RequestContext;
import com.epl.service.core.test.EplServiceTestConfiguration;
import com.epl.service.core.test.EplTestDatabase;
import com.epl.service.core.test.GrpcTestServer;
import com.epl.service.core.test.TestTenantResolver;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Testcontainers(disabledWithoutDocker = true)
@Import(EplServiceTestConfiguration.class)
class ArrivalEventsTests {

    @DynamicPropertySource
    static void infrastructure(DynamicPropertyRegistry registry) {
        EplTestDatabase.register(registry, "inb");
        EplTestKafka.register(registry);
    }

    @Autowired
    private GrpcTestServer grpc;

    @Autowired
    private Inbox inbox;

    @Test
    void cancelledArrivalIsPublished() {
        var arrivals = ArrivalServiceGrpc.newBlockingStub(grpc.tenantChannel());
        var created = arrivals.createArrival(CreateArrivalRequest.newBuilder().setArrival(Arrival.newBuilder()
                .setScheduledDate(Date.newBuilder().setYear(2026).setMonth(11).setDay(2)).setTransactionKind("010")
                .addLines(ArrivalLine.newBuilder().setLineNo(1).setProductCode("P001")
                        .setPlanned(Quantity.newBuilder().setTotalPieces(Decimal.newBuilder().setValue("5"))))).build());
        arrivals.cancelArrival(CancelArrivalRequest.newBuilder().setArrivalNo(created.getArrivalNo())
                .setReasonCode("01").setVersion(created.getAudit().getVersion()).build());

        var record = receive("inbound.arrival.cancelled", created.getArrivalNo());
        var event = Event.from(record);
        assertThat(event.type()).isEqualTo("inbound.arrival.cancelled");
        assertThat(event.aggregateType()).isEqualTo("arrival");
        assertThat(event.tenantId()).isEqualTo(tenantId());
        assertThat(event.occurredAt()).isNotNull();
        Arrival payload = event.payload(Arrival.newBuilder());
        assertThat(payload.getStatus()).isEqualTo("CANCELLED");
    }

    @Test
    void eventIsProcessedOnceWithItsTenant() {
        var record = record(UUID.randomUUID());
        var calls = new AtomicInteger();
        var tenants = new ArrayList<UUID>();
        assertThat(inbox.process("inbound.test", record, event -> {
            calls.incrementAndGet();
            tenants.add(RequestContext.current().tenantId());
        })).isTrue();
        assertThat(inbox.process("inbound.test", record, event -> calls.incrementAndGet())).isFalse();

        assertThat(calls).hasValue(1);
        assertThat(tenants).containsExactly(tenantId());
    }

    @Test
    void failedHandlerIsRetried() {
        var record = record(UUID.randomUUID());
        assertThatThrownBy(() -> inbox.process("inbound.test", record, event -> {
            throw new IllegalStateException("boom");
        })).hasMessage("boom");
        // the failure rolled back processed_event, so the redelivery runs the handler
        var calls = new AtomicInteger();
        assertThat(inbox.process("inbound.test", record, event -> calls.incrementAndGet())).isTrue();
        assertThat(calls).hasValue(1);
    }

    private static ConsumerRecord<String, String> record(UUID eventId) {
        var record = new ConsumerRecord<>("outbound.shipment.returned", 0, 0L, "S1", "{\"shipmentNo\":\"S1\"}");
        record.headers().add(EventHeaders.EVENT_ID, eventId.toString().getBytes(StandardCharsets.UTF_8));
        record.headers().add(EventHeaders.TENANT_ID, tenantId().toString().getBytes(StandardCharsets.UTF_8));
        return record;
    }

    private static UUID tenantId() {
        return UUID.fromString(new TestTenantResolver()
                .resolve(EplTestDatabase.COMPANY, EplTestDatabase.WAREHOUSE, EplTestDatabase.CUSTOMER).getTenantId());
    }

    private static ConsumerRecord<String, String> receive(String topic, String key) {
        var config = Map.<String, Object>of(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, EplTestKafka.kafka().getBootstrapServers(),
                ConsumerConfig.GROUP_ID_CONFIG, "test-" + UUID.randomUUID(),
                ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest",
                ConsumerConfig.METADATA_MAX_AGE_CONFIG, "500");
        try (var consumer = new KafkaConsumer<>(config, new StringDeserializer(), new StringDeserializer())) {
            consumer.subscribe(List.of(topic));
            var deadline = System.nanoTime() + Duration.ofSeconds(30).toNanos();
            while (System.nanoTime() < deadline) {
                for (var record : consumer.poll(Duration.ofMillis(500))) {
                    if (key.equals(record.key())) {
                        return record;
                    }
                }
            }
        }
        throw new AssertionError("No " + topic + " event for " + key + " within 30s");
    }
}

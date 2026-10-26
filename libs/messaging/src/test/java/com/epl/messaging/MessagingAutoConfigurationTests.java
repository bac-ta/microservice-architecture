package com.epl.messaging;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.kafka.listener.CommonErrorHandler;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

class MessagingAutoConfigurationTests {

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(KafkaAutoConfiguration.class, MessagingAutoConfiguration.class))
            .withBean(DataSource.class, () -> mock(DataSource.class))
            .withBean(PlatformTransactionManager.class, () -> mock(PlatformTransactionManager.class))
            // the relay starts with the context; keep it from polling the mock DataSource
            .withPropertyValues("epl.messaging.relay.poll-interval=1h");

    @Test
    void relayIsOnByDefault() {
        runner.run(context -> {
            assertThat(context).hasNotFailed();
            assertThat(context).hasSingleBean(Outbox.class).hasSingleBean(Inbox.class).hasSingleBean(OutboxRelay.class);
            assertThat(context).hasSingleBean(CommonErrorHandler.class);
        });
    }

    @Test
    void relayCanBeTurnedOff() {
        runner.withPropertyValues("epl.messaging.relay.enabled=false").run(context -> {
            assertThat(context).hasNotFailed();
            assertThat(context).hasSingleBean(Outbox.class).doesNotHaveBean(OutboxRelay.class);
        });
    }

    @Test
    void eventFromRecord() {
        var id = UUID.randomUUID();
        var tenant = UUID.randomUUID();
        var record = new ConsumerRecord<>("stock.adjusted", 0, 0L, "ST-1", "{\"qty\":5}");
        header(record, EventHeaders.EVENT_ID, id.toString());
        header(record, EventHeaders.TENANT_ID, tenant.toString());
        header(record, EventHeaders.AGGREGATE_TYPE, "stock");
        header(record, EventHeaders.OCCURRED_AT, "2026-10-26T01:02:03Z");

        var event = Event.from(record);
        assertThat(event.id()).isEqualTo(id);
        assertThat(event.type()).isEqualTo("stock.adjusted");
        assertThat(event.aggregateId()).isEqualTo("ST-1");
        assertThat(event.tenantId()).isEqualTo(tenant);
        assertThat(event.payload(Map.class)).containsEntry("qty", 5);
    }

    @Test
    void recordWithoutEventIdIsRejected() {
        var record = new ConsumerRecord<>("stock.adjusted", 0, 0L, "ST-1", "{}");
        assertThatThrownBy(() -> Event.from(record)).isInstanceOf(IllegalArgumentException.class);
    }

    private static void header(ConsumerRecord<String, String> record, String name, String value) {
        record.headers().add(name, value.getBytes(StandardCharsets.UTF_8));
    }
}

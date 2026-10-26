package com.epl.messaging;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.time.Duration;

@ConfigurationProperties("epl.messaging")
public record MessagingProperties(@DefaultValue Outbox outbox, @DefaultValue Relay relay, @DefaultValue Consumer consumer) {

    /**
     * @param tenantColumn false when the schema's outbox_event has no tenant_id (iam)
     */
    public record Outbox(@DefaultValue("true") boolean tenantColumn) {
    }

    /**
     * Publishes outbox_event rows to Kafka. Off for services without an outbox (report), in the
     * migrate job and in tests that do not start Kafka.
     *
     * @param enabled      EPL_OUTBOX_RELAY_ENABLED
     * @param pollInterval pause between two polls when the outbox was empty
     * @param batchSize    rows sent per poll
     * @param sendTimeout  wait for the broker's acknowledgement of a batch
     * @param retention    published rows older than this are deleted
     */
    public record Relay(@DefaultValue("true") boolean enabled,
                        @DefaultValue("500ms") Duration pollInterval,
                        @DefaultValue("100") int batchSize,
                        @DefaultValue("10s") Duration sendTimeout,
                        @DefaultValue("7d") Duration retention) {
    }

    /**
     * @param retries  redeliveries of a failing record before it goes to {@code <topic>.DLT}
     * @param backOff  pause between redeliveries
     */
    public record Consumer(@DefaultValue("3") int retries, @DefaultValue("1s") Duration backOff) {
    }
}

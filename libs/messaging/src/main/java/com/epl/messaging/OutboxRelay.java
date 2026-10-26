package com.epl.messaging;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.SmartLifecycle;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.transaction.support.TransactionTemplate;

import java.nio.charset.StandardCharsets;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Publishes unpublished outbox_event rows to Kafka and marks them published (at-least-once:
 * consumers de-duplicate with processed_event). Rows are locked with FOR UPDATE SKIP LOCKED, so
 * several replicas share the outbox without sending a row twice in normal operation.
 */
public class OutboxRelay implements SmartLifecycle {

    private static final Logger logger = LoggerFactory.getLogger(OutboxRelay.class);

    private static final long CLEANUP_EVERY_MILLIS = TimeUnit.HOURS.toMillis(1);

    private final JdbcTemplate jdbc;
    private final TransactionTemplate transactions;
    private final KafkaTemplate<String, String> kafka;
    private final MessagingProperties.Relay properties;
    private final boolean tenantColumn;

    private ScheduledExecutorService executor;
    private volatile long lastCleanup;

    public OutboxRelay(JdbcTemplate jdbc, TransactionTemplate transactions, KafkaTemplate<String, String> kafka,
                       MessagingProperties properties) {
        this.jdbc = jdbc;
        this.transactions = transactions;
        this.kafka = kafka;
        this.properties = properties.relay();
        this.tenantColumn = properties.outbox().tenantColumn();
    }

    /** Sends one batch; returns how many events were published. */
    public int publishBatch() {
        var published = transactions.execute(status -> {
            var events = jdbc.query("SELECT id, aggregate_type, aggregate_id, event_type, "
                    + (tenantColumn ? "tenant_id" : "NULL::uuid AS tenant_id")
                    + ", payload::text AS payload, created_at FROM outbox_event WHERE published_at IS NULL"
                    + " ORDER BY created_at LIMIT ? FOR UPDATE SKIP LOCKED", OutboxRelay::row, properties.batchSize());
            if (events.isEmpty()) {
                return 0;
            }
            var sends = events.stream().map(event -> kafka.send(record(event))).toArray(CompletableFuture[]::new);
            try {
                CompletableFuture.allOf(sends).get(properties.sendTimeout().toMillis(), TimeUnit.MILLISECONDS);
            } catch (Exception e) {
                // nothing is marked: the whole batch is sent again on the next poll
                throw new IllegalStateException("Kafka did not acknowledge " + events.size() + " outbox events", e);
            }
            jdbc.update("UPDATE outbox_event SET published_at = now() WHERE id = ANY (?)",
                    ps -> ps.setArray(1, ps.getConnection().createArrayOf("uuid", events.stream().map(OutboxRow::id).toArray())));
            return events.size();
        });
        return published == null ? 0 : published;
    }

    /** Deletes published rows older than the retention. */
    public int cleanup() {
        return jdbc.update("DELETE FROM outbox_event WHERE published_at < ?",
                Timestamp.from(Instant.now().minus(properties.retention())));
    }

    private void poll() {
        try {
            // keep draining while there is a backlog, then wait for the next poll
            while (publishBatch() == properties.batchSize()) {
                if (Thread.currentThread().isInterrupted()) {
                    return;
                }
            }
            if (System.currentTimeMillis() - lastCleanup > CLEANUP_EVERY_MILLIS) {
                lastCleanup = System.currentTimeMillis();
                var deleted = cleanup();
                if (deleted > 0) {
                    logger.info("Deleted {} published outbox events", deleted);
                }
            }
        } catch (RuntimeException e) {
            logger.warn("Outbox relay failed, retrying in {}: {}", properties.pollInterval(), e.getMessage());
        }
    }

    private static ProducerRecord<String, String> record(OutboxRow event) {
        var record = new ProducerRecord<String, String>(event.eventType(), event.aggregateId(), event.payload());
        header(record, EventHeaders.EVENT_ID, event.id().toString());
        header(record, EventHeaders.EVENT_TYPE, event.eventType());
        header(record, EventHeaders.AGGREGATE_TYPE, event.aggregateType());
        if (event.tenantId() != null) {
            header(record, EventHeaders.TENANT_ID, event.tenantId().toString());
        }
        header(record, EventHeaders.OCCURRED_AT, event.createdAt().toString());
        return record;
    }

    private static void header(ProducerRecord<String, String> record, String name, String value) {
        record.headers().add(name, value.getBytes(StandardCharsets.UTF_8));
    }

    private static OutboxRow row(ResultSet rs, int rowNum) throws SQLException {
        return new OutboxRow(rs.getObject("id", UUID.class), rs.getString("aggregate_type"), rs.getString("aggregate_id"),
                rs.getString("event_type"), rs.getObject("tenant_id", UUID.class), rs.getString("payload"),
                rs.getTimestamp("created_at").toInstant());
    }

    @Override
    public synchronized void start() {
        if (executor != null) {
            return;
        }
        executor = Executors.newSingleThreadScheduledExecutor(runnable -> {
            var thread = new Thread(runnable, "outbox-relay");
            thread.setDaemon(true);
            return thread;
        });
        var interval = properties.pollInterval().toMillis();
        executor.scheduleWithFixedDelay(this::poll, interval, interval, TimeUnit.MILLISECONDS);
        logger.info("Outbox relay started (every {}, batch {})", properties.pollInterval(), properties.batchSize());
    }

    @Override
    public synchronized void stop() {
        if (executor == null) {
            return;
        }
        executor.shutdownNow();
        executor = null;
    }

    @Override
    public synchronized boolean isRunning() {
        return executor != null;
    }

    private record OutboxRow(UUID id, String aggregateType, String aggregateId, String eventType,
                             UUID tenantId, String payload, Instant createdAt) {
    }
}

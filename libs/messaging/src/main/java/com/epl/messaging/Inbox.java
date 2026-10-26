package com.epl.messaging;

import com.epl.service.core.RequestContext;
import io.grpc.Context;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.function.Consumer;

/**
 * Runs a Kafka event handler once per event: the event id goes into processed_event in the same
 * transaction as the handler's changes, so a redelivered event is skipped. The transaction runs
 * with the event's tenant as request context, so Row-Level Security and created_by apply as in a
 * gRPC call. processed_event is keyed by event id alone: one handler per event type and service.
 * <pre>{@code
 * @KafkaListener(topics = "outbound.shipment.confirmed")
 * void onShipmentConfirmed(ConsumerRecord<String, String> record) {
 *     inbox.process("inventory.shipment-issue", record, event -> stockService.issue(event.payload(Shipment.newBuilder())));
 * }
 * }</pre>
 */
public class Inbox {

    private static final Logger logger = LoggerFactory.getLogger(Inbox.class);

    private final JdbcTemplate jdbc;
    private final TransactionTemplate transactions;

    public Inbox(JdbcTemplate jdbc, TransactionTemplate transactions) {
        this.jdbc = jdbc;
        this.transactions = transactions;
    }

    /**
     * @param consumer name stored in processed_event.consumer, e.g. {@code inventory.shipment-issue}
     * @return false when the event had already been processed
     */
    public boolean process(String consumer, ConsumerRecord<String, String> record, Consumer<Event> handler) {
        var event = Event.from(record);
        var context = new RequestContext("event:" + consumer, null, null, null, null, null, null, event.tenantId());
        Boolean processed;
        var grpcContext = context.attach(Context.current());
        var previous = grpcContext.attach();
        try {
            processed = transactions.execute(status -> {
                var inserted = jdbc.update("INSERT INTO processed_event (event_id, consumer) VALUES (?, ?) ON CONFLICT DO NOTHING",
                        event.id(), consumer);
                if (inserted == 0) {
                    return false;
                }
                handler.accept(event);
                return true;
            });
        } finally {
            grpcContext.detach(previous);
        }
        if (!Boolean.TRUE.equals(processed)) {
            logger.debug("Event {} ({}) already processed, skipped", event.id(), event.type());
            return false;
        }
        return true;
    }
}

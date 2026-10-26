package com.epl.messaging;

import com.epl.service.core.RequestContext;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import com.google.protobuf.util.JsonFormat;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Writes an event to the schema's outbox_event in the caller's transaction, so the event exists
 * exactly when the business change commits. {@link OutboxRelay} then publishes it to Kafka
 * (topic = event type, key = aggregate id).
 * <pre>{@code
 * outbox.append("inbound.arrival.cancelled", "arrival", entity.getArrivalNo(), arrival);
 * }</pre>
 */
public class Outbox {

    private final JdbcTemplate jdbc;
    private final ObjectMapper json;
    private final boolean tenantColumn;

    public Outbox(JdbcTemplate jdbc, ObjectMapper json, MessagingProperties properties) {
        this.jdbc = jdbc;
        this.json = json;
        this.tenantColumn = properties.outbox().tenantColumn();
    }

    /**
     * @param eventType     {@code <domain>.<entity>.<past-tense verb>} (docs/naming-conventions.md)
     * @param aggregateType e.g. arrival, stock, user
     * @param aggregateId   business key; events of one aggregate keep their order (same partition)
     * @param payload       protobuf message (proto3 JSON) or any Jackson-serialisable object
     * @return the event id
     */
    @Transactional(propagation = Propagation.MANDATORY)
    public UUID append(String eventType, String aggregateType, String aggregateId, Object payload) {
        var text = toJson(payload);
        if (tenantColumn) {
            return jdbc.queryForObject("""
                    INSERT INTO outbox_event (aggregate_type, aggregate_id, event_type, tenant_id, payload)
                    VALUES (?, ?, ?, ?, ?::jsonb) RETURNING id
                    """, UUID.class, aggregateType, aggregateId, eventType, RequestContext.current().tenantId(), text);
        }
        return jdbc.queryForObject("""
                INSERT INTO outbox_event (aggregate_type, aggregate_id, event_type, payload)
                VALUES (?, ?, ?, ?::jsonb) RETURNING id
                """, UUID.class, aggregateType, aggregateId, eventType, text);
    }

    private String toJson(Object payload) {
        try {
            if (payload instanceof Message message) {
                return JsonFormat.printer().omittingInsignificantWhitespace().print(message);
            }
            return json.writeValueAsString(payload);
        } catch (InvalidProtocolBufferException | JsonProcessingException e) {
            throw new IllegalArgumentException("Event payload is not serialisable: " + payload.getClass().getName(), e);
        }
    }
}

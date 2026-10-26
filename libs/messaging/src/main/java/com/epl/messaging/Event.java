package com.epl.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import com.google.protobuf.util.JsonFormat;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Headers;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.UUID;

/** An event received from Kafka: the {@link EventHeaders} plus the JSON payload. */
public record Event(UUID id, String type, String aggregateType, String aggregateId,
                    UUID tenantId, Instant occurredAt, String payload) {

    private static final ObjectMapper JSON = new ObjectMapper().findAndRegisterModules();

    public static Event from(ConsumerRecord<String, String> record) {
        var headers = record.headers();
        var id = header(headers, EventHeaders.EVENT_ID);
        if (id == null) {
            throw new IllegalArgumentException("Record " + record.topic() + "@" + record.offset() + " has no " + EventHeaders.EVENT_ID);
        }
        var tenantId = header(headers, EventHeaders.TENANT_ID);
        var occurredAt = header(headers, EventHeaders.OCCURRED_AT);
        return new Event(UUID.fromString(id),
                headerOr(headers, EventHeaders.EVENT_TYPE, record.topic()),
                header(headers, EventHeaders.AGGREGATE_TYPE),
                record.key(),
                tenantId == null ? null : UUID.fromString(tenantId),
                occurredAt == null ? null : Instant.parse(occurredAt),
                record.value());
    }

    /** Payload as a Java type (record, Map …). */
    public <T> T payload(Class<T> type) {
        try {
            return JSON.readValue(payload, type);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Event " + id + " (" + type + ") has an unreadable payload", e);
        }
    }

    /** Payload as a protobuf message, for events written with a message ({@link Outbox#append}). */
    @SuppressWarnings("unchecked")
    public <T extends Message> T payload(Message.Builder builder) {
        try {
            JsonFormat.parser().ignoringUnknownFields().merge(payload, builder);
            return (T) builder.build();
        } catch (InvalidProtocolBufferException e) {
            throw new IllegalArgumentException("Event " + id + " (" + type + ") has an unreadable payload", e);
        }
    }

    private static String header(Headers headers, String name) {
        var header = headers.lastHeader(name);
        return header == null ? null : new String(header.value(), StandardCharsets.UTF_8);
    }

    private static String headerOr(Headers headers, String name, String fallback) {
        var value = header(headers, name);
        return value == null ? fallback : value;
    }
}

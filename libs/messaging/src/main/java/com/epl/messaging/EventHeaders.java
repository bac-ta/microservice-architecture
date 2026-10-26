package com.epl.messaging;

/** Kafka record headers set by {@link OutboxRelay}; the record key is the aggregate id, the value the JSON payload. */
public final class EventHeaders {

    /** outbox_event.id, the idempotency key of consumers (processed_event.event_id) */
    public static final String EVENT_ID = "epl-event-id";
    /** e.g. inbound.arrival.cancelled; also the topic name */
    public static final String EVENT_TYPE = "epl-event-type";
    public static final String AGGREGATE_TYPE = "epl-aggregate-type";
    /** absent for events that belong to no tenant (iam, global master data) */
    public static final String TENANT_ID = "epl-tenant-id";
    /** ISO-8601 instant the event was written to the outbox */
    public static final String OCCURRED_AT = "epl-occurred-at";

    private EventHeaders() {
    }
}

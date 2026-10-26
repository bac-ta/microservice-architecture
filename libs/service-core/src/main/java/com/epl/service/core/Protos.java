package com.epl.service.core;

import com.epl.database.AuditedEntity;
import com.epl.common.v1.Audit;
import com.epl.common.v1.Date;
import com.epl.common.v1.Decimal;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import com.google.protobuf.MessageOrBuilder;
import com.google.protobuf.Struct;
import com.google.protobuf.Timestamp;
import com.google.protobuf.util.JsonFormat;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Conversions between proto messages and entity fields. Proto3 has no null: an empty string,
 * an unset message or 0 means "no value", and becomes null in the entity.
 */
public final class Protos {

    private static final ObjectMapper JSON = new ObjectMapper();

    private Protos() {
    }

    // strings -----------------------------------------------------------------

    public static String nullIfEmpty(String value) {
        return value == null || value.isBlank() ? null : value;
    }

    public static String emptyIfNull(String value) {
        return value == null ? "" : value;
    }

    public static String text(UUID id) {
        return id == null ? "" : id.toString();
    }

    public static UUID uuid(String value, String field) {
        try {
            return UUID.fromString(Errors.required(value, field));
        } catch (IllegalArgumentException e) {
            throw Errors.invalid(field + " is not a valid id");
        }
    }

    public static Integer nullIfZero(int value) {
        return value == 0 ? null : value;
    }

    public static int zeroIfNull(Integer value) {
        return value == null ? 0 : value;
    }

    // dates and times ---------------------------------------------------------

    public static Timestamp timestamp(Instant instant) {
        return instant == null ? Timestamp.getDefaultInstance()
                : Timestamp.newBuilder().setSeconds(instant.getEpochSecond()).setNanos(instant.getNano()).build();
    }

    public static Date date(LocalDate date) {
        return date == null ? Date.getDefaultInstance()
                : Date.newBuilder().setYear(date.getYear()).setMonth(date.getMonthValue()).setDay(date.getDayOfMonth()).build();
    }

    /** Unset / all-zero Date = null. */
    public static LocalDate localDate(Date date) {
        if (date == null || date.getYear() == 0) {
            return null;
        }
        try {
            return LocalDate.of(date.getYear(), date.getMonth(), date.getDay());
        } catch (RuntimeException e) {
            throw Errors.invalid("Invalid date " + date.getYear() + "-" + date.getMonth() + "-" + date.getDay());
        }
    }

    // numbers -----------------------------------------------------------------

    public static Decimal decimal(BigDecimal value) {
        return value == null ? Decimal.getDefaultInstance() : Decimal.newBuilder().setValue(value.toPlainString()).build();
    }

    /** Unset / empty Decimal = null. */
    public static BigDecimal bigDecimal(Decimal value) {
        if (value == null || value.getValue().isBlank()) {
            return null;
        }
        try {
            return new BigDecimal(value.getValue().trim());
        } catch (NumberFormatException e) {
            throw Errors.invalid("Invalid decimal '" + value.getValue() + "'");
        }
    }

    public static BigDecimal bigDecimalOrZero(Decimal value) {
        var result = bigDecimal(value);
        return result == null ? BigDecimal.ZERO : result;
    }

    // json --------------------------------------------------------------------

    /** jsonb column (Map) to google.protobuf.Struct. */
    public static Struct struct(Map<String, Object> value) {
        var builder = Struct.newBuilder();
        if (value == null || value.isEmpty()) {
            return builder.build();
        }
        try {
            JsonFormat.parser().merge(JSON.writeValueAsString(value), builder);
            return builder.build();
        } catch (JsonProcessingException | InvalidProtocolBufferException e) {
            throw new IllegalStateException("Cannot convert json to Struct", e);
        }
    }

    /** google.protobuf.Struct to jsonb column (Map), never null. */
    public static Map<String, Object> map(Struct struct) {
        if (struct == null || struct.getFieldsCount() == 0) {
            return new LinkedHashMap<>();
        }
        try {
            return JSON.readValue(JsonFormat.printer().print(struct), new TypeReference<LinkedHashMap<String, Object>>() {
            });
        } catch (InvalidProtocolBufferException | JsonProcessingException e) {
            throw Errors.invalid("Invalid json object");
        }
    }

    /** Any message (e.g. a Party snapshot) to a jsonb column, in proto3 JSON (camelCase keys); never null. */
    public static Map<String, Object> map(MessageOrBuilder message) {
        if (message instanceof Struct struct) {
            return map(struct);
        }
        try {
            return JSON.readValue(JsonFormat.printer().print(message), new TypeReference<LinkedHashMap<String, Object>>() {
            });
        } catch (InvalidProtocolBufferException | JsonProcessingException e) {
            throw new IllegalStateException("Cannot convert " + message.getClass().getSimpleName() + " to json", e);
        }
    }

    /** jsonb column written by {@link #map(MessageOrBuilder)} back into a message builder; unknown keys are ignored. */
    public static <B extends Message.Builder> B merge(Map<String, Object> value, B builder) {
        if (value == null || value.isEmpty()) {
            return builder;
        }
        try {
            JsonFormat.parser().ignoringUnknownFields().merge(JSON.writeValueAsString(value), builder);
            return builder;
        } catch (JsonProcessingException | InvalidProtocolBufferException e) {
            throw new IllegalStateException("Cannot convert json to " + builder.getDescriptorForType().getName(), e);
        }
    }

    // audit -------------------------------------------------------------------

    public static Audit audit(AuditedEntity entity) {
        return Audit.newBuilder()
                .setCreatedAt(timestamp(entity.getCreatedAt()))
                .setCreatedBy(emptyIfNull(entity.getCreatedBy()))
                .setUpdatedAt(timestamp(entity.getUpdatedAt()))
                .setUpdatedBy(emptyIfNull(entity.getUpdatedBy()))
                .setVersion(entity.getVersion())
                .build();
    }
}

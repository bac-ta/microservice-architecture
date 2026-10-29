package com.epl.gateway.rpc;

import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.EnumDescriptor;
import com.google.protobuf.Descriptors.EnumValueDescriptor;
import com.google.protobuf.Descriptors.FieldDescriptor;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.BooleanSchema;
import io.swagger.v3.oas.models.media.IntegerSchema;
import io.swagger.v3.oas.models.media.MapSchema;
import io.swagger.v3.oas.models.media.NumberSchema;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;

import java.util.Map;

/**
 * OpenAPI schemas of protobuf messages as the gateway reads and writes them: proto3 JSON
 * (lowerCamelCase names, enums as names, 64-bit integers as strings, well-known types as their JSON form).
 * Messages and enums go to {@code schemas} under their full proto name and are referenced from there.
 */
public class ProtoSchemas {

    private static final String REF_PREFIX = "#/components/schemas/";

    @SuppressWarnings("rawtypes")
    private final Map<String, Schema> schemas;

    @SuppressWarnings("rawtypes")
    public ProtoSchemas(Map<String, Schema> schemas) {
        this.schemas = schemas;
    }

    /** Reference to the schema of {@code message}, defining it (and every type it uses) first. */
    public Schema<?> ref(Descriptor message) {
        var name = message.getFullName();
        if (!schemas.containsKey(name)) {
            var wellKnown = wellKnown(name);
            if (wellKnown != null) {
                schemas.put(name, wellKnown);
            } else {
                var schema = new ObjectSchema().description(name);
                // registered before its fields: recursive messages refer to themselves
                schemas.put(name, schema);
                for (var field : message.getFields()) {
                    schema.addProperty(field.getJsonName(), field(field));
                }
            }
        }
        return new Schema<>().$ref(REF_PREFIX + name);
    }

    private Schema<?> ref(EnumDescriptor type) {
        var name = type.getFullName();
        if (!schemas.containsKey(name)) {
            var schema = new StringSchema();
            schema.description(name);
            type.getValues().stream().map(EnumValueDescriptor::getName).forEach(schema::addEnumItem);
            schemas.put(name, schema);
        }
        return new Schema<>().$ref(REF_PREFIX + name);
    }

    private Schema<?> field(FieldDescriptor field) {
        if (field.isMapField()) {
            return new MapSchema().additionalProperties(single(field.getMessageType().findFieldByName("value")));
        }
        if (field.isRepeated()) {
            return new ArraySchema().items(single(field));
        }
        return single(field);
    }

    private Schema<?> single(FieldDescriptor field) {
        return switch (field.getJavaType()) {
            case INT -> new IntegerSchema();
            case LONG -> new StringSchema().format("int64");
            case FLOAT -> new NumberSchema().format("float");
            case DOUBLE -> new NumberSchema().format("double");
            case BOOLEAN -> new BooleanSchema();
            case STRING -> new StringSchema();
            case BYTE_STRING -> new StringSchema().format("byte");
            case ENUM -> ref(field.getEnumType());
            case MESSAGE -> ref(field.getMessageType());
        };
    }

    /** JSON form of the google.protobuf well-known types, null for any other message. */
    private static Schema<?> wellKnown(String fullName) {
        return switch (fullName) {
            case "google.protobuf.Timestamp" -> new StringSchema().format("date-time").example("2026-01-31T09:30:00Z");
            case "google.protobuf.Duration" -> new StringSchema().example("1.5s");
            case "google.protobuf.FieldMask" -> new StringSchema().example("name,status");
            case "google.protobuf.Empty", "google.protobuf.Struct", "google.protobuf.Any" -> new ObjectSchema();
            case "google.protobuf.Value" -> new Schema<>();
            case "google.protobuf.ListValue" -> new ArraySchema().items(new Schema<>());
            case "google.protobuf.StringValue" -> new StringSchema();
            case "google.protobuf.BytesValue" -> new StringSchema().format("byte");
            case "google.protobuf.BoolValue" -> new BooleanSchema();
            case "google.protobuf.Int32Value", "google.protobuf.UInt32Value" -> new IntegerSchema();
            case "google.protobuf.Int64Value", "google.protobuf.UInt64Value" -> new StringSchema().format("int64");
            case "google.protobuf.FloatValue", "google.protobuf.DoubleValue" -> new NumberSchema();
            default -> null;
        };
    }
}

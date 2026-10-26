package com.epl.gateway.rpc;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import com.google.protobuf.MessageOrBuilder;
import com.google.protobuf.util.JsonFormat;

/**
 * Proto3 JSON mapping (https://protobuf.dev/programming-guides/json/): lowerCamelCase field names on output,
 * both lowerCamelCase and proto field names accepted on input. Unknown fields are rejected.
 */
public final class ProtoJson {

    private static final JsonFormat.Parser PARSER = JsonFormat.parser();
    private static final JsonFormat.Printer PRINTER = JsonFormat.printer()
            .includingDefaultValueFields()
            .omittingInsignificantWhitespace();

    private ProtoJson() {
    }

    /**
     * @param json request body; null or blank = the default (empty) message
     * @throws InvalidProtocolBufferException when the body does not match the message
     */
    @SuppressWarnings("unchecked")
    public static <M extends Message> M parse(String json, M prototype) throws InvalidProtocolBufferException {
        if (json == null || json.isBlank()) {
            return prototype;
        }
        var builder = prototype.newBuilderForType();
        PARSER.merge(json, builder);
        return (M) builder.build();
    }

    public static String print(MessageOrBuilder message) {
        try {
            return PRINTER.print(message);
        } catch (InvalidProtocolBufferException e) {
            // only for Any fields of an unregistered type; no EPL contract uses Any
            throw new IllegalStateException("Cannot print " + message.getDescriptorForType().getFullName() + " as JSON", e);
        }
    }
}

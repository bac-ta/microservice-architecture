package com.epl.gateway.graphql;

import graphql.GraphQLContext;
import graphql.execution.CoercedVariables;
import graphql.language.ArrayValue;
import graphql.language.BooleanValue;
import graphql.language.EnumValue;
import graphql.language.FloatValue;
import graphql.language.IntValue;
import graphql.language.NullValue;
import graphql.language.ObjectValue;
import graphql.language.StringValue;
import graphql.language.Value;
import graphql.language.VariableReference;
import graphql.schema.Coercing;
import graphql.schema.CoercingParseLiteralException;
import graphql.schema.CoercingParseValueException;
import graphql.schema.CoercingSerializeException;
import graphql.schema.GraphQLScalarType;

import java.math.BigInteger;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.function.Function;
import java.util.regex.Pattern;

/**
 * Scalars for proto3 JSON values that have no built-in GraphQL type.
 */
final class ProtoScalars {

    private static final Pattern INTEGER = Pattern.compile("-?\\d+");

    /** int64, uint64, uint32 …: a string on output as in proto3 JSON (no precision loss in JavaScript); a string or an integer on input. */
    static final GraphQLScalarType LONG = GraphQLScalarType.newScalar()
            .name("Long")
            .description("64-bit / unsigned integer: a string on output, a string or an integer on input")
            .coercing(new Coercing<String, String>() {
                @Override
                public String serialize(Object value, GraphQLContext context, Locale locale) {
                    if (value instanceof Number || value instanceof String) {
                        return value.toString();
                    }
                    throw new CoercingSerializeException("Not an integer: " + value);
                }

                @Override
                public String parseValue(Object input, GraphQLContext context, Locale locale) {
                    if (input instanceof Integer || input instanceof Long || input instanceof BigInteger) {
                        return input.toString();
                    }
                    return integer(String.valueOf(input), CoercingParseValueException::new);
                }

                @Override
                public String parseLiteral(Value<?> input, CoercedVariables variables, GraphQLContext context, Locale locale) {
                    if (input instanceof IntValue value) {
                        return value.getValue().toString();
                    }
                    if (input instanceof StringValue value) {
                        return integer(value.getValue(), CoercingParseLiteralException::new);
                    }
                    throw new CoercingParseLiteralException("Expected an integer or a string, got " + input);
                }
            })
            .build();

    /** google.protobuf.Struct / Value / Empty, map fields, messages without fields: any JSON value. */
    static final GraphQLScalarType JSON = GraphQLScalarType.newScalar()
            .name("JSON")
            .description("Any JSON value (google.protobuf.Struct, map fields …)")
            .coercing(new Coercing<Object, Object>() {
                @Override
                public Object serialize(Object value, GraphQLContext context, Locale locale) {
                    return value;
                }

                @Override
                public Object parseValue(Object input, GraphQLContext context, Locale locale) {
                    return input;
                }

                @Override
                public Object parseLiteral(Value<?> input, CoercedVariables variables, GraphQLContext context, Locale locale) {
                    return literal(input, variables);
                }
            })
            .build();

    private ProtoScalars() {
    }

    private static String integer(String value, Function<String, RuntimeException> error) {
        if (!INTEGER.matcher(value).matches()) {
            throw error.apply("Not an integer: " + value);
        }
        return value;
    }

    private static Object literal(Value<?> value, CoercedVariables variables) {
        return switch (value) {
            case NullValue ignored -> null;
            case StringValue string -> string.getValue();
            case IntValue integer -> integer.getValue();
            case FloatValue decimal -> decimal.getValue();
            case BooleanValue bool -> bool.isValue();
            case EnumValue enumValue -> enumValue.getName();
            case VariableReference variable -> variables.get(variable.getName());
            case ArrayValue array -> array.getValues().stream().map(item -> literal(item, variables)).toList();
            case ObjectValue object -> {
                var map = new LinkedHashMap<String, Object>();
                object.getObjectFields().forEach(field -> map.put(field.getName(), literal(field.getValue(), variables)));
                yield map;
            }
            default -> throw new CoercingParseLiteralException("Unsupported JSON literal " + value);
        };
    }
}

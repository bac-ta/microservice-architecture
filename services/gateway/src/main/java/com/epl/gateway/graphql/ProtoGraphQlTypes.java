package com.epl.gateway.graphql;

import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.EnumDescriptor;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.Descriptors.GenericDescriptor;
import graphql.Scalars;
import graphql.schema.GraphQLEnumType;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLInputObjectField;
import graphql.schema.GraphQLInputObjectType;
import graphql.schema.GraphQLInputType;
import graphql.schema.GraphQLList;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLOutputType;
import graphql.schema.GraphQLScalarType;
import graphql.schema.GraphQLType;
import graphql.schema.GraphQLTypeReference;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * GraphQL types of proto messages and enums, in their proto3 JSON shape (lowerCamelCase field names,
 * 64-bit integers as {@code Long} strings, enums by name, Timestamp as an RFC 3339 string).
 * A message becomes an object type ({@code Product}) and an input type ({@code ProductInput}); the simple
 * name is used unless two packages share it, then the full name ({@code epl_common_v1_Page}).
 */
final class ProtoGraphQlTypes {

    private static final String INPUT_SUFFIX = "Input";
    private static final Map<String, GraphQLScalarType> WELL_KNOWN = Map.ofEntries(
            Map.entry("google.protobuf.Timestamp", Scalars.GraphQLString),
            Map.entry("google.protobuf.Duration", Scalars.GraphQLString),
            Map.entry("google.protobuf.FieldMask", Scalars.GraphQLString),
            Map.entry("google.protobuf.Struct", ProtoScalars.JSON),
            Map.entry("google.protobuf.Value", ProtoScalars.JSON),
            Map.entry("google.protobuf.ListValue", ProtoScalars.JSON),
            Map.entry("google.protobuf.Any", ProtoScalars.JSON),
            Map.entry("google.protobuf.Empty", ProtoScalars.JSON),
            Map.entry("google.protobuf.Int32Value", Scalars.GraphQLInt),
            Map.entry("google.protobuf.UInt32Value", ProtoScalars.LONG),
            Map.entry("google.protobuf.Int64Value", ProtoScalars.LONG),
            Map.entry("google.protobuf.UInt64Value", ProtoScalars.LONG),
            Map.entry("google.protobuf.FloatValue", Scalars.GraphQLFloat),
            Map.entry("google.protobuf.DoubleValue", Scalars.GraphQLFloat),
            Map.entry("google.protobuf.BoolValue", Scalars.GraphQLBoolean),
            Map.entry("google.protobuf.StringValue", Scalars.GraphQLString),
            Map.entry("google.protobuf.BytesValue", Scalars.GraphQLString));

    private final Map<String, String> names;
    private final Map<String, GraphQLType> types = new LinkedHashMap<>();

    /**
     * @param roots request and response messages of every exposed RPC
     */
    ProtoGraphQlTypes(Collection<Descriptor> roots) {
        var reachable = new LinkedHashSet<GenericDescriptor>();
        roots.forEach(root -> collect(root, reachable));
        this.names = names(reachable);
    }

    /** Type of an RPC response. */
    GraphQLOutputType output(Descriptor message) {
        return (GraphQLOutputType) message(message, false);
    }

    /** Type of an RPC request; null when the message has no fields (the RPC then takes no input). */
    GraphQLInputType input(Descriptor message) {
        return isOpaque(message) ? null : (GraphQLInputType) message(message, true);
    }

    /** Every object, input and enum type referenced so far. */
    Set<GraphQLType> additionalTypes() {
        var all = new LinkedHashSet<GraphQLType>(types.values());
        all.add(ProtoScalars.LONG);
        all.add(ProtoScalars.JSON);
        return all;
    }

    private GraphQLType message(Descriptor message, boolean input) {
        var wellKnown = WELL_KNOWN.get(message.getFullName());
        if (wellKnown != null) {
            return wellKnown;
        }
        if (message.getFields().isEmpty()) {
            return ProtoScalars.JSON;
        }
        var name = names.get(message.getFullName()) + (input ? INPUT_SUFFIX : "");
        if (!types.containsKey(name)) {
            // reserved before the fields are built: recursive messages refer to it by name
            types.put(name, null);
            types.put(name, input ? inputObject(name, message) : object(name, message));
        }
        return GraphQLTypeReference.typeRef(name);
    }

    private GraphQLObjectType object(String name, Descriptor message) {
        var type = GraphQLObjectType.newObject().name(name).description(message.getFullName());
        for (var field : message.getFields()) {
            type.field(GraphQLFieldDefinition.newFieldDefinition()
                    .name(field.getJsonName())
                    .type((GraphQLOutputType) field(field, false)));
        }
        return type.build();
    }

    private GraphQLInputObjectType inputObject(String name, Descriptor message) {
        var type = GraphQLInputObjectType.newInputObject().name(name).description(message.getFullName());
        for (var field : message.getFields()) {
            type.field(GraphQLInputObjectField.newInputObjectField()
                    .name(field.getJsonName())
                    .type((GraphQLInputType) field(field, true)));
        }
        return type.build();
    }

    private GraphQLType field(FieldDescriptor field, boolean input) {
        if (field.isMapField()) {
            return ProtoScalars.JSON;
        }
        var type = switch (field.getType()) {
            case INT32, SINT32, SFIXED32 -> Scalars.GraphQLInt;
            case UINT32, FIXED32, INT64, UINT64, SINT64, FIXED64, SFIXED64 -> ProtoScalars.LONG;
            case FLOAT, DOUBLE -> Scalars.GraphQLFloat;
            case BOOL -> Scalars.GraphQLBoolean;
            // bytes: base64
            case STRING, BYTES -> Scalars.GraphQLString;
            case ENUM -> enumeration(field.getEnumType());
            case MESSAGE, GROUP -> message(field.getMessageType(), input);
        };
        return field.isRepeated() ? GraphQLList.list(type) : type;
    }

    private GraphQLType enumeration(EnumDescriptor enumeration) {
        var name = names.get(enumeration.getFullName());
        types.computeIfAbsent(name, key -> {
            var type = GraphQLEnumType.newEnum().name(key).description(enumeration.getFullName());
            // proto3 JSON prints enums by name
            enumeration.getValues().forEach(value -> type.value(value.getName(), value.getName()));
            return type.build();
        });
        return GraphQLTypeReference.typeRef(name);
    }

    private static boolean isOpaque(Descriptor message) {
        return WELL_KNOWN.containsKey(message.getFullName()) || message.getFields().isEmpty();
    }

    private static void collect(Descriptor message, Set<GenericDescriptor> reachable) {
        if (isOpaque(message) || !reachable.add(message)) {
            return;
        }
        for (var field : message.getFields()) {
            if (field.isMapField()) {
                continue;
            }
            if (field.getJavaType() == FieldDescriptor.JavaType.MESSAGE) {
                collect(field.getMessageType(), reachable);
            } else if (field.getJavaType() == FieldDescriptor.JavaType.ENUM) {
                reachable.add(field.getEnumType());
            }
        }
    }

    /** Full proto name -> GraphQL name: name inside its package ({@code UploadChunk_Header}), qualified only on a clash. */
    private static Map<String, String> names(Set<GenericDescriptor> descriptors) {
        Function<GenericDescriptor, String> local = descriptor -> {
            var packageName = descriptor.getFile().getPackage();
            var name = packageName.isEmpty() ? descriptor.getFullName() : descriptor.getFullName().substring(packageName.length() + 1);
            return name.replace('.', '_');
        };
        var counts = descriptors.stream().collect(Collectors.groupingBy(local, Collectors.counting()));
        var names = new HashMap<String, String>();
        for (var descriptor : descriptors) {
            var name = local.apply(descriptor);
            names.put(descriptor.getFullName(), counts.get(name) > 1 ? descriptor.getFullName().replace('.', '_') : name);
        }
        return names;
    }
}

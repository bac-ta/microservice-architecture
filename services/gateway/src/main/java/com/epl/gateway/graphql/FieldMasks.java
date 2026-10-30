package com.epl.gateway.graphql;

import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.FieldDescriptor;
import graphql.schema.DataFetchingFieldSelectionSet;
import graphql.schema.SelectedField;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * GraphQL selection -> FieldMask paths of the response message, sent to the backend as {@code x-field-mask}
 * (see ResponseMask in libs/service-core): {@code { arrivalNo lines { productCode } }} -> {@code arrivalNo,lines.productCode}.
 * Aliases and fragments reading the same field give one path.
 */
final class FieldMasks {

    private FieldMasks() {
    }

    /** @return comma-separated paths; empty when the response type is opaque (JSON scalar) or nothing is selected */
    static String of(Descriptor response, DataFetchingFieldSelectionSet selection) {
        var paths = new LinkedHashSet<String>();
        collect(response, selection.getImmediateFields(), "", paths);
        return String.join(",", paths);
    }

    private static void collect(Descriptor message, List<SelectedField> selected, String prefix, Set<String> paths) {
        for (var selectedField : selected) {
            var field = byJsonName(message, selectedField.getName());
            if (field == null) {
                // __typename
                continue;
            }
            var path = prefix + field.getName();
            var children = selectedField.getSelectionSet().getImmediateFields();
            var before = paths.size();
            if (!children.isEmpty() && field.getJavaType() == FieldDescriptor.JavaType.MESSAGE && !field.isMapField()) {
                collect(field.getMessageType(), children, path + ".", paths);
            }
            if (paths.size() == before) {
                // a leaf, a JSON scalar (Struct, Timestamp …) or only __typename inside: the whole field
                paths.add(path);
            }
        }
    }

    private static FieldDescriptor byJsonName(Descriptor message, String jsonName) {
        for (var field : message.getFields()) {
            if (field.getJsonName().equals(jsonName)) {
                return field;
            }
        }
        return null;
    }
}

package com.epl.service.core;

import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.Message;
import io.grpc.Context;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Response fields the caller reads: the FieldMask paths of the gRPC metadata {@code x-field-mask}
 * ({@code arrivalNo,lines.productCode}), set by the gateway from the GraphQL selection. Without it, every field.
 * <p>
 * {@link ResponseMaskServerInterceptor} trims each response to these fields. Services check
 * {@link #includes(String)} to skip work nobody reads, e.g. loading the lines of a document:
 * <pre>
 * ResponseMask.current().includes("lines") ? withLines(find(no)) : header(findHeader(no))
 * </pre>
 * A repeated message field's sub-paths apply to each element ({@code lines.productCode}), unlike FieldMaskUtil.
 */
public final class ResponseMask {

    public static final ResponseMask ALL = new ResponseMask(null);

    static final Context.Key<ResponseMask> KEY = Context.key("epl.response-mask");

    /** null = every field; a node without children = the whole field. */
    private final Node root;

    private ResponseMask(Node root) {
        this.root = root;
    }

    /** The mask of the current gRPC call, {@link #ALL} outside a call or without x-field-mask. */
    public static ResponseMask current() {
        var mask = KEY.get();
        return mask == null ? ALL : mask;
    }

    /**
     * @param paths FieldMask paths separated by ',' (proto field names, '.' between levels); blank = every field
     * @throws IllegalArgumentException on an empty path segment
     */
    public static ResponseMask parse(String paths) {
        if (paths == null || paths.isBlank()) {
            return ALL;
        }
        var root = new Node();
        for (var path : paths.split(",")) {
            var node = root;
            for (var segment : path.trim().split("\\.", -1)) {
                if (segment.isEmpty()) {
                    throw new IllegalArgumentException("Invalid path '" + path.trim() + "'");
                }
                node = node.children.computeIfAbsent(segment, key -> new Node());
            }
        }
        return new ResponseMask(root);
    }

    public boolean isAll() {
        return root == null;
    }

    /** True when the field at {@code path} ({@code lines}, {@code lines.productCode}) or anything inside it is read. */
    public boolean includes(String path) {
        if (root == null) {
            return true;
        }
        var node = root;
        for (var segment : path.split("\\.")) {
            if (node != root && node.children.isEmpty()) {
                // the whole parent field is read
                return true;
            }
            node = node.children.get(segment);
            if (node == null) {
                return false;
            }
        }
        return true;
    }

    /** @throws IllegalArgumentException when a path names a field {@code message} does not have */
    public void validate(Descriptor message) {
        if (root != null) {
            validate(message, root, "");
        }
    }

    /** {@code message} with only the fields of this mask set. */
    @SuppressWarnings("unchecked")
    public <M extends Message> M trim(M message) {
        return root == null ? message : (M) trim(message, root);
    }

    public Context attach(Context context) {
        return context.withValue(KEY, this);
    }

    private static Message trim(Message message, Node node) {
        var builder = message.newBuilderForType();
        node.children.forEach((name, child) -> {
            var field = message.getDescriptorForType().findFieldByName(name);
            if (field == null || (!field.isRepeated() && field.hasPresence() && !message.hasField(field))) {
                return;
            }
            var value = message.getField(field);
            if (child.children.isEmpty() || !isMessage(field)) {
                builder.setField(field, value);
            } else if (field.isRepeated()) {
                for (var element : (Iterable<?>) value) {
                    builder.addRepeatedField(field, trim((Message) element, child));
                }
            } else {
                builder.setField(field, trim((Message) value, child));
            }
        });
        return builder.build();
    }

    private static void validate(Descriptor message, Node node, String prefix) {
        node.children.forEach((name, child) -> {
            var field = message.findFieldByName(name);
            if (field == null) {
                throw new IllegalArgumentException(message.getFullName() + " has no field '" + prefix + name + "'");
            }
            if (!child.children.isEmpty()) {
                if (!isMessage(field)) {
                    throw new IllegalArgumentException("'" + prefix + name + "' has no sub-fields");
                }
                validate(field.getMessageType(), child, prefix + name + ".");
            }
        });
    }

    private static boolean isMessage(FieldDescriptor field) {
        return field.getJavaType() == FieldDescriptor.JavaType.MESSAGE && !field.isMapField();
    }

    private static final class Node {
        private final Map<String, Node> children = new LinkedHashMap<>();
    }
}

package com.example.service.core;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;

/** gRPC errors raised by service methods (mapped to HTTP by the gateway). */
public final class Errors {

    private Errors() {
    }

    public static StatusRuntimeException notFound(String what, Object key) {
        return Status.NOT_FOUND.withDescription(what + " " + key + " not found").asRuntimeException();
    }

    public static StatusRuntimeException invalid(String message) {
        return Status.INVALID_ARGUMENT.withDescription(message).asRuntimeException();
    }

    public static StatusRuntimeException failedPrecondition(String message) {
        return Status.FAILED_PRECONDITION.withDescription(message).asRuntimeException();
    }

    public static StatusRuntimeException unsupported(String message) {
        return Status.UNIMPLEMENTED.withDescription(message).asRuntimeException();
    }

    public static String required(String value, String field) {
        if (value == null || value.isBlank()) {
            throw invalid(field + " is required");
        }
        return value.trim();
    }

    /**
     * Optimistic lock check for updates and deletes: the client sends back the version it read
     * (Audit.version or a version field).
     */
    public static void checkVersion(int current, int requested) {
        if (current != requested) {
            throw Status.ABORTED
                    .withDescription("The record was changed by someone else (version " + current + ", sent " + requested
                            + "); reload it and retry")
                    .asRuntimeException();
        }
    }

    /**
     * Upsert rule: a new key creates; an existing (not deleted) record is updated only when the client
     * sends the version it read, otherwise ALREADY_EXISTS (no blind overwrite).
     */
    public static void checkUpsert(String what, Object key, boolean exists, boolean hasVersion, int current, int requested) {
        if (!exists) {
            return;
        }
        if (!hasVersion) {
            throw Status.ALREADY_EXISTS
                    .withDescription(what + " " + key + " already exists; send audit.version to update it")
                    .asRuntimeException();
        }
        checkVersion(current, requested);
    }
}

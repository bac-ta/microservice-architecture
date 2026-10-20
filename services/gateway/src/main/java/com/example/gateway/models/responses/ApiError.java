package com.example.gateway.models.responses;

import io.grpc.Status;

/**
 * Error body of every failed gateway call.
 *
 * @param code      gRPC status code name (NOT_FOUND, INVALID_ARGUMENT, UNIMPLEMENTED …)
 * @param message   description from the backend
 * @param requestId the request's X-Request-Id, to find it in the logs
 */
public record ApiError(String code, String message, String requestId) {

    public static ApiError of(Status status, String requestId) {
        var message = status.getDescription() != null ? status.getDescription() : status.getCode().name();
        return new ApiError(status.getCode().name(), message, requestId);
    }
}

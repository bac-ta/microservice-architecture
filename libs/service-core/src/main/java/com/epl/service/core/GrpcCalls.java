package com.epl.service.core;

import com.google.protobuf.Empty;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.OptimisticLockingFailureException;

import java.sql.SQLException;
import java.util.function.Supplier;

/**
 * Unary RPC body: replies with the value or with the gRPC status the failure maps to, e.g.
 * <pre>{@code
 * public void getArea(GetAreaRequest request, StreamObserver<Area> responseObserver) {
 *     GrpcCalls.unary(responseObserver, () -> areaService.get(request.getCode()));
 * }
 * }</pre>
 */
public final class GrpcCalls {

    private static final Logger logger = LoggerFactory.getLogger(GrpcCalls.class);

    private GrpcCalls() {
    }

    public static <T> void unary(StreamObserver<T> responseObserver, Supplier<T> body) {
        T response;
        try {
            response = body.get();
        } catch (RuntimeException e) {
            responseObserver.onError(toStatus(e).asRuntimeException());
            return;
        }
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    public static void unaryEmpty(StreamObserver<Empty> responseObserver, Runnable body) {
        unary(responseObserver, () -> {
            body.run();
            return Empty.getDefaultInstance();
        });
    }

    /** Status for an exception thrown by a service method. */
    public static Status toStatus(RuntimeException e) {
        if (e instanceof StatusRuntimeException sre) {
            return sre.getStatus();
        }
        if (e instanceof OptimisticLockingFailureException) {
            return Status.ABORTED.withDescription("The record was changed by someone else; reload it and retry");
        }
        if (e instanceof DataAccessException) {
            var status = databaseStatus(e);
            if (status != null) {
                return status;
            }
        }
        if (e instanceof IllegalArgumentException) {
            return Status.INVALID_ARGUMENT.withDescription(e.getMessage());
        }
        logger.error("Unexpected error", e);
        return Status.INTERNAL.withDescription("Internal error");
    }

    // PostgreSQL SQLSTATE: 23505 unique, 23503 foreign key, 23514 check, 23502 not null, 42501 RLS / privilege
    private static Status databaseStatus(RuntimeException e) {
        var sqlException = sqlException(e);
        if (sqlException == null || sqlException.getSQLState() == null) {
            return null;
        }
        var state = sqlException.getSQLState();
        var detail = sqlException.getMessage() == null ? "" : sqlException.getMessage().lines().findFirst().orElse("");
        return switch (state) {
            case "23505" -> Status.ALREADY_EXISTS.withDescription(detail);
            case "23503" -> Status.FAILED_PRECONDITION.withDescription(detail);
            case "42501" -> Status.PERMISSION_DENIED.withDescription(detail);
            default -> state.startsWith("23") || state.startsWith("22") ? Status.INVALID_ARGUMENT.withDescription(detail) : null;
        };
    }

    private static SQLException sqlException(Throwable e) {
        for (var cause = e; cause != null; cause = cause.getCause()) {
            if (cause instanceof SQLException sql) {
                return sql;
            }
        }
        return null;
    }
}

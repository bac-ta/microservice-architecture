package com.example.gateway.controllers;

import com.example.gateway.configurations.RequestIdFilter;
import com.example.gateway.models.responses.ApiError;
import com.example.gateway.rpc.GrpcHttpStatus;
import com.google.protobuf.InvalidProtocolBufferException;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Backend gRPC errors -> HTTP status + {@link ApiError} body.
 */
@RestControllerAdvice
public class GatewayExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GatewayExceptionHandler.class);

    @ExceptionHandler(StatusRuntimeException.class)
    public ResponseEntity<ApiError> handleStatus(StatusRuntimeException e, HttpServletRequest request) {
        var status = e.getStatus();
        var httpStatus = GrpcHttpStatus.of(status.getCode());
        // UNIMPLEMENTED is expected while the legacy logic is being ported
        if (httpStatus >= 500 && status.getCode() != Status.Code.UNIMPLEMENTED) {
            logger.warn("{} {} -> {}", request.getMethod(), request.getRequestURI(), status, e);
        }
        return error(httpStatus, status, request);
    }

    @ExceptionHandler(InvalidProtocolBufferException.class)
    public ResponseEntity<ApiError> handleInvalidBody(InvalidProtocolBufferException e, HttpServletRequest request) {
        return error(400, Status.INVALID_ARGUMENT.withDescription("Invalid request body: " + e.getMessage()), request);
    }

    private static ResponseEntity<ApiError> error(int httpStatus, Status status, HttpServletRequest request) {
        return ResponseEntity.status(httpStatus)
                .contentType(MediaType.APPLICATION_JSON)
                .body(ApiError.of(status, RequestIdFilter.requestId(request)));
    }
}

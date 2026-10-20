package com.example.authentication.controllers;

import com.example.authentication.exceptions.ErrorResponse;
import com.example.authentication.exceptions.ExceptionHandler;
import com.example.authentication.exceptions.ServiceException;
import io.grpc.StatusRuntimeException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;

@ControllerAdvice
public class ExceptionController {

    private final ExceptionHandler<StatusRuntimeException> statusExceptionHandler;

    private final ExceptionHandler<ServiceException> serviceExceptionHandler;

    public ExceptionController(@Qualifier("statusRuntimeExceptionHandler") ExceptionHandler<StatusRuntimeException> statusExceptionHandler,
                               @Qualifier("serviceExceptionHandler") ExceptionHandler<ServiceException> serviceExceptionHandler) {
        this.statusExceptionHandler = statusExceptionHandler;
        this.serviceExceptionHandler = serviceExceptionHandler;
    }

    @org.springframework.web.bind.annotation.ExceptionHandler(StatusRuntimeException.class)
    public ResponseEntity<ErrorResponse> handleStatusRuntimeException(StatusRuntimeException sre) {
        ErrorResponse errorResponse = statusExceptionHandler.handle(sre);
        return new ResponseEntity<>(errorResponse, errorResponse.getHttpStatus());
    }

    @org.springframework.web.bind.annotation.ExceptionHandler(ServiceException.class)
    public ResponseEntity<ErrorResponse> handleServiceException(ServiceException se) {
        ErrorResponse errorResponse = serviceExceptionHandler.handle(se);
        return new ResponseEntity<>(errorResponse, errorResponse.getHttpStatus());
    }

}


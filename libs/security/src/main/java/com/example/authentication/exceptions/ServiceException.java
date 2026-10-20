package com.example.authentication.exceptions;

import com.google.common.collect.Lists;
import java.util.Collections;
import java.util.List;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ServiceException extends RuntimeException {

    private final HttpStatus httpStatus;
    private final List<String> errorCodes;

    public ServiceException(HttpStatus httpStatus) {
        this(httpStatus, "", Collections.emptyList());
    }

    public ServiceException(HttpStatus httpStatus, String message) {
        this(httpStatus, message, Collections.emptyList());
    }

    public ServiceException(HttpStatus httpStatus, String message, String errorCode) {
        this(httpStatus, message, Lists.newArrayList(errorCode));
    }

    public ServiceException(HttpStatus httpStatus, String message, List<String> errorCodes) {
        super(message);
        this.httpStatus = httpStatus;
        this.errorCodes = errorCodes;
    }

}

package com.entropy.authentication.securities;

public class InvalidAccessRuntimeException extends RuntimeException {

    public InvalidAccessRuntimeException() {
        super();
    }

    public InvalidAccessRuntimeException(String message) {
        super(message);
    }

    public InvalidAccessRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidAccessRuntimeException(Throwable cause) {
        super(cause);
    }

    protected InvalidAccessRuntimeException(String message, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}

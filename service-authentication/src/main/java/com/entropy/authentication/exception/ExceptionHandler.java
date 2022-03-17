package com.entropy.authentication.exception;

public interface ExceptionHandler<T extends Exception> {

    ErrorResponse handle(T exception);

}

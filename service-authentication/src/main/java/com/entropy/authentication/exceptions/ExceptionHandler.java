package com.entropy.authentication.exceptions;

public interface ExceptionHandler<T extends Exception> {

    ErrorResponse handle(T exception);

}

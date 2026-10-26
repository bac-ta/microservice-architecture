package com.epl.authentication.exceptions;

public interface ExceptionHandler<T extends Exception> {

    ErrorResponse handle(T exception);

}

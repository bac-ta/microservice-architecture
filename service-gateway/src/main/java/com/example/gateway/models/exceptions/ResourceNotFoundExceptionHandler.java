package com.example.gateway.models.exceptions;

import com.example.gateway.utils.APIMessage;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * @author bac-ta
 */
@Getter
@ResponseStatus(HttpStatus.NOT_FOUND)
public class ResourceNotFoundExceptionHandler extends RuntimeException {
    private String resourceName;
    private String fieldName;
    private Object fieldValue;

    public ResourceNotFoundExceptionHandler(String resourceName, String fieldName, Object fieldValue) {
        super(String.format(APIMessage.RESOURCE_NOT_FOUND, resourceName, fieldName, fieldValue));
        this.resourceName = resourceName;
        this.fieldName = fieldName;
        this.fieldValue = fieldValue;
    }

    public ResourceNotFoundExceptionHandler(String message, Throwable cause) {
        super(message, cause);
    }

    public ResourceNotFoundExceptionHandler(String message) {
        super(message);
    }

}

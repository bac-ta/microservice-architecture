package com.example.gateway.models.enums;

import com.example.gateway.models.exceptions.ResourceNotFoundExceptionHandler;
import com.example.gateway.utils.APIMessage;
import lombok.Getter;

import java.util.Arrays;

/**
 * @author bac-ta
 */
@Getter
public enum UserType {
    NORMAL(0, "NORMAL"),
    GUEST(1, "GUEST"),
    ADMINISTRATOR(2, "ADMINISTRATOR");
    private final int value;
    private final String name;

    UserType(int value, final String name) {
        this.value = value;
        this.name = name;
    }

    public static UserType findByValue(int value) {
        return Arrays.stream(UserType.values())
                .filter(userType -> userType.getValue() == value)
                .findFirst().orElseThrow(() -> new ResourceNotFoundExceptionHandler(APIMessage.USER_TYPE_INVALID));
    }
}

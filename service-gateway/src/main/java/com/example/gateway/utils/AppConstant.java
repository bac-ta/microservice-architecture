package com.example.gateway.utils;

/**
 * Class config app constants
 *
 * @author bac-ta
 * @see AppConstant
 * @since 2021-05-31
 */
public class AppConstant {
    public static final int REST_TEMPLATE_CONNECTION_TIMEOUT = 5000;
    public static final int REST_TEMPLATE_READ_TIMEOUT = 5000;
    public static final long CORS_MAX_AGE = 3000;
    public static final String LOGGER_PREFIX_NAME = "APP.";
    public static final String LOGGER_ERROR_PREFIX_NAME = "APP_ERROR.";
    public static final String DATE_FORMAT = "yyyy-MM-dd";
    public static final int MIN_PASSWORD_LENGTH = 8;
    public static final int MAX_PASSWORD_LENGTH = 20;
    public static final String PASSWORD_REGEX = "((?=.*[a-z])(?=.*[A-Z])(?=.*[0-9])|(?=.*[a-z])(?=.*[A-Z])(?=.*[!-/:-@\\[-`{-~])|(?=.*[a-z])(?=.*[0-9])(?=.*[!-/:-@\\[-`{-~])|(?=.*[A-Z])(?=.*[0-9])(?=.*[!-/:-@\\[-`{-~]))[a-zA-Z0-9!-/:-@\\[-`{-~]{0,1000}";
}


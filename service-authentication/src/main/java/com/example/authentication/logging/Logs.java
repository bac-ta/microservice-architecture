package com.example.authentication.logging;

import org.apache.commons.lang3.StringUtils;

final class Logs {

    private static final String EMPTY_REQUEST_ID;

    static {
        final int requestIdMaxLength = 36;
        char[] buf = new char[requestIdMaxLength];
        for (var i = requestIdMaxLength - 1; i >= 0; i--) {
            buf[i] = '-';
        }
        EMPTY_REQUEST_ID = new String(buf);
    }

    private Logs() {
    }

    static String toAlternateRequestIdIfEmpty(String requestId) {
        return StringUtils.isBlank(requestId) ? EMPTY_REQUEST_ID : requestId;
    }
}

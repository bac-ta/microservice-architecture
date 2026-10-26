package com.epl.service.core;

import com.epl.common.v1.PageRequest;
import com.epl.common.v1.PageResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Cursor paging of epl.common.v1.PageRequest on top of Spring Data pages.
 * The token is opaque to clients (today: the encoded page number).
 */
public final class Pages {

    public static final int DEFAULT_SIZE = 50;
    public static final int MAX_SIZE = 500;

    private Pages() {
    }

    public static Pageable pageable(PageRequest page, Sort sort) {
        var size = page.getPageSize() <= 0 ? DEFAULT_SIZE : Math.min(page.getPageSize(), MAX_SIZE);
        return org.springframework.data.domain.PageRequest.of(pageNumber(page.getPageToken()), size, sort);
    }

    public static PageResponse response(Page<?> page) {
        var builder = PageResponse.newBuilder().setTotalSize(page.getTotalElements());
        if (page.hasNext()) {
            builder.setNextPageToken(token(page.getNumber() + 1));
        }
        return builder.build();
    }

    private static int pageNumber(String token) {
        if (token == null || token.isEmpty()) {
            return 0;
        }
        try {
            return Integer.parseInt(new String(Base64.getUrlDecoder().decode(token), StandardCharsets.UTF_8));
        } catch (IllegalArgumentException e) {
            throw Errors.invalid("Invalid page_token");
        }
    }

    private static String token(int pageNumber) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(String.valueOf(pageNumber).getBytes(StandardCharsets.UTF_8));
    }
}

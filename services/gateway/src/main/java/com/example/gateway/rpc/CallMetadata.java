package com.example.gateway.rpc;

import com.example.gateway.configurations.RequestIdFilter;
import io.grpc.Metadata;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.List;

/**
 * gRPC metadata forwarded to the backend from the HTTP request.
 * Tenant context travels in headers, not in messages (see epl/common/v1/common.proto):
 * backends verify the JWT again and check the requested tenant against its grants.
 */
public final class CallMetadata {

    static final List<String> FORWARDED_HEADERS = List.of(
            "authorization",
            "x-company",
            "x-warehouse",
            "x-customer",
            "accept-language");

    private CallMetadata() {
    }

    /** Authenticated user, set from the verified JWT ("usr", else "sub"); used by backends for created_by / updated_by. */
    static final String USER_HEADER = "x-user";

    public static Metadata from(HttpServletRequest request) {
        var metadata = new Metadata();
        var user = authenticatedUser();
        if (user != null) {
            metadata.put(Metadata.Key.of(USER_HEADER, Metadata.ASCII_STRING_MARSHALLER), user);
        }
        for (var header : FORWARDED_HEADERS) {
            var value = request.getHeader(header);
            if (value != null && !value.isBlank()) {
                metadata.put(Metadata.Key.of(header, Metadata.ASCII_STRING_MARSHALLER), value);
            }
        }
        metadata.put(Metadata.Key.of(RequestIdFilter.HEADER, Metadata.ASCII_STRING_MARSHALLER), RequestIdFilter.requestId(request));
        return metadata;
    }

    private static String authenticatedUser() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof JwtAuthenticationToken jwt) {
            var username = jwt.getToken().getClaimAsString("usr");
            return username != null && !username.isBlank() ? username : jwt.getName();
        }
        return null;
    }
}

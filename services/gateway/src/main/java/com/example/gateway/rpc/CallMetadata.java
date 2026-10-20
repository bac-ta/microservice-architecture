package com.example.gateway.rpc;

import com.example.gateway.configurations.RequestIdFilter;
import io.grpc.Metadata;
import jakarta.servlet.http.HttpServletRequest;

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

    public static Metadata from(HttpServletRequest request) {
        var metadata = new Metadata();
        for (var header : FORWARDED_HEADERS) {
            var value = request.getHeader(header);
            if (value != null && !value.isBlank()) {
                metadata.put(Metadata.Key.of(header, Metadata.ASCII_STRING_MARSHALLER), value);
            }
        }
        metadata.put(Metadata.Key.of(RequestIdFilter.HEADER, Metadata.ASCII_STRING_MARSHALLER), RequestIdFilter.requestId(request));
        return metadata;
    }
}

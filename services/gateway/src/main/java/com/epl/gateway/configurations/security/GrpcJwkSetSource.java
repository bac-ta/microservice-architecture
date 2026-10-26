package com.epl.gateway.configurations.security;

import com.nimbusds.jose.KeySourceException;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.source.JWKSetCacheRefreshEvaluator;
import com.nimbusds.jose.jwk.source.JWKSetSource;
import com.nimbusds.jose.proc.SecurityContext;
import io.grpc.StatusRuntimeException;

import java.text.ParseException;
import java.util.function.Supplier;

/**
 * Key set of the auth service, fetched over gRPC (AuthService/GetJwks) instead of an HTTP JWKS URL.
 * Caching and rate limiting are added by {@link com.nimbusds.jose.jwk.source.JWKSourceBuilder}.
 */
public class GrpcJwkSetSource implements JWKSetSource<SecurityContext> {

    private final Supplier<String> jwksJson;

    public GrpcJwkSetSource(Supplier<String> jwksJson) {
        this.jwksJson = jwksJson;
    }

    @Override
    public JWKSet getJWKSet(JWKSetCacheRefreshEvaluator refreshEvaluator, long currentTime, SecurityContext context)
            throws KeySourceException {
        try {
            return JWKSet.parse(jwksJson.get());
        } catch (StatusRuntimeException e) {
            throw new KeySourceException("Cannot load the JWKS from the auth service: " + e.getStatus(), e);
        } catch (ParseException e) {
            throw new KeySourceException("Invalid JWKS from the auth service", e);
        }
    }

    @Override
    public void close() {
    }
}

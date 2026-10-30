package com.epl.gateway.configurations;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * Settings under {@code epl.gateway} (see application.yml).
 *
 * @param backends       backend name -> gRPC target, e.g. {@code inventory -> dns:///inventory:9104}
 * @param deadline       deadline of a unary / client-streaming call
 * @param streamDeadline deadline of a server-streaming call (exports, report rendering)
 * @param scanPackages   packages searched for generated {@code *Grpc} classes; their RPCs are exposed under /rpc
 * @param graphql        mobile GraphQL API ({@code /m/graphql})
 */
@ConfigurationProperties("epl.gateway")
public record GatewayProperties(
        Map<String, String> backends,
        @DefaultValue("30s") Duration deadline,
        @DefaultValue("10m") Duration streamDeadline,
        @DefaultValue("com.epl") List<String> scanPackages,
        @DefaultValue Security security,
        @DefaultValue Cors cors,
        @DefaultValue Graphql graphql) {

    public GatewayProperties {
        backends = backends == null ? Map.of() : Map.copyOf(backends);
    }

    /**
     * @param enabled       false = every request is let through (local development only)
     * @param issuer        expected {@code iss} claim; empty = not checked
     * @param jwsAlgorithms accepted signature algorithms of the auth service's JWT
     * @param jwksCacheTtl  how long the key set fetched from AuthService/GetJwks is reused
     * @param publicPaths   paths reachable without a token (relative to the context path)
     */
    public record Security(
            @DefaultValue("true") boolean enabled,
            String issuer,
            @DefaultValue("ES256") List<String> jwsAlgorithms,
            @DefaultValue("5m") Duration jwksCacheTtl,
            List<String> publicPaths) {

        public Security {
            publicPaths = publicPaths == null ? List.of() : List.copyOf(publicPaths);
        }
    }

    /**
     * @param maxRpcCalls             RPC fields one request may call (aliases count once each)
     * @param maxDepth                maximum query depth (namespace and service count as levels)
     * @param persistedQueryCacheSize parsed documents kept for Automatic Persisted Queries (least recently used dropped)
     * @param exclude                 RPCs not exposed, as full method name patterns ({@code epl.auth.v1.AuthorizationService/*})
     */
    public record Graphql(
            @DefaultValue("20") int maxRpcCalls,
            @DefaultValue("15") int maxDepth,
            @DefaultValue("1000") int persistedQueryCacheSize,
            List<String> exclude) {

        public Graphql {
            exclude = exclude == null ? List.of() : List.copyOf(exclude);
        }
    }

    public record Cors(List<String> allowedOrigins) {

        public Cors {
            allowedOrigins = allowedOrigins == null ? List.of() : List.copyOf(allowedOrigins);
        }
    }
}

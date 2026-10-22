package com.example.service.core;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.time.Duration;

@ConfigurationProperties("epl")
public record ServiceCoreProperties(@DefaultValue Tenancy tenancy) {

    /**
     * @param authTarget gRPC target of the auth service (GRPC_AUTH_TARGET), e.g. dns:///auth:9101
     * @param deadline   per ResolveTenant call
     * @param cacheTtl   how long resolved ids are kept
     */
    public record Tenancy(@DefaultValue("dns:///localhost:9101") String authTarget,
                          @DefaultValue("3s") Duration deadline,
                          @DefaultValue("10m") Duration cacheTtl) {
    }
}

package com.example.service.core.test;

import com.example.grpc.server.services.GrpcFinderService;
import com.example.service.core.TenantResolver;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import java.io.IOException;

/**
 * {@code @Import} in a service's {@code @SpringBootTest}: in-process gRPC server and tenant
 * resolution from the test database. The auth service uses {@link GrpcOnly} (it resolves itself).
 */
@TestConfiguration(proxyBeanMethods = false)
public class EplServiceTestConfiguration {

    @Bean
    public GrpcTestServer grpcTestServer(GrpcFinderService finder) throws IOException {
        return new GrpcTestServer(finder);
    }

    @Bean
    @Primary
    public TenantResolver testTenantResolver() {
        return new TestTenantResolver();
    }

    @TestConfiguration(proxyBeanMethods = false)
    public static class GrpcOnly {

        @Bean
        public GrpcTestServer grpcTestServer(GrpcFinderService finder) throws IOException {
            return new GrpcTestServer(finder);
        }
    }
}

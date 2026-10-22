package com.example.service.core.test;

import com.example.service.core.TenantResolver;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;

/**
 * {@code @Import} in a service's {@code @SpringBootTest}: in-process gRPC server and tenant
 * resolution from the test database instead of the auth service.
 */
@TestConfiguration(proxyBeanMethods = false)
@Import(GrpcTestServerConfiguration.class)
public class EplServiceTestConfiguration {

    @Bean
    @Primary
    public TenantResolver testTenantResolver() {
        return new TestTenantResolver();
    }
}

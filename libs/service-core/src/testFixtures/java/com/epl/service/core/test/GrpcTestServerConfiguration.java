package com.epl.service.core.test;

import com.epl.grpc.server.services.GrpcFinderService;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import java.io.IOException;

/** {@code @Import} in the auth service's tests: in-process gRPC server only (auth resolves tenants itself). */
@TestConfiguration(proxyBeanMethods = false)
public class GrpcTestServerConfiguration {

    @Bean
    public GrpcTestServer grpcTestServer(GrpcFinderService finder) throws IOException {
        return new GrpcTestServer(finder);
    }
}

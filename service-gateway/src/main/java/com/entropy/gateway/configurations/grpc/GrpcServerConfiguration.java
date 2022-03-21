package com.entropy.gateway.configurations.grpc;

import com.entropy.authentication.security.Interceptor.grpc.ErrorHandlingInterceptor;
import com.entropy.authentication.logging.GrpcServerLoggingInterceptor;
import com.entropy.grpcserver.services.GrpcServerInterceptorInitializerService;
import com.google.common.collect.Lists;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GrpcServerConfiguration {
    @Bean
    public GrpcServerInterceptorInitializerService globalServerInterceptorInitializer() {
        return () -> Lists.newArrayList(
                new GrpcServerLoggingInterceptor(),
                new ErrorHandlingInterceptor());
    }
}

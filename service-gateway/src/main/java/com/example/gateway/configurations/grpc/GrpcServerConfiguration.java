package com.example.gateway.configurations.grpc;

import com.example.authentication.logging.GrpcServerLoggingInterceptor;
import com.example.authentication.securities.interceptors.grpc.ErrorHandlingInterceptor;
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

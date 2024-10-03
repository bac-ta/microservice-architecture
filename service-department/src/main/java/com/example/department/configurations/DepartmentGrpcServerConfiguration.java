package com.example.department.configurations;

import com.example.grpc.server.services.GrpcServerInterceptorInitializerService;
import com.example.authentication.logging.GrpcServerLoggingInterceptor;
import com.example.authentication.securities.interceptors.grpc.ErrorHandlingInterceptor;
import com.google.common.collect.Lists;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DepartmentGrpcServerConfiguration {

    @Bean
    public GrpcServerInterceptorInitializerService globalServerInterceptorInitializer() {
        return () -> Lists.newArrayList(new ErrorHandlingInterceptor(), new GrpcServerLoggingInterceptor());
    }
}

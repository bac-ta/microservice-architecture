package com.entropy.department.configurations;

import com.entropy.authentication.securities.Interceptors.grpc.ErrorHandlingInterceptor;
import com.entropy.authentication.logging.GrpcServerLoggingInterceptor;
import com.entropy.grpcserver.services.GrpcServerInterceptorInitializerService;
import com.google.common.collect.Lists;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DepartmentGrpcServerConfiguration {

    @Bean
    public GrpcServerInterceptorInitializerService globalServerInterceptorInitializer() {
        return () -> Lists.newArrayList(
                new ErrorHandlingInterceptor(),
                new GrpcServerLoggingInterceptor()
        );
    }
}

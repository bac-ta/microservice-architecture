package com.example.epl.report.configurations;

import com.example.authentication.logging.GrpcServerLoggingInterceptor;
import com.example.authentication.securities.interceptors.grpc.ErrorHandlingInterceptor;
import com.example.grpc.server.services.GrpcServerInterceptorInitializerService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class ReportGrpcServerConfiguration {

    @Bean
    public GrpcServerInterceptorInitializerService globalServerInterceptorInitializer() {
        return () -> List.of(
                new ErrorHandlingInterceptor(),
                new GrpcServerLoggingInterceptor()
        );
    }
}

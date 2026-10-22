package com.example.epl.inbound.configurations;

import com.example.authentication.logging.GrpcServerLoggingInterceptor;
import com.example.authentication.securities.interceptors.grpc.ErrorHandlingInterceptor;
import com.example.grpc.server.services.GrpcServerInterceptorInitializerService;
import com.example.service.core.RequestContextServerInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class InboundGrpcServerConfiguration {

    // the last interceptor runs first: the request context is set before the others
    @Bean
    public GrpcServerInterceptorInitializerService globalServerInterceptorInitializer(RequestContextServerInterceptor requestContext) {
        return () -> List.of(
                new ErrorHandlingInterceptor(),
                new GrpcServerLoggingInterceptor(),
                requestContext
        );
    }
}

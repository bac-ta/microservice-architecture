package com.epl.billing.configurations;

import com.epl.authentication.logging.GrpcServerLoggingInterceptor;
import com.epl.authentication.securities.interceptors.grpc.ErrorHandlingInterceptor;
import com.epl.grpc.server.services.GrpcServerInterceptorInitializerService;
import com.epl.service.core.RequestContextServerInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class BillingGrpcServerConfiguration {

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

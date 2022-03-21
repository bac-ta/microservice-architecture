package com.entropy.gateway.configurations.grpc;

import com.entropy.authentication.security.Interceptor.grpc.GrpcHeaderClientInterceptor;
import com.entropy.grpc.client.services.GrpcClientInterceptorInitializerService;
import com.google.common.collect.Lists;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GrpcClientConfiguration {

    @Bean
    public GrpcClientInterceptorInitializerService globalClientInterceptorInitializer() {
        return () -> Lists.newArrayList(
                new GrpcHeaderClientInterceptor());
    }
}

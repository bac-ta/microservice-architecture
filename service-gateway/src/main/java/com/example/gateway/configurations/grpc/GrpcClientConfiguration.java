package com.example.gateway.configurations.grpc;

import com.example.authentication.securities.interceptors.grpc.GrpcHeaderClientInterceptor;
import com.example.grpc.client.services.GrpcClientInterceptorInitializerService;
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

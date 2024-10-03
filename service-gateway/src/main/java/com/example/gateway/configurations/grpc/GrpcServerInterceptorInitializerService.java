package com.example.gateway.configurations.grpc;

import io.grpc.ServerInterceptor;

import java.util.Collection;

public interface GrpcServerInterceptorInitializerService {
    Collection<ServerInterceptor> interceptors();
}

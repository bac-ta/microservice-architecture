package com.epl.grpc.server.services;

import io.grpc.ServerInterceptor;

import java.util.Collection;

public interface GrpcServerInterceptorInitializerService {
    Collection<ServerInterceptor> interceptors();
}

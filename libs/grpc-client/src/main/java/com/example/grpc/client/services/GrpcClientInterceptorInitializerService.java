package com.example.grpc.client.services;

import io.grpc.ClientInterceptor;
import java.util.Collection;

public interface GrpcClientInterceptorInitializerService {

    Collection<ClientInterceptor> interceptors();
}

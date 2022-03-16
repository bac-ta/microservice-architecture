package com.entropy.grpc.client.services;

import io.grpc.ClientInterceptor;
import java.util.Collection;

public interface ClientInterceptorInitializerService {

    Collection<ClientInterceptor> interceptors();
}

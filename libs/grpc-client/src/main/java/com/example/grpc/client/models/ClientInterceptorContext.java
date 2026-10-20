package com.example.grpc.client.models;

import com.example.grpc.client.services.GrpcClientInterceptorInitializerService;
import io.grpc.ClientInterceptor;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class ClientInterceptorContext implements ApplicationContextAware {

    private final List<ClientInterceptor> clientInterceptors = new ArrayList<>();
    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @PostConstruct
    public void init() {
        var adapters =
                applicationContext.getBeansOfType(GrpcClientInterceptorInitializerService.class);
        for (GrpcClientInterceptorInitializerService initializer : adapters.values()) {
            Collection<ClientInterceptor> interceptors = initializer.interceptors();
            if (interceptors != null && !interceptors.isEmpty()) {
                clientInterceptors.addAll(interceptors);
            }
        }
    }

    public Collection<ClientInterceptor> getClientInterceptors() {
        return Collections.unmodifiableCollection(clientInterceptors);
    }

}

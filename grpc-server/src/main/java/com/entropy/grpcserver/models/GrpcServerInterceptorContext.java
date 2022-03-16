package com.entropy.grpcserver.models;

import com.entropy.grpcserver.services.GrpcServerInterceptorInitializerService;
import io.grpc.ServerInterceptor;
import lombok.Setter;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import javax.annotation.PostConstruct;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class GrpcServerInterceptorContext implements ApplicationContextAware {
    private final List<ServerInterceptor> serverInterceptors = new LinkedList<>();

    @Setter
    private ApplicationContext applicationContext;

    @PostConstruct
    public void init() {
        Map<String, GrpcServerInterceptorInitializerService> map =
                applicationContext.getBeansOfType(GrpcServerInterceptorInitializerService.class);
        map.values().forEach(initializer -> Optional.of(initializer.interceptors()).ifPresent(serverInterceptors::addAll));
    }

    public Collection<ServerInterceptor> getServerInterceptors() {
        return Collections.unmodifiableCollection(serverInterceptors);
    }
}

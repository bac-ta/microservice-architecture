package com.example.grpc.client.services.impl;

import com.example.grpc.client.models.ClientInterceptorContext;
import com.example.grpc.client.services.GrpcChannelService;
import io.grpc.Channel;
import io.grpc.ClientInterceptor;
import io.grpc.ClientInterceptors;
import io.grpc.ManagedChannelBuilder;

import java.util.ArrayList;
import java.util.List;

public class DiscoveryClientChannelProviderImpl implements GrpcChannelService {
    private final ClientInterceptorContext interceptorContext;

    public DiscoveryClientChannelProviderImpl(
            ClientInterceptorContext interceptorContext) {
        this.interceptorContext = interceptorContext;
    }

    @Override
    public Channel createChannel(String name, List<ClientInterceptor> interceptors) {
        var channel = ManagedChannelBuilder.forTarget(name)
                .defaultLoadBalancingPolicy("round_robin")
                .usePlaintext()
                .build();
        return ClientInterceptors.intercept(channel, createInterceptors(interceptors));
    }

    private List<ClientInterceptor> createInterceptors(List<ClientInterceptor> interceptors) {
        var globalInterceptors =
                interceptorContext.getClientInterceptors();
        List<ClientInterceptor> allInterceptors = new ArrayList<>();
        allInterceptors.addAll(globalInterceptors);
        allInterceptors.addAll(interceptors);
        return allInterceptors;
    }
}


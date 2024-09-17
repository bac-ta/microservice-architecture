package com.entropy.grpc.client.services.impl;

import com.entropy.grpc.client.configurations.GrpcChannelPropertiesComponent;
import com.entropy.grpc.client.models.ClientInterceptorContext;
import com.entropy.grpc.client.services.GrpcChannelService;
import io.grpc.Channel;
import io.grpc.ClientInterceptor;
import io.grpc.ClientInterceptors;
import io.grpc.netty.NettyChannelBuilder;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class AddressChannelFactoryImpl implements GrpcChannelService {

    private final GrpcChannelPropertiesComponent properties;
    private final ClientInterceptorContext interceptorContext;

    public AddressChannelFactoryImpl(GrpcChannelPropertiesComponent properties,
                                     ClientInterceptorContext interceptorContext) {

        this.properties = properties;
        this.interceptorContext = interceptorContext;
    }

    @Override
    public Channel createChannel(String name, List<ClientInterceptor> interceptors) {
        var channelProperties = properties.getChannel(name);
        var builder = NettyChannelBuilder.forTarget(name)
                .defaultLoadBalancingPolicy("round_robin")
                .usePlaintext();

        if (channelProperties.isEnableKeepAlive()) {
            builder.keepAliveWithoutCalls(channelProperties.isKeepAliveWithoutCalls())
                    .keepAliveTime(channelProperties.getKeepAliveTime(), TimeUnit.SECONDS)
                    .keepAliveTimeout(channelProperties.getKeepAliveTimeout(), TimeUnit.SECONDS);
        }

        var channel = builder.build();

        var globalInterceptors = interceptorContext.getClientInterceptors();
        Set<ClientInterceptor> interceptorSet = new HashSet<>();
        if (globalInterceptors != null && !globalInterceptors.isEmpty()) {
            interceptorSet.addAll(globalInterceptors);
        }
        if (interceptors != null && !interceptors.isEmpty()) {
            interceptorSet.addAll(interceptors);
        }

        return ClientInterceptors.intercept(channel, new ArrayList<>(interceptorSet));
    }
}

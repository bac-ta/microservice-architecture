package com.entropy.grpc.client.services.impl;

import com.entropy.grpc.client.models.DiscoveryClientResolverFactory;
import com.entropy.grpc.client.configurations.GrpcChannelPropertiesComponent;
import com.entropy.grpc.client.models.ClientInterceptorContext;
import com.entropy.grpc.client.models.GrpcChannelProperties;
import com.entropy.grpc.client.services.GrpcChannelService;
import io.grpc.Channel;
import io.grpc.ClientInterceptor;
import io.grpc.ClientInterceptors;
import io.grpc.LoadBalancer;
import io.grpc.ManagedChannelBuilder;
import org.springframework.cloud.client.discovery.DiscoveryClient;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class DiscoveryClientChannelProviderImpl implements GrpcChannelService {
    private final GrpcChannelPropertiesComponent channels;
    private final DiscoveryClient client;
    private final LoadBalancer.Factory loadBalancerFactory;
    private final ClientInterceptorContext interceptorContext;

    public DiscoveryClientChannelProviderImpl(
            GrpcChannelPropertiesComponent channels,
            DiscoveryClient client,
            LoadBalancer.Factory loadBalancerFactory,
            ClientInterceptorContext interceptorContext) {
        this.channels = channels;
        this.client = client;
        this.loadBalancerFactory = loadBalancerFactory;
        this.interceptorContext = interceptorContext;
    }

    @Override
    public Channel createChannel(String name, List<ClientInterceptor> interceptors) {
        GrpcChannelProperties channelProperties = channels.getChannel(name);
        Channel channel = ManagedChannelBuilder.forTarget(name)
                .loadBalancerFactory(loadBalancerFactory)
                .nameResolverFactory(new DiscoveryClientResolverFactory(client))
                .usePlaintext(channelProperties.isPlaintext()).build();
        return ClientInterceptors.intercept(channel, createInterceptors(interceptors));
    }

    private List<ClientInterceptor> createInterceptors(List<ClientInterceptor> interceptors) {
        Collection<ClientInterceptor> globalInterceptors =
                interceptorContext.getClientInterceptors();
        List<ClientInterceptor> allInterceptors = new ArrayList<>();
        allInterceptors.addAll(globalInterceptors);
        allInterceptors.addAll(interceptors);
        return allInterceptors;
    }
}

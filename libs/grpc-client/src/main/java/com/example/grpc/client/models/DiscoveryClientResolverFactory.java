package com.example.grpc.client.models;

import io.grpc.NameResolver;
import io.grpc.internal.GrpcUtil;
import org.springframework.cloud.client.discovery.DiscoveryClient;

import javax.annotation.Nullable;
import java.net.URI;

public class DiscoveryClientResolverFactory extends NameResolver.Factory {
    private final DiscoveryClient client;

    public DiscoveryClientResolverFactory(DiscoveryClient client) {
        this.client = client;
    }

    @Nullable
    @Override
    public NameResolver newNameResolver(URI uri, NameResolver.Args args) {
        return new DiscoveryClientNameResolver(uri.toString(),
                client,
                GrpcUtil.TIMER_SERVICE,
                GrpcUtil.SHARED_CHANNEL_EXECUTOR);
    }

    @Override
    public String getDefaultScheme() {
        return "spring";
    }
}

package com.entropy.grpc.client.models;

import com.entropy.grpc.client.configurations.GrpcChannelPropertiesComponent;
import io.grpc.NameResolver;
import io.grpc.NameResolverProvider;
import io.grpc.internal.GrpcUtil;

import javax.annotation.Nullable;
import java.net.URI;

public class AddressChannelResolverProvider extends NameResolverProvider {

    private final GrpcChannelPropertiesComponent properties;

    public AddressChannelResolverProvider(GrpcChannelPropertiesComponent properties) {
        this.properties = properties;
    }

    @Override
    protected boolean isAvailable() {
        return true;
    }

    @Override
    protected int priority() {
        return 5;
    }

    @Nullable
    @Override
    public NameResolver newNameResolver(URI uri, NameResolver.Args args) {
        return new AddressChannelNameResolver(uri.toString(), properties.getChannel(uri.toString()), GrpcUtil.SHARED_CHANNEL_EXECUTOR);
    }

    @Override
    public String getDefaultScheme() {
        return "address";
    }
}

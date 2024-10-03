package com.example.grpc.client.models;

import io.grpc.Attributes;
import io.grpc.EquivalentAddressGroup;
import io.grpc.NameResolver;
import io.grpc.internal.SharedResourceHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.concurrent.GuardedBy;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;

public class AddressChannelNameResolver extends NameResolver {

    private static final Logger logger = LoggerFactory.getLogger(AddressChannelNameResolver.class);

    private final String name;
    private final GrpcChannelProperties properties;
    private final SharedResourceHolder.Resource<Executor> executorResource;

    @GuardedBy("this")
    private boolean shutdown;
    @GuardedBy("this")
    private Executor executor;
    @GuardedBy("this")
    private boolean resolving;
    @GuardedBy("this")
    private Listener listener;

    public AddressChannelNameResolver(String name,
                                      GrpcChannelProperties properties,
                                      SharedResourceHolder.Resource<Executor> executorResource) {
        this.name = name;
        this.properties = properties;
        this.executorResource = executorResource;
    }

    @Override
    public String getServiceAuthority() {
        return name;
    }

    @Override
    public void start(Listener listener) {
        if (listener == null) {
            throw new IllegalArgumentException("listener is null");
        }

        if (this.listener != null) {
            throw new IllegalStateException("already started.");
        }

        this.listener = listener;
        executor = SharedResourceHolder.get(executorResource);

        resolve();
    }

    private void resolve() {
        if (resolving || shutdown) {
            return;
        }
        executor.execute(() -> {
            Listener savedListener;
            synchronized (AddressChannelNameResolver.this) {
                if (shutdown) {
                    return;
                }
                savedListener = listener;
                resolving = true;
            }

            try {
                if (properties.getHosts().size() != properties.getPorts().size()) {
                    logger.error("hosts size is not equal ports size, hosts: {}, ports: {}", properties.getHosts().size(), properties.getPorts().size());
                    return;
                }

                List<EquivalentAddressGroup> groups = new ArrayList<>();
                for (int i = 0; i < properties.getHosts().size(); i++) {
                    String host = properties.getHosts().get(i);
                    int port = properties.getPorts().get(i);

                    logger.info("Found gRPC server {} {}:{}", name, host, port);

                    InetSocketAddress address = new InetSocketAddress(host, port);
                    groups.add(new EquivalentAddressGroup(address, Attributes.EMPTY));
                }
                savedListener.onAddresses(groups, Attributes.EMPTY);
            } finally {
                synchronized (AddressChannelNameResolver.this) {
                    resolving = false;
                }
            }
        });
    }

    @Override
    public void refresh() {
        if (this.listener != null) {
            resolve();
        }
    }

    @Override
    public void shutdown() {
        if (shutdown) {
            return;
        }
        shutdown = true;

        if (executor != null) {
            executor = SharedResourceHolder.release(executorResource, executor);
        }

    }
}

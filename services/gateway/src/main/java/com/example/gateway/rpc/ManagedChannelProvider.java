package com.example.gateway.rpc;

import com.example.gateway.configurations.GatewayProperties;
import io.grpc.Channel;
import io.grpc.ManagedChannel;
import io.grpc.Status;
import io.grpc.netty.NettyChannelBuilder;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * One long-lived channel per backend, created on first use from {@code epl.gateway.backends}.
 */
@Component
public class ManagedChannelProvider implements ChannelProvider {

    private static final Logger logger = LoggerFactory.getLogger(ManagedChannelProvider.class);

    private final Map<String, String> targets;
    private final Map<String, ManagedChannel> channels = new ConcurrentHashMap<>();

    public ManagedChannelProvider(GatewayProperties properties) {
        this.targets = properties.backends();
    }

    @Override
    public Channel channel(String backend) {
        var target = targets.get(backend);
        if (target == null || target.isBlank()) {
            throw Status.UNAVAILABLE
                    .withDescription("No gRPC target configured for backend '" + backend + "' (epl.gateway.backends." + backend + ")")
                    .asRuntimeException();
        }
        return channels.computeIfAbsent(backend, name -> {
            logger.info("Opening gRPC channel to backend {} at {}", name, target);
            return NettyChannelBuilder.forTarget(target)
                    .defaultLoadBalancingPolicy("round_robin")
                    .usePlaintext()
                    .build();
        });
    }

    @PreDestroy
    public void shutdown() throws InterruptedException {
        channels.values().forEach(ManagedChannel::shutdown);
        for (var channel : channels.values()) {
            if (!channel.awaitTermination(5, TimeUnit.SECONDS)) {
                channel.shutdownNow();
            }
        }
    }
}

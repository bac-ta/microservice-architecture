package com.epl.grpc.server;

import com.epl.grpc.server.models.GrpcServerProperties;
import com.epl.grpc.server.services.impls.GrpcServerServiceImpl;
import com.epl.grpc.server.services.impls.GrpcServerServiceLifecycleImpl;
import io.grpc.ManagedChannel;
import io.grpc.StatusRuntimeException;
import io.grpc.health.v1.HealthCheckRequest;
import io.grpc.health.v1.HealthCheckResponse.ServingStatus;
import io.grpc.health.v1.HealthGrpc;
import io.grpc.netty.NettyChannelBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.ServerSocket;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GrpcServerLifecycleTests {

    private ManagedChannel channel;

    @AfterEach
    void closeChannel() {
        if (channel != null) {
            channel.shutdownNow();
        }
    }

    @Test
    void servesHealthWhileRunningAndStopsGracefully() throws IOException {
        var properties = new GrpcServerProperties();
        properties.setAddress("127.0.0.1");
        properties.setPort(freePort());
        properties.setShutdownGracePeriod(Duration.ofSeconds(2));
        var lifecycle = new GrpcServerServiceLifecycleImpl(new GrpcServerServiceImpl(properties));

        lifecycle.start();
        assertThat(lifecycle.isRunning()).isTrue();

        channel = NettyChannelBuilder.forAddress("127.0.0.1", properties.getPort()).usePlaintext().build();
        var health = HealthGrpc.newBlockingStub(channel);
        assertThat(health.check(HealthCheckRequest.getDefaultInstance()).getStatus()).isEqualTo(ServingStatus.SERVING);

        lifecycle.stop();
        assertThat(lifecycle.isRunning()).isFalse();
        assertThatThrownBy(() -> health.check(HealthCheckRequest.getDefaultInstance()))
                .isInstanceOf(StatusRuntimeException.class);
    }

    private static int freePort() throws IOException {
        try (var socket = new ServerSocket(0)) {
            return socket.getLocalPort();
        }
    }
}

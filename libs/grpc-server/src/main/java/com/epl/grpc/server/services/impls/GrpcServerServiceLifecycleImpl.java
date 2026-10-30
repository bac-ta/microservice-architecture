package com.epl.grpc.server.services.impls;

import com.epl.grpc.server.services.GrpcServerService;
import io.grpc.Server;
import io.grpc.health.v1.HealthCheckResponse.ServingStatus;
import io.grpc.protobuf.services.HealthStatusManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.SmartLifecycle;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Starts the gRPC server once the context is ready and stops it gracefully first on shutdown
 * (SIGTERM in Kubernetes): health NOT_SERVING, no new calls, running calls finish within
 * app.grpc.server.shutdown-grace-period, then the rest is cancelled.
 */
public class GrpcServerServiceLifecycleImpl implements SmartLifecycle {

    private static final Logger logger = LoggerFactory.getLogger(GrpcServerServiceLifecycleImpl.class);

    private static final AtomicInteger serverCounter = new AtomicInteger(-1);

    private volatile Server server;
    private final GrpcServerService grpcServerService;

    public GrpcServerServiceLifecycleImpl(GrpcServerService grpcServerService) {
        this.grpcServerService = grpcServerService;
    }

    @Override
    public void start() {
        try {
            startGrpcServer();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void stop() {
        stopGrpcServer();
    }

    @Override
    public boolean isRunning() {
        return this.server != null && !this.server.isShutdown();
    }

    @Override
    public int getPhase() {
        return Integer.MAX_VALUE;
    }

    @Override
    public void stop(Runnable callback) {
        this.stop();
        callback.run();
    }

    protected void startGrpcServer() throws IOException {
        var localServer = this.server;
        if (localServer == null) {
            this.server = grpcServerService.createServer();
            this.server.start();
            grpcServerService.health().setStatus(HealthStatusManager.SERVICE_NAME_ALL_SERVICES, ServingStatus.SERVING);

            logger.info("gRPC Server started, listening on address: {}, port: {}", grpcServerService.getAddress(), grpcServerService.getPort());

            var awaitThread = new Thread("container-" + (serverCounter.incrementAndGet())) {

                @Override
                public void run() {
                    try {
                        GrpcServerServiceLifecycleImpl.this.server.awaitTermination();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            };
            awaitThread.setDaemon(false);
            awaitThread.start();
        }
    }

    protected void stopGrpcServer() {
        var localServer = this.server;
        if (localServer == null) {
            return;
        }
        grpcServerService.health().enterTerminalState();
        localServer.shutdown();
        var grace = grpcServerService.shutdownGracePeriod();
        try {
            if (!localServer.awaitTermination(grace.toMillis(), TimeUnit.MILLISECONDS)) {
                logger.warn("gRPC calls still running after {}, cancelling them", grace);
                localServer.shutdownNow();
                localServer.awaitTermination(5, TimeUnit.SECONDS);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            localServer.shutdownNow();
        }
        this.server = null;
        logger.info("gRPC server was stopped");
    }
}

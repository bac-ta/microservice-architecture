package com.entropy.grpcserver.services.impls;

import com.entropy.grpcserver.services.GrpcServerService;
import io.grpc.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.SmartLifecycle;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

public class GrpcServerServiceLifecycleImpl implements SmartLifecycle {

    private static final Logger logger = LoggerFactory.getLogger(GrpcServerServiceLifecycleImpl.class);

    private static AtomicInteger serverCounter = new AtomicInteger(-1);

    private volatile Server server;
    private volatile int phase = Integer.MAX_VALUE;
    private final GrpcServerService grpcServerService;

    public GrpcServerServiceLifecycleImpl(GrpcServerService grpcServerService) {
        this.grpcServerService = grpcServerService;
        logger.info("constructor");
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
        return this.phase;
    }

    @Override
    public boolean isAutoStartup() {
        return true;
    }

    @Override
    public void stop(Runnable callback) {
        this.stop();
        callback.run();
    }

    protected void startGrpcServer() throws IOException {
        Server localServer = this.server;
        if (localServer == null) {
            this.server = grpcServerService.createServer();
            this.server.start();

            logger.info("gRPC Server started, listening on address: {}, port: {}", grpcServerService.getAddress(), grpcServerService.getPort());

            Thread awaitThread = new Thread(
                    "container-" + (serverCounter.incrementAndGet())) {

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
        Server localServer = this.server;
        if (localServer != null) {
            localServer.shutdown();
            this.server = null;
            logger.info("gRPC server stopped");
        }
    }
}

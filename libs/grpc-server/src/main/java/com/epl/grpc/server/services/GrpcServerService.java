package com.epl.grpc.server.services;

import com.epl.grpc.server.models.GrpcDefinition;
import io.grpc.Server;
import io.grpc.protobuf.services.HealthStatusManager;

import java.time.Duration;

public interface GrpcServerService {
    Server createServer();

    void addService(GrpcDefinition service);

    /** Backs the standard grpc.health.v1.Health service of the server (Kubernetes gRPC probes). */
    HealthStatusManager health();

    Duration shutdownGracePeriod();

    String getAddress();

    int getPort();
}

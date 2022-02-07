package com.entropy.grpcserver.services;

import com.entropy.grpcserver.models.GrpcDefinition;
import io.grpc.Server;

public interface GrpcServerService {
    Server createServer();

    void addService(GrpcDefinition service);

    String getAddress();

    int getPort();
}

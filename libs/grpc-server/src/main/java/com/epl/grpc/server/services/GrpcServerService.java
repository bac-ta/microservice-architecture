package com.epl.grpc.server.services;

import com.epl.grpc.server.models.GrpcDefinition;
import io.grpc.Server;

public interface GrpcServerService {
    Server createServer();

    void addService(GrpcDefinition service);

    String getAddress();

    int getPort();
}

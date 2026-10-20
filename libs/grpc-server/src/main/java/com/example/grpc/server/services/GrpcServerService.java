package com.example.grpc.server.services;

import com.example.grpc.server.models.GrpcDefinition;
import io.grpc.Server;

public interface GrpcServerService {
    Server createServer();

    void addService(GrpcDefinition service);

    String getAddress();

    int getPort();
}

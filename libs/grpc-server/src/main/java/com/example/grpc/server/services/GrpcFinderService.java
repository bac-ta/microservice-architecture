package com.example.grpc.server.services;


import com.example.grpc.server.models.GrpcDefinition;

import java.util.Collection;

public interface GrpcFinderService {
    Collection<GrpcDefinition> findGrpcServices();
}

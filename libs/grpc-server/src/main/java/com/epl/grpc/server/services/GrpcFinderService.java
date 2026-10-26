package com.epl.grpc.server.services;


import com.epl.grpc.server.models.GrpcDefinition;

import java.util.Collection;

public interface GrpcFinderService {
    Collection<GrpcDefinition> findGrpcServices();
}

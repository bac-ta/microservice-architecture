package com.entropy.grpcserver.services;

import com.entropy.grpcserver.models.GrpcDefinition;

import java.util.Collection;

public interface GrpcFinderService {
    Collection<GrpcDefinition> findGrpcServices();
}

package com.entropy.grpcserver.models;

import io.grpc.ServerServiceDefinition;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class GrpcDefinition {
    private final String beanName;
    private final Class<?> beanClass;
    private final ServerServiceDefinition definition;

    public String info() {
        return "service:" + definition.getServiceDescriptor().getName() +
                ", class:" + beanClass.getClass().getName() +
                ", beanName:'" + beanName;
    }
}

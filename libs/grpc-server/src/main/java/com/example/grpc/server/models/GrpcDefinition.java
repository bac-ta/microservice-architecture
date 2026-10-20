package com.example.grpc.server.models;

import io.grpc.ServerServiceDefinition;

public record GrpcDefinition(String beanName, Class<?> beanClass, ServerServiceDefinition definition) {
    @Override
    public String toString() {
        return "service:" + definition.getServiceDescriptor().getName() +
                ", class:" + beanClass.getName() +
                ", beanName:'" + beanName;
    }
}

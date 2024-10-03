package com.example.grpc.server.configurations;

import com.example.grpc.server.models.GrpcServerInterceptorContext;
import com.example.grpc.server.models.GrpcServerProperties;
import com.example.grpc.server.services.GrpcFinderService;
import com.example.grpc.server.services.GrpcServerService;
import com.example.grpc.server.services.impls.GrpcFinderServiceImpl;
import com.example.grpc.server.services.impls.GrpcServerServiceImpl;
import com.example.grpc.server.services.impls.GrpcServerServiceLifecycleImpl;
import io.grpc.Server;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties
@ConditionalOnClass({Server.class, GrpcServerService.class})
public class GrpcServerConfiguration {

    @ConditionalOnMissingBean
    @Bean
    public GrpcServerProperties grpcServerProperties() {
        return new GrpcServerProperties();
    }

    @Bean
    public GrpcServerInterceptorContext globalServerInterceptorContext() {
        return new GrpcServerInterceptorContext();
    }

    @ConditionalOnMissingBean
    @Bean
    public GrpcFinderService defaultGrpcServiceFinder() {
        return new GrpcFinderServiceImpl();
    }

    @ConditionalOnMissingBean
    @Bean
    public GrpcServerService defaultGrpcServerFactory(GrpcServerProperties properties,
                                                      GrpcFinderService discover) {
        var grpcServerService = new GrpcServerServiceImpl(properties);
        discover.findGrpcServices().forEach(grpcServerService::addService);
        return grpcServerService;
    }

    @ConditionalOnMissingBean
    @Bean
    public GrpcServerServiceLifecycleImpl grpcServerLifecycle(GrpcServerService grpcServerService) {
        return new GrpcServerServiceLifecycleImpl(grpcServerService);
    }
}

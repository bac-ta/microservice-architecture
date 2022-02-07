package com.entropy.grpcserver.configurations;

import com.entropy.grpcserver.models.GrpcDefinition;
import com.entropy.grpcserver.models.GrpcServerInterceptorContext;
import com.entropy.grpcserver.services.GrpcFinderService;
import com.entropy.grpcserver.services.GrpcServerService;
import com.entropy.grpcserver.services.impls.GrpcFinderServiceImpl;
import com.entropy.grpcserver.services.impls.GrpcServerServiceImpl;
import com.entropy.grpcserver.services.impls.GrpcServerServiceLifecycleImpl;
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
        GrpcServerService grpcServerService = new GrpcServerServiceImpl(properties);
        for (GrpcDefinition definition : discover.findGrpcServices()) {
            grpcServerService.addService(definition);
        }
        return grpcServerService;
    }

    @ConditionalOnMissingBean
    @Bean
    public GrpcServerServiceLifecycleImpl grpcServerLifecycle(GrpcServerService grpcServerService) {
        return new GrpcServerServiceLifecycleImpl(grpcServerService);
    }
}

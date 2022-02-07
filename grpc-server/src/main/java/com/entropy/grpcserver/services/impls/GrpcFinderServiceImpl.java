package com.entropy.grpcserver.services.impls;

import com.entropy.grpcserver.models.GrpcDefinition;
import com.entropy.grpcserver.models.GrpcServerInterceptorContext;
import com.entropy.grpcserver.models.annotations.GrpcServer;
import com.entropy.grpcserver.services.GrpcFinderService;
import io.grpc.BindableService;
import io.grpc.ServerInterceptor;
import io.grpc.ServerInterceptors;
import io.grpc.ServerServiceDefinition;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class GrpcFinderServiceImpl implements GrpcFinderService, ApplicationContextAware {
    private static final Logger logger = LoggerFactory.getLogger(GrpcFinderServiceImpl.class);
    @Setter
    private ApplicationContext applicationContext;

    @Override
    public Collection<GrpcDefinition> findGrpcServices() {
        GrpcServerInterceptorContext context =
                applicationContext.getBean(GrpcServerInterceptorContext.class);
        Collection<ServerInterceptor> globalInterceptors = context.getServerInterceptors();

        return Stream.of(applicationContext.getBeanNamesForAnnotation(GrpcServer.class))
                .map(beanName -> createGrpcServiceDefinition(globalInterceptors, beanName))
                .collect(Collectors.toList());
    }

    private GrpcDefinition createGrpcServiceDefinition(
            Collection<ServerInterceptor> globalInterceptors, String beanName) {

        BindableService bindableService =
                applicationContext.getBean(beanName, BindableService.class);
        ServerServiceDefinition definition = bindableService.bindService();

        GrpcServer grpcServer =
                applicationContext.findAnnotationOnBean(beanName, GrpcServer.class);
        definition = bindInterceptors(definition, Objects.requireNonNull(grpcServer), globalInterceptors);

        GrpcDefinition result =
                new GrpcDefinition(beanName, bindableService.getClass(), definition);
        logger.info("Found gRPC service: {}", result.info());
        return result;
    }

    private ServerServiceDefinition bindInterceptors(ServerServiceDefinition definition,
                                                     GrpcServer grpcServer,
                                                     Collection<ServerInterceptor> globalInterceptors) {

        List<ServerInterceptor> allInterceptors = new ArrayList<>();
        allInterceptors.addAll(globalInterceptors);
        allInterceptors.addAll(createGrpcServiceInterceptors(grpcServer.interceptors()));

        return ServerInterceptors.intercept(definition, allInterceptors);
    }

    @SafeVarargs
    private final List<ServerInterceptor> createGrpcServiceInterceptors(
            Class<? extends ServerInterceptor>... interceptors) {

        return Stream.of(interceptors)
                .map(interceptorClass -> {
                    if (applicationContext.getBeanNamesForType(interceptorClass).length > 0)
                        return applicationContext.getBean(interceptorClass);
                    else {
                        try {
                            return interceptorClass.newInstance();
                        } catch (Exception e) {
                            throw new BeanCreationException(
                                    "Failed to create interceptor instance.", e);
                        }
                    }
                })
                .collect(Collectors.toList());
    }
}

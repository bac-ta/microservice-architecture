package com.example.grpc.client.services.impl;

import com.example.grpc.client.models.annotations.GrpcClient;
import com.example.grpc.client.services.GrpcChannelService;
import io.grpc.ClientInterceptor;
import lombok.NoArgsConstructor;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.ReflectionUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@NoArgsConstructor
public class GrpcClientCreatorImpl implements BeanPostProcessor {

    private final Map<String, ArrayList<Class>> beansToProcess = new HashMap<>();

    @Autowired
    private DefaultListableBeanFactory beanFactory;

    @Autowired
    private GrpcChannelService channelFactory;


    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName)
            throws BeansException {

        var clazz = bean.getClass();
        do {
            for (var field : clazz.getDeclaredFields()) {
                if (field.isAnnotationPresent(GrpcClient.class)) {
                    if (!beansToProcess.containsKey(beanName)) {
                        beansToProcess.put(beanName, new ArrayList<>());
                    }
                    beansToProcess.get(beanName).add(clazz);
                }
            }
            clazz = clazz.getSuperclass();
        } while (clazz != null);

        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName)
            throws BeansException {

        if (!beansToProcess.containsKey(beanName))
            return bean;

        for (var clazz : beansToProcess.get(beanName)) {
            for (var field : clazz.getDeclaredFields()) {
                var grpcClient = AnnotationUtils.getAnnotation(field, GrpcClient.class);

                if (grpcClient == null)
                    continue;

                var channel = channelFactory.createChannel(grpcClient.value(),
                        createInterceptors(grpcClient));
                ReflectionUtils.makeAccessible(field);
                ReflectionUtils.setField(field, bean, channel);

            }
        }

        return bean;
    }

    private List<ClientInterceptor> createInterceptors(GrpcClient grpcClient) {
        return Stream.of(grpcClient.interceptors())
                .map(aClass -> {
                    if (beanFactory.getBeanNamesForType(ClientInterceptor.class).length > 0)
                        return beanFactory.getBean(aClass);

                    try {
                        return aClass.getDeclaredConstructor().newInstance();
                    } catch (Exception e) {
                        throw new BeanCreationException("Failed to create interceptor", e);
                    }

                })
                .collect(Collectors.toList());
    }

}

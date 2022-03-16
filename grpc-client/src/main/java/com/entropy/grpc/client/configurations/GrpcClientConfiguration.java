package com.entropy.grpc.client.configurations;

import com.entropy.grpc.client.models.ClientInterceptorContext;
import com.entropy.grpc.client.models.annotations.GrpcClient;
import com.entropy.grpc.client.services.GrpcChannelService;
import com.entropy.grpc.client.services.impl.AddressChannelFactoryImpl;
import com.entropy.grpc.client.services.impl.DiscoveryClientChannelProviderImpl;
import com.entropy.grpc.client.services.impl.GrpcClientCreatorImpl;
import io.grpc.Channel;
import io.grpc.LoadBalancer;
import io.grpc.util.RoundRobinLoadBalancerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties
@ConditionalOnClass({Channel.class})
public class GrpcClientConfiguration {

    @ConditionalOnMissingBean
    @Bean
    public GrpcChannelPropertiesComponent grpcChannelsProperties() {
        return new GrpcChannelPropertiesComponent();
    }

    @Bean
    public ClientInterceptorContext globalClientInterceptorContext() {
        return new ClientInterceptorContext();
    }

    @ConditionalOnMissingBean
    @Bean
    public LoadBalancer.Factory grpcLoadBalancerFactory() {
        return RoundRobinLoadBalancerFactory.getInstance();
    }

    @ConditionalOnMissingBean(value = GrpcChannelService.class, type = "org.springframework.cloud.client.discovery.DiscoveryClient")
    @Bean
    public GrpcChannelService addressChannelFactory(GrpcChannelPropertiesComponent properties, LoadBalancer.Factory loadBalancerFactory, ClientInterceptorContext interceptorContext) {
        return new AddressChannelFactoryImpl(properties, loadBalancerFactory, interceptorContext);
    }

    @ConditionalOnClass(GrpcClient.class)
    @Bean
    public GrpcClientCreatorImpl grpcClientBeanPostProcessor() {
        return new GrpcClientCreatorImpl();
    }

    @Configuration
    @ConditionalOnBean(DiscoveryClient.class)
    protected static class DiscoveryGrpcClientAutoConfiguration {

        @ConditionalOnMissingBean
        @Bean
        public GrpcChannelService discoveryClientChannelFactory(GrpcChannelPropertiesComponent properties, DiscoveryClient discoveryClient, LoadBalancer.Factory loadBalancerFactory, ClientInterceptorContext globalClientInterceptorRegistry) {
            return new DiscoveryClientChannelProviderImpl(properties, discoveryClient, loadBalancerFactory, globalClientInterceptorRegistry);
        }
    }

}

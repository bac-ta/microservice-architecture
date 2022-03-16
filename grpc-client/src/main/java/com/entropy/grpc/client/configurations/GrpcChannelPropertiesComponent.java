package com.entropy.grpc.client.configurations;

import com.entropy.grpc.client.models.GrpcChannelProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@ConfigurationProperties("grpc.client")
public class GrpcChannelPropertiesComponent {

    @NestedConfigurationProperty
    private Map<String, GrpcChannelProperties> channels = new HashMap<>();

    public GrpcChannelProperties getChannel(String name) {
        GrpcChannelProperties channel = channels.get(name);
        if (channel == null) {
            return GrpcChannelProperties.DEFAULT_CHANNEL;
        }
        return channel;
    }

    public Map<String, GrpcChannelProperties> getChannels() {
        return channels;
    }

    public void setChannels(Map<String, GrpcChannelProperties> channels) {
        this.channels = channels;
    }

    @Override
    public String toString() {
        return "GrpcChannelPropertiesComponent{" +
                "channels=" + channels +
                '}';
    }
}

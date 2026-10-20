package com.example.grpc.client.configurations;

import com.example.grpc.client.models.GrpcChannelProperties;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Setter
@Getter
@Component
@ConfigurationProperties("app.grpc.client")
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

    @Override
    public String toString() {
        return "GrpcChannelPropertiesComponent{" +
                "channels=" + channels +
                '}';
    }
}

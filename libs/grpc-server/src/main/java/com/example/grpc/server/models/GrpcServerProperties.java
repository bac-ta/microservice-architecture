package com.example.grpc.server.models;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Component
@ConfigurationProperties("app.grpc.server")
public class GrpcServerProperties {
    @Setter
    @Getter
    public static class Security {

        private Boolean enabled = false;

        private String certificateChainPath = "";

        private String certificatePath = "";

    }

    @Setter
    @Value("${app.grpc.server.port}")
    private int port;

    @Setter
    @Value("${app.grpc.server.address}")
    private String address;

    private final Security security = new Security();

}

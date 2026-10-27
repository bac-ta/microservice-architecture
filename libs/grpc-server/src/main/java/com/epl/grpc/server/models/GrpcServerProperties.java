package com.epl.grpc.server.models;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.unit.DataSize;

import java.time.Duration;

@Getter
@Setter
@ConfigurationProperties("app.grpc.server")
public class GrpcServerProperties {

    @Setter
    @Getter
    public static class Security {

        private Boolean enabled = false;

        private String certificateChainPath = "";

        private String certificatePath = "";

    }

    /** false (e.g. the "migrate" profile) builds the context without serving. */
    private boolean enabled = true;

    private String address = "0.0.0.0";

    private int port = 9090;

    /** Largest request message accepted; bigger ones fail with RESOURCE_EXHAUSTED. */
    private DataSize maxInboundMessageSize = DataSize.ofMegabytes(4);

    /** On shutdown: health turns NOT_SERVING, new calls are refused and running calls get this long to finish. */
    private Duration shutdownGracePeriod = Duration.ofSeconds(20);

    /** gRPC server reflection (grpcurl, Postman); keep off outside development. */
    private boolean reflectionEnabled = false;

    private final Security security = new Security();

}

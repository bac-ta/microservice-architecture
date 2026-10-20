package com.example.epl.inbound;

import com.example.grpc.server.configurations.GrpcServerConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

/**
 * Inbound: arrival plan, inspection, put-away, confirmation (legacy GwhSrvcAp/Ab/AhWeb).
 */
@SpringBootApplication
// libs/grpc-server is not auto-configured: import it so the gRPC server starts with this service.
@Import(GrpcServerConfiguration.class)
public class InboundServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(InboundServiceApplication.class, args);
    }
}

package com.epl.outbound;

import com.epl.grpc.server.configurations.GrpcServerConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

/**
 * Outbound: shipping orders, allocation, picking, packing, confirmation.
 */
@SpringBootApplication
// libs/grpc-server is not auto-configured: import it so the gRPC server starts with this service.
@Import(GrpcServerConfiguration.class)
public class OutboundServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(OutboundServiceApplication.class, args);
    }
}

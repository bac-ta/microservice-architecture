package com.example.epl.masterdata;

import com.example.grpc.server.configurations.GrpcServerConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

/**
 * Master data: locations, products, parties, scoped configuration.
 */
@SpringBootApplication
// libs/grpc-server is not auto-configured: import it so the gRPC server starts with this service.
@Import(GrpcServerConfiguration.class)
public class MasterdataServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(MasterdataServiceApplication.class, args);
    }
}

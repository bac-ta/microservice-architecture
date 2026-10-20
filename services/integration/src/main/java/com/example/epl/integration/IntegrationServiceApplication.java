package com.example.epl.integration;

import com.example.grpc.server.configurations.GrpcServerConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

/**
 * Integration: file uploads, EDI, batch jobs, data purge (legacy lpupl/lpedi, GwhSrvcUpWeb, GwhSrvcDpWeb).
 */
@SpringBootApplication
// libs/grpc-server is not auto-configured: import it so the gRPC server starts with this service.
@Import(GrpcServerConfiguration.class)
public class IntegrationServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(IntegrationServiceApplication.class, args);
    }
}

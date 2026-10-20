package com.example.epl.inventory;

import com.example.grpc.server.configurations.GrpcServerConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

/**
 * Inventory: stock, movements, stocktake, serials, kitting (legacy GwhSrvcLp/Lb/LhWeb).
 */
@SpringBootApplication
// libs/grpc-server is not auto-configured: import it so the gRPC server starts with this service.
@Import(GrpcServerConfiguration.class)
public class InventoryServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(InventoryServiceApplication.class, args);
    }
}

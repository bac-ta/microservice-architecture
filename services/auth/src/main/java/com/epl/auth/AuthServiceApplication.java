package com.epl.auth;

import com.epl.grpc.server.configurations.GrpcServerConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.Import;

/**
 * IAM: login / JWT, users and tenant grants, tenant registry, ABAC authorization.
 */
@SpringBootApplication
@ConfigurationPropertiesScan
// libs/grpc-server is not auto-configured: import it so the gRPC server starts with this service.
@Import(GrpcServerConfiguration.class)
public class AuthServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuthServiceApplication.class, args);
    }
}

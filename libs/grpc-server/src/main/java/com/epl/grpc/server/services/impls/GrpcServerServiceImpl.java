package com.epl.grpc.server.services.impls;

import com.epl.grpc.server.models.GrpcServerProperties;
import com.epl.grpc.server.models.GrpcDefinition;
import com.epl.grpc.server.services.GrpcServerService;
import com.google.common.net.InetAddresses;
import io.grpc.Server;
import io.grpc.netty.NettyServerBuilder;
import io.grpc.protobuf.services.HealthStatusManager;
import io.grpc.protobuf.services.ProtoReflectionServiceV1;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class GrpcServerServiceImpl implements GrpcServerService {
    private static final Log logger = LogFactory.getLog(GrpcServerServiceImpl.class);

    private final GrpcServerProperties properties;
    private final List<GrpcDefinition> definitions = new ArrayList<>();
    private final HealthStatusManager health = new HealthStatusManager();

    public GrpcServerServiceImpl(GrpcServerProperties properties) {
        this.properties = properties;
    }

    @Override
    public Server createServer() {
        final var address = properties.getAddress();
        final var port = properties.getPort();
        final var security = properties.getSecurity();

        var builder = NettyServerBuilder.forAddress(new InetSocketAddress(InetAddresses.forString(address), port))
                .maxInboundMessageSize((int) properties.getMaxInboundMessageSize().toBytes());

        definitions.forEach(definition -> {
            logger.info("Registered gRPC " + definition);
            builder.addService(definition.definition());
        });

        // not intercepted: probes and tooling carry no tenant metadata
        builder.addService(health.getHealthService());
        if (properties.isReflectionEnabled()) {
            builder.addService(ProtoReflectionServiceV1.newInstance());
        }

        if (security.getEnabled()) {
            var certificateChain = new File(security.getCertificateChainPath());
            var certificate = new File(security.getCertificatePath());
            builder.useTransportSecurity(certificateChain, certificate);
        }
        return builder.build();
    }

    @Override
    public void addService(GrpcDefinition definition) {
        definitions.add(definition);
    }

    @Override
    public HealthStatusManager health() {
        return health;
    }

    @Override
    public Duration shutdownGracePeriod() {
        return properties.getShutdownGracePeriod();
    }

    @Override
    public String getAddress() {
        return properties.getAddress();
    }

    @Override
    public int getPort() {
        return properties.getPort();
    }
}

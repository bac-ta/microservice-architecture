package com.entropy.grpcserver.services.impls;

import com.entropy.grpcserver.configurations.GrpcServerProperties;
import com.entropy.grpcserver.models.GrpcDefinition;
import com.entropy.grpcserver.services.GrpcServerService;
import com.google.common.net.InetAddresses;
import io.grpc.Server;
import io.grpc.netty.NettyServerBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

public class GrpcServerServiceImpl implements GrpcServerService {
    private static final Log logger = LogFactory.getLog(GrpcServerServiceImpl.class);

    private final GrpcServerProperties properties;
    private final List<GrpcDefinition> definitions = new ArrayList<>();

    public GrpcServerServiceImpl(GrpcServerProperties properties) {
        this.properties = properties;
    }

    @Override
    public Server createServer() {
        final String address = properties.getAddress();
        final int port = properties.getPort();
        final GrpcServerProperties.Security security = properties.getSecurity();

        NettyServerBuilder builder = NettyServerBuilder.forAddress(
                new InetSocketAddress(InetAddresses.forString(address), port));
        definitions.forEach(definition -> {
            logger.info("Registered gRPC " + definition.info());
            builder.addService(definition.getDefinition());
        });

        if (security.getEnabled()) {
            File certificateChain = new File(security.getCertificateChainPath());
            File certificate = new File(security.getCertificatePath());
            builder.useTransportSecurity(certificateChain, certificate);
        }
        return builder.build();
    }

    @Override
    public void addService(GrpcDefinition definition) {
        definitions.add(definition);
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

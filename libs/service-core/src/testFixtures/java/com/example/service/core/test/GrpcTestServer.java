package com.example.service.core.test;

import com.example.grpc.server.services.GrpcFinderService;
import io.grpc.Channel;
import io.grpc.ClientInterceptors;
import io.grpc.ManagedChannel;
import io.grpc.Metadata;
import io.grpc.Server;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import io.grpc.stub.MetadataUtils;
import jakarta.annotation.PreDestroy;

import java.io.IOException;
import java.util.UUID;

/**
 * The service's @GrpcServer beans with their interceptors on an in-process server, so a test calls
 * them exactly like the gateway does, metadata included.
 */
public class GrpcTestServer {

    private final Server server;
    private final ManagedChannel channel;

    public GrpcTestServer(GrpcFinderService finder) throws IOException {
        var name = "test-" + UUID.randomUUID();
        var builder = InProcessServerBuilder.forName(name).directExecutor();
        finder.findGrpcServices().forEach(definition -> builder.addService(definition.definition()));
        this.server = builder.build().start();
        this.channel = InProcessChannelBuilder.forName(name).directExecutor().build();
    }

    /** Channel for the default tenant C001 / WH01 / CUST01 as user "tester". */
    public Channel tenantChannel() {
        return channel(EplTestDatabase.COMPANY, EplTestDatabase.WAREHOUSE, EplTestDatabase.CUSTOMER);
    }

    /** Channel sending x-company / x-warehouse / x-customer (null = not sent) and x-user. */
    public Channel channel(String company, String warehouse, String customer) {
        var headers = new Metadata();
        headers.put(key("x-user"), "tester");
        if (company != null) {
            headers.put(key("x-company"), company);
        }
        if (warehouse != null) {
            headers.put(key("x-warehouse"), warehouse);
        }
        if (customer != null) {
            headers.put(key("x-customer"), customer);
        }
        return ClientInterceptors.intercept(channel, MetadataUtils.newAttachHeadersInterceptor(headers));
    }

    @PreDestroy
    public void shutdown() {
        channel.shutdownNow();
        server.shutdownNow();
    }

    private static Metadata.Key<String> key(String name) {
        return Metadata.Key.of(name, Metadata.ASCII_STRING_MARSHALLER);
    }
}

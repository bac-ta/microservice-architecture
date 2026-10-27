package com.epl.service.core;

import com.epl.auth.v1.ResolveTenantRequest;
import com.epl.auth.v1.ResolvedTenant;
import com.epl.auth.v1.TenantServiceGrpc;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.grpc.ManagedChannel;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.netty.NettyChannelBuilder;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * {@link TenantResolver} backed by the auth service; ids never change, so they are cached.
 * Unknown codes (NOT_FOUND) are remembered briefly, so a client repeating bad headers does not
 * reach the auth service on every call.
 */
public class GrpcTenantResolver implements TenantResolver {

    private static final Logger logger = LoggerFactory.getLogger(GrpcTenantResolver.class);

    private final ManagedChannel channel;
    private final TenantServiceGrpc.TenantServiceBlockingStub stub;
    private final Duration deadline;
    private static final Duration NOT_FOUND_TTL = Duration.ofSeconds(30);

    private final Cache<ResolveTenantRequest, ResolvedTenant> cache;
    private final Cache<ResolveTenantRequest, Status> notFound;

    public GrpcTenantResolver(ServiceCoreProperties.Tenancy properties) {
        logger.info("Resolving tenants with the auth service at {}", properties.authTarget());
        this.channel = NettyChannelBuilder.forTarget(properties.authTarget()).usePlaintext().build();
        this.stub = TenantServiceGrpc.newBlockingStub(channel);
        this.deadline = properties.deadline();
        this.cache = Caffeine.newBuilder()
                .maximumSize(10_000)
                .expireAfterWrite(properties.cacheTtl())
                .build();
        this.notFound = Caffeine.newBuilder()
                .maximumSize(1_000)
                .expireAfterWrite(NOT_FOUND_TTL)
                .build();
    }

    @Override
    public ResolvedTenant resolve(String companyCode, String warehouseCode, String customerCode) {
        var request = ResolveTenantRequest.newBuilder()
                .setCompanyCode(companyCode)
                .setWarehouseCode(warehouseCode == null ? "" : warehouseCode)
                .setCustomerCode(customerCode == null ? "" : customerCode)
                .build();
        var unknown = notFound.getIfPresent(request);
        if (unknown != null) {
            throw unknown.asRuntimeException();
        }
        try {
            return cache.get(request, key -> stub
                    .withDeadlineAfter(deadline.toMillis(), TimeUnit.MILLISECONDS)
                    .resolveTenant(key));
        } catch (StatusRuntimeException e) {
            if (e.getStatus().getCode() == Status.Code.NOT_FOUND) {
                notFound.put(request, e.getStatus());
            }
            throw e;
        }
    }

    @PreDestroy
    public void shutdown() throws InterruptedException {
        channel.shutdown();
        if (!channel.awaitTermination(5, TimeUnit.SECONDS)) {
            channel.shutdownNow();
        }
    }
}

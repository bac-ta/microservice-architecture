package com.epl.service.core;

import io.grpc.Context;
import io.grpc.Contexts;
import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import io.grpc.StatusRuntimeException;

import java.util.UUID;

/**
 * Builds the {@link RequestContext} of each call from its metadata and attaches it to the gRPC context.
 * Registered by {@link ServiceCoreAutoConfiguration#eplServerInterceptors}.
 * <p>
 * Trust: x-user and the tenant codes are set by the gateway after it verified the JWT; the
 * backends do not verify the JWT or the tenant grants yet, so only the gateway may reach them.
 */
public class RequestContextServerInterceptor implements ServerInterceptor {

    public static final Metadata.Key<String> USER = Metadata.Key.of("x-user", Metadata.ASCII_STRING_MARSHALLER);
    public static final Metadata.Key<String> COMPANY = Metadata.Key.of("x-company", Metadata.ASCII_STRING_MARSHALLER);
    public static final Metadata.Key<String> WAREHOUSE = Metadata.Key.of("x-warehouse", Metadata.ASCII_STRING_MARSHALLER);
    public static final Metadata.Key<String> CUSTOMER = Metadata.Key.of("x-customer", Metadata.ASCII_STRING_MARSHALLER);

    private final TenantResolver tenantResolver;

    public RequestContextServerInterceptor(TenantResolver tenantResolver) {
        this.tenantResolver = tenantResolver;
    }

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(ServerCall<ReqT, RespT> call, Metadata headers,
                                                                 ServerCallHandler<ReqT, RespT> next) {
        RequestContext requestContext;
        try {
            requestContext = build(headers);
        } catch (StatusRuntimeException e) {
            call.close(e.getStatus(), new Metadata());
            return new ServerCall.Listener<>() {
            };
        }
        return Contexts.interceptCall(requestContext.attach(Context.current()), call, headers, next);
    }

    private RequestContext build(Metadata headers) {
        var user = headers.get(USER);
        var company = blankToNull(headers.get(COMPANY));
        var warehouse = company == null ? null : blankToNull(headers.get(WAREHOUSE));
        var customer = warehouse == null ? null : blankToNull(headers.get(CUSTOMER));
        if (company == null) {
            return new RequestContext(user, null, null, null, null, null, null, null);
        }
        var ids = tenantResolver.resolve(company, warehouse, customer);
        return new RequestContext(user, company, warehouse, customer,
                uuid(ids.getCompanyId()), uuid(ids.getWarehouseId()), uuid(ids.getCustomerId()), uuid(ids.getTenantId()));
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private static UUID uuid(String value) {
        return value.isEmpty() ? null : UUID.fromString(value);
    }
}

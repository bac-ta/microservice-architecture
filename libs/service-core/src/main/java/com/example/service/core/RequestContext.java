package com.example.service.core;

import io.grpc.Context;
import io.grpc.Status;

import java.util.UUID;

/**
 * Who calls and for which tenant, built from the gRPC metadata by {@link RequestContextServerInterceptor}.
 * Codes come from x-company / x-warehouse / x-customer, ids from {@link TenantResolver}.
 */
public record RequestContext(String user,
                             String companyCode, String warehouseCode, String customerCode,
                             UUID companyId, UUID warehouseId, UUID customerId, UUID tenantId) {

    static final Context.Key<RequestContext> KEY = Context.key("epl.request-context");

    public static final RequestContext NONE = new RequestContext(null, null, null, null, null, null, null, null);

    /** The context of the current gRPC call, {@link #NONE} outside a call. */
    public static RequestContext current() {
        var context = KEY.get();
        return context == null ? NONE : context;
    }

    public Context attach(Context context) {
        return context.withValue(KEY, this);
    }

    /** Tenant-scoped data needs x-company, x-warehouse and x-customer. */
    public UUID requireTenant() {
        if (tenantId == null) {
            throw Status.FAILED_PRECONDITION
                    .withDescription("x-company, x-warehouse and x-customer metadata are required")
                    .asRuntimeException();
        }
        return tenantId;
    }

    /** Warehouse-scoped data needs x-company and x-warehouse. */
    public UUID requireWarehouse() {
        if (warehouseId == null) {
            throw Status.FAILED_PRECONDITION
                    .withDescription("x-company and x-warehouse metadata are required")
                    .asRuntimeException();
        }
        return warehouseId;
    }
}

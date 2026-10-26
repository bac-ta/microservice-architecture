package com.epl.service.core;

import com.epl.auth.v1.ResolvedTenant;

/**
 * Codes from the tenant metadata to registry ids. The default asks the auth service
 * ({@code TenantService/ResolveTenant}, cached); the auth service resolves from its own tables.
 */
@FunctionalInterface
public interface TenantResolver {

    /** @throws io.grpc.StatusRuntimeException NOT_FOUND when a code does not exist */
    ResolvedTenant resolve(String companyCode, String warehouseCode, String customerCode);
}

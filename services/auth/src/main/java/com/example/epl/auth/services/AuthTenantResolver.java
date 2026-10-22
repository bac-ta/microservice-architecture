package com.example.epl.auth.services;

import com.example.epl.auth.v1.ResolveTenantRequest;
import com.example.epl.auth.v1.ResolvedTenant;
import com.example.service.core.TenantResolver;
import org.springframework.stereotype.Component;

/** The auth service owns the registry: it resolves the tenant metadata from its own tables. */
@Component
public class AuthTenantResolver implements TenantResolver {

    private final TenantService tenantService;

    public AuthTenantResolver(TenantService tenantService) {
        this.tenantService = tenantService;
    }

    @Override
    public ResolvedTenant resolve(String companyCode, String warehouseCode, String customerCode) {
        return tenantService.resolve(ResolveTenantRequest.newBuilder()
                .setCompanyCode(companyCode)
                .setWarehouseCode(warehouseCode == null ? "" : warehouseCode)
                .setCustomerCode(customerCode == null ? "" : customerCode)
                .build());
    }
}

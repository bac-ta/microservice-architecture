package com.epl.auth.services;

import com.epl.auth.entities.CustomerEntity;
import com.epl.auth.entities.TenantEntity;
import com.epl.auth.mappers.TenantMappers;
import com.epl.auth.repositories.CustomerRepository;
import com.epl.auth.repositories.TenantRepository;
import com.epl.auth.v1.CreateTenantRequest;
import com.epl.auth.v1.ResolveTenantRequest;
import com.epl.auth.v1.ResolvedTenant;
import com.epl.auth.v1.Tenant;
import com.epl.service.core.Errors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.epl.service.core.Protos.emptyIfNull;
import static com.epl.service.core.Protos.localDate;
import static com.epl.service.core.Protos.text;
import static com.epl.service.core.Protos.uuid;

/** Tenant = customer operated in a warehouse; also resolves the tenant metadata of every service. */
@Service
public class TenantService {

    private final TenantRepository tenantRepository;
    private final CustomerRepository customerRepository;
    private final CompanyService companyService;
    private final WarehouseService warehouseService;

    public TenantService(TenantRepository tenantRepository, CustomerRepository customerRepository,
                         CompanyService companyService, WarehouseService warehouseService) {
        this.tenantRepository = tenantRepository;
        this.customerRepository = customerRepository;
        this.companyService = companyService;
        this.warehouseService = warehouseService;
    }

    @Transactional(readOnly = true)
    public List<Tenant> list(String companyCode, String warehouseCode) {
        return tenantRepository.search(Errors.required(companyCode, "company_code"), emptyIfNull(warehouseCode))
                .stream().map(TenantMappers::tenant).toList();
    }

    @Transactional(readOnly = true)
    public Tenant get(String id) {
        return TenantMappers.tenant(find(id));
    }

    /** Creates the customer too when it does not exist yet (customer_name required then). */
    @Transactional
    public Tenant create(CreateTenantRequest request) {
        if (!request.getCopyConfigFromTenantId().isBlank()) {
            throw Errors.unsupported("copy_config_from_tenant_id is not supported yet");
        }
        var warehouse = warehouseService.find(request.getCompanyCode(), request.getWarehouseCode());
        var customer = customerRepository.findByCompanyCodeAndCode(request.getCompanyCode(),
                        Errors.required(request.getCustomerCode(), "customer_code"))
                .orElseGet(() -> newCustomer(request));

        var entity = new TenantEntity();
        entity.setCompany(warehouse.getCompany());
        entity.setWarehouse(warehouse);
        entity.setCustomer(customer);
        entity.setCompanyCode(warehouse.getCompany().getCode());
        entity.setWarehouseCode(warehouse.getCode());
        entity.setCustomerCode(customer.getCode());
        entity.setStatus("ONBOARDING");
        return TenantMappers.tenant(tenantRepository.saveAndFlush(entity));
    }

    /** Only status and validity dates change; the codes are fixed. */
    @Transactional
    public Tenant update(Tenant request) {
        var entity = find(request.getId());
        Errors.checkVersion(entity.getVersion(), request.getAudit().getVersion());
        entity.setStatus(Errors.required(request.getStatus(), "status"));
        entity.setStartDate(localDate(request.getStartDate()));
        entity.setEndDate(localDate(request.getEndDate()));
        return TenantMappers.tenant(tenantRepository.saveAndFlush(entity));
    }

    @Transactional
    public void delete(String id, int version) {
        var entity = find(id);
        Errors.checkVersion(entity.getVersion(), version);
        tenantRepository.delete(entity);
        tenantRepository.flush();
    }

    @Transactional(readOnly = true)
    public ResolvedTenant resolve(ResolveTenantRequest request) {
        var builder = ResolvedTenant.newBuilder();
        var company = companyService.find(request.getCompanyCode());
        builder.setCompanyId(text(company.getId()));
        if (request.getWarehouseCode().isBlank()) {
            return builder.build();
        }
        var warehouse = warehouseService.find(request.getCompanyCode(), request.getWarehouseCode());
        builder.setWarehouseId(text(warehouse.getId()));
        if (request.getCustomerCode().isBlank()) {
            return builder.build();
        }
        var tenant = tenantRepository.findByCompanyCodeAndWarehouseCodeAndCustomerCode(
                        request.getCompanyCode(), request.getWarehouseCode(), request.getCustomerCode())
                .orElseThrow(() -> Errors.notFound("Tenant",
                        request.getCompanyCode() + "/" + request.getWarehouseCode() + "/" + request.getCustomerCode()));
        return builder
                .setCustomerId(text(tenant.getCustomer().getId()))
                .setTenantId(text(tenant.getId()))
                .build();
    }

    private TenantEntity find(String id) {
        return tenantRepository.findWithCustomerById(uuid(id, "id"))
                .orElseThrow(() -> Errors.notFound("Tenant", id));
    }

    private CustomerEntity newCustomer(CreateTenantRequest request) {
        var customer = new CustomerEntity();
        customer.setCompany(companyService.find(request.getCompanyCode()));
        customer.setCode(request.getCustomerCode());
        customer.setName(Errors.required(request.getCustomerName(), "customer_name"));
        customer.setStatus("ACTIVE");
        return customerRepository.save(customer);
    }
}

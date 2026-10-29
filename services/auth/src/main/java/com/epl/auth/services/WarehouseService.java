package com.epl.auth.services;

import com.epl.auth.entities.WarehouseEntity;
import com.epl.auth.mappers.TenantMappers;
import com.epl.auth.repositories.WarehouseRepository;
import com.epl.auth.v1.Warehouse;
import com.epl.service.core.Errors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.epl.service.core.Protos.localDate;

@RequiredArgsConstructor
@Service
public class WarehouseService {

    private final WarehouseRepository warehouseRepository;
    private final CompanyService companyService;

    @Transactional(readOnly = true)
    public List<Warehouse> list(String companyCode) {
        return warehouseRepository.findAllByCompanyCodeOrderByCode(Errors.required(companyCode, "company_code"))
                .stream().map(TenantMappers::warehouse).toList();
    }

    @Transactional(readOnly = true)
    public Warehouse get(String companyCode, String code) {
        return TenantMappers.warehouse(find(companyCode, code));
    }

    @Transactional
    public Warehouse create(Warehouse request) {
        var entity = new WarehouseEntity();
        entity.setCompany(companyService.find(request.getCompanyCode()));
        entity.setCode(Errors.required(request.getCode(), "code"));
        apply(entity, request);
        return TenantMappers.warehouse(warehouseRepository.saveAndFlush(entity));
    }

    @Transactional
    public Warehouse update(Warehouse request) {
        var entity = find(request.getCompanyCode(), request.getCode());
        Errors.checkVersion(entity.getVersion(), request.getAudit().getVersion());
        apply(entity, request);
        return TenantMappers.warehouse(warehouseRepository.saveAndFlush(entity));
    }

    @Transactional
    public void delete(String companyCode, String code, int version) {
        var entity = find(companyCode, code);
        Errors.checkVersion(entity.getVersion(), version);
        warehouseRepository.delete(entity);
        warehouseRepository.flush();
    }

    WarehouseEntity find(String companyCode, String code) {
        return warehouseRepository.findByCompanyCodeAndCode(Errors.required(companyCode, "company_code"),
                        Errors.required(code, "warehouse_code"))
                .orElseThrow(() -> Errors.notFound("Warehouse", companyCode + "/" + code));
    }

    private static void apply(WarehouseEntity entity, Warehouse request) {
        entity.setName(Errors.required(request.getName(), "name"));
        entity.setTimeZone(Errors.required(request.getTimeZone(), "time_zone"));
        entity.setStartDate(localDate(request.getStartDate()));
        entity.setEndDate(localDate(request.getEndDate()));
        entity.setStatus(request.getStatus().isBlank() ? "ACTIVE" : request.getStatus());
    }
}

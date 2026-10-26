package com.epl.masterdata.services;

import com.epl.masterdata.entities.SubInventoryEntity;
import com.epl.masterdata.entities.TenantCodeId;
import com.epl.masterdata.mappers.MasterdataMappers;
import com.epl.masterdata.repositories.SubInventoryRepository;
import com.epl.masterdata.v1.SubInventory;
import com.epl.service.core.Errors;
import com.epl.service.core.RequestContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

/** Sub-inventories (stock grades) of the tenant. The table has no version: last write wins. */
@Service
public class SubInventoryService {

    private final SubInventoryRepository subInventoryRepository;

    public SubInventoryService(SubInventoryRepository subInventoryRepository) {
        this.subInventoryRepository = subInventoryRepository;
    }

    @Transactional(readOnly = true)
    public List<SubInventory> list() {
        return subInventoryRepository.findAllByTenantIdAndDeletedAtIsNullOrderByCode(RequestContext.current().requireTenant())
                .stream().map(MasterdataMappers::subInventory).toList();
    }

    /** Making one the default clears the flag on the others. */
    @Transactional
    public SubInventory upsert(SubInventory request) {
        var tenantId = RequestContext.current().requireTenant();
        var code = Errors.required(request.getCode(), "code");
        if (request.getIsDefault()) {
            subInventoryRepository.findAllByTenantIdAndDeletedAtIsNullOrderByCode(tenantId).stream()
                    .filter(other -> other.isDefaultSubInventory() && !other.getCode().equals(code))
                    .forEach(other -> other.setDefaultSubInventory(false));
            subInventoryRepository.flush();
        }
        var entity = subInventoryRepository.findById(new TenantCodeId(tenantId, code)).orElseGet(() -> {
            var created = new SubInventoryEntity();
            created.setTenantId(tenantId);
            created.setCode(code);
            return created;
        });
        entity.setName(Errors.required(request.getName(), "name"));
        entity.setDefaultSubInventory(request.getIsDefault());
        entity.setDeletedAt(null);
        return MasterdataMappers.subInventory(subInventoryRepository.saveAndFlush(entity));
    }

    @Transactional
    public void delete(String code) {
        var tenantId = RequestContext.current().requireTenant();
        var entity = subInventoryRepository.findById(new TenantCodeId(tenantId, Errors.required(code, "code")))
                .filter(found -> found.getDeletedAt() == null)
                .orElseThrow(() -> Errors.notFound("Sub-inventory", code));
        entity.setDeletedAt(Instant.now());
        entity.setDefaultSubInventory(false);
    }
}

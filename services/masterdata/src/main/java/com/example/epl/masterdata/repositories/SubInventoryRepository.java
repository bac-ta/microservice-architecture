package com.example.epl.masterdata.repositories;

import com.example.epl.masterdata.entities.SubInventoryEntity;
import com.example.epl.masterdata.entities.TenantCodeId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SubInventoryRepository extends JpaRepository<SubInventoryEntity, TenantCodeId> {

    List<SubInventoryEntity> findAllByTenantIdAndDeletedAtIsNullOrderByCode(UUID tenantId);
}

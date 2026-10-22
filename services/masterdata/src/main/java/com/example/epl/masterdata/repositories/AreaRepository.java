package com.example.epl.masterdata.repositories;

import com.example.epl.masterdata.entities.AreaEntity;
import com.example.epl.masterdata.entities.WarehouseCodeId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AreaRepository extends JpaRepository<AreaEntity, WarehouseCodeId> {

    Optional<AreaEntity> findByWarehouseIdAndCodeAndDeletedAtIsNull(UUID warehouseId, String code);

    Page<AreaEntity> findAllByWarehouseIdAndDeletedAtIsNull(UUID warehouseId, Pageable pageable);
}

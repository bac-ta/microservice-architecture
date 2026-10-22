package com.example.epl.masterdata.repositories;

import com.example.epl.masterdata.entities.LocationEntity;
import com.example.epl.masterdata.entities.WarehouseCodeId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface LocationRepository extends JpaRepository<LocationEntity, WarehouseCodeId> {

    Optional<LocationEntity> findByWarehouseIdAndCodeAndDeletedAtIsNull(UUID warehouseId, String code);

    boolean existsByWarehouseIdAndAreaCodeAndDeletedAtIsNull(UUID warehouseId, String areaCode);

    /** '' = no filter */
    @Query("""
            select l from LocationEntity l
            where l.warehouseId = :warehouseId and l.deletedAt is null
              and (:areaCode = '' or l.areaCode = :areaCode)
              and (:codeFrom = '' or l.code >= :codeFrom)
              and (:codeTo = '' or l.code <= :codeTo)
            """)
    Page<LocationEntity> search(UUID warehouseId, String areaCode, String codeFrom, String codeTo, Pageable pageable);
}

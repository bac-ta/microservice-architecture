package com.epl.billing.repositories;

import com.epl.billing.entities.RateEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RateRepository extends JpaRepository<RateEntity, UUID> {

    /** including deleted rows (unique key tenant_id, code, valid_from) */
    Optional<RateEntity> findByTenantIdAndCodeAndValidFrom(UUID tenantId, String code, LocalDate validFrom);

    List<RateEntity> findAllByTenantIdAndCodeAndDeletedAtIsNullOrderByValidFrom(UUID tenantId, String code);

    /** chargeType '' = all; allPeriods = true ignores validOn */
    @Query("""
            select r from RateEntity r
            where r.tenantId = :tenantId and r.deletedAt is null
              and (:chargeType = '' or r.chargeType = :chargeType)
              and (:allPeriods = true or (r.validFrom <= :validOn and (r.validTo is null or r.validTo >= :validOn)))
            order by r.code, r.validFrom
            """)
    List<RateEntity> search(UUID tenantId, String chargeType, boolean allPeriods, LocalDate validOn);
}

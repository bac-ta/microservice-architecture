package com.epl.inbound.repositories;

import com.epl.inbound.entities.ArrivalEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.UUID;

public interface ArrivalRepository extends JpaRepository<ArrivalEntity, UUID>, JpaSpecificationExecutor<ArrivalEntity> {

    @EntityGraph(attributePaths = "lines")
    Optional<ArrivalEntity> findWithLinesByTenantIdAndArrivalNo(UUID tenantId, String arrivalNo);

    /** Header only: GetArrival when the caller does not read the lines (ResponseMask). */
    Optional<ArrivalEntity> findByTenantIdAndArrivalNo(UUID tenantId, String arrivalNo);
}

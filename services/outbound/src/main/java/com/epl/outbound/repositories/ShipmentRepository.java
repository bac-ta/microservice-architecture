package com.epl.outbound.repositories;

import com.epl.outbound.entities.ShipmentEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.UUID;

public interface ShipmentRepository extends JpaRepository<ShipmentEntity, UUID>, JpaSpecificationExecutor<ShipmentEntity> {

    @EntityGraph(attributePaths = {"lines", "pickingBatch"})
    Optional<ShipmentEntity> findWithLinesByTenantIdAndShipmentNo(UUID tenantId, String shipmentNo);
}

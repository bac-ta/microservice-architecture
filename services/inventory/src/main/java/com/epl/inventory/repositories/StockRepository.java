package com.epl.inventory.repositories;

import com.epl.inventory.entities.StockEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.UUID;

public interface StockRepository extends JpaRepository<StockEntity, UUID>, JpaSpecificationExecutor<StockEntity> {

    @EntityGraph(attributePaths = "lot")
    Optional<StockEntity> findByTenantIdAndId(UUID tenantId, UUID id);
}

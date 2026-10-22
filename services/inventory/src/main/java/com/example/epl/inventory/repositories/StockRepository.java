package com.example.epl.inventory.repositories;

import com.example.epl.inventory.entities.StockEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.UUID;

public interface StockRepository extends JpaRepository<StockEntity, UUID>, JpaSpecificationExecutor<StockEntity> {

    @EntityGraph(attributePaths = "lot")
    Optional<StockEntity> findByTenantIdAndId(UUID tenantId, UUID id);
}

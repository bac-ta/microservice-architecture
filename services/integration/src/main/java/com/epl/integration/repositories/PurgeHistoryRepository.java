package com.epl.integration.repositories;

import com.epl.integration.entities.PurgeHistoryEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.UUID;

public interface PurgeHistoryRepository extends JpaRepository<PurgeHistoryEntity, Long> {

    Page<PurgeHistoryEntity> findAllByTenantIdAndStartedAtBetween(UUID tenantId, Instant from, Instant to, Pageable pageable);
}

package com.epl.integration.repositories;

import com.epl.integration.entities.JobRunEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

public interface JobRunRepository extends JpaRepository<JobRunEntity, UUID> {

    Optional<JobRunEntity> findByTenantIdAndId(UUID tenantId, UUID id);

    /** jobCode '' = all; allStatuses = true ignores statuses */
    @Query("""
            select j from JobRunEntity j
            where j.tenantId = :tenantId
              and (:jobCode = '' or j.jobCode = :jobCode)
              and (:allStatuses = true or j.status in :statuses)
            """)
    Page<JobRunEntity> search(UUID tenantId, String jobCode, boolean allStatuses, Collection<String> statuses, Pageable pageable);
}

package com.epl.inventory.repositories;

import com.epl.inventory.entities.StocktakeEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

public interface StocktakeRepository extends JpaRepository<StocktakeEntity, UUID> {

    Optional<StocktakeEntity> findByTenantIdAndStocktakeNo(UUID tenantId, String stocktakeNo);

    /** allStatuses = true ignores the status list (JPQL cannot test an empty collection) */
    @Query("""
            select s from StocktakeEntity s
            where s.tenantId = :tenantId
              and (:allStatuses = true or s.status in :statuses)
              and s.stocktakeDate between :dateFrom and :dateTo
            """)
    Page<StocktakeEntity> search(UUID tenantId, boolean allStatuses, Collection<String> statuses,
                                 LocalDate dateFrom, LocalDate dateTo, Pageable pageable);
}

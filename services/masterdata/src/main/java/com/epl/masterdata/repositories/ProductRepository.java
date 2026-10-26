package com.epl.masterdata.repositories;

import com.epl.masterdata.entities.ProductEntity;
import com.epl.masterdata.entities.TenantCodeId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface ProductRepository extends JpaRepository<ProductEntity, TenantCodeId> {

    Optional<ProductEntity> findByTenantIdAndCodeAndDeletedAtIsNull(UUID tenantId, String code);

    /** pattern = '%text%' lower case matched against the code and every name; '' = no filter. */
    @Query(value = """
            SELECT * FROM product p
            WHERE p.tenant_id = :tenantId AND p.deleted_at IS NULL
              AND (:pattern = '' OR lower(p.code) LIKE :pattern
                   OR EXISTS (SELECT 1 FROM unnest(p.names) n WHERE lower(n) LIKE :pattern))
              AND (:groupCode = '' OR p.product_group_code = :groupCode)
            ORDER BY p.code
            """, countQuery = """
            SELECT count(*) FROM product p
            WHERE p.tenant_id = :tenantId AND p.deleted_at IS NULL
              AND (:pattern = '' OR lower(p.code) LIKE :pattern
                   OR EXISTS (SELECT 1 FROM unnest(p.names) n WHERE lower(n) LIKE :pattern))
              AND (:groupCode = '' OR p.product_group_code = :groupCode)
            """, nativeQuery = true)
    Page<ProductEntity> search(UUID tenantId, String pattern, String groupCode, Pageable pageable);
}

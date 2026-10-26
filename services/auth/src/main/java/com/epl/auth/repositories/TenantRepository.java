package com.epl.auth.repositories;

import com.epl.auth.entities.TenantEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TenantRepository extends JpaRepository<TenantEntity, UUID> {

    @EntityGraph(attributePaths = "customer")
    Optional<TenantEntity> findWithCustomerById(UUID id);

    Optional<TenantEntity> findByCompanyCodeAndWarehouseCodeAndCustomerCode(String companyCode, String warehouseCode,
                                                                           String customerCode);

    /** Empty warehouse code = every warehouse of the company. */
    @EntityGraph(attributePaths = "customer")
    @Query("""
            select t from TenantEntity t
            where t.companyCode = :companyCode and (:warehouseCode = '' or t.warehouseCode = :warehouseCode)
            order by t.warehouseCode, t.customerCode
            """)
    List<TenantEntity> search(String companyCode, String warehouseCode);
}

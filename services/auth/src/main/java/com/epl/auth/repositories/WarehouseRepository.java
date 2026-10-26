package com.epl.auth.repositories;

import com.epl.auth.entities.WarehouseEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WarehouseRepository extends JpaRepository<WarehouseEntity, UUID> {

    @EntityGraph(attributePaths = "company")
    Optional<WarehouseEntity> findByCompanyCodeAndCode(String companyCode, String code);

    @EntityGraph(attributePaths = "company")
    List<WarehouseEntity> findAllByCompanyCodeOrderByCode(String companyCode);
}

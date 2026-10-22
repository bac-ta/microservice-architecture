package com.example.epl.auth.repositories;

import com.example.epl.auth.entities.UserTenantGrantEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface UserTenantGrantRepository extends JpaRepository<UserTenantGrantEntity, UUID> {

    @EntityGraph(attributePaths = {"company", "warehouse", "customer"})
    List<UserTenantGrantEntity> findAllByUserIdOrderByValidFrom(UUID userId);

    boolean existsByRoleCode(String roleCode);
}

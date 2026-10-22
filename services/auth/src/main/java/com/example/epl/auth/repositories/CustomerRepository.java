package com.example.epl.auth.repositories;

import com.example.epl.auth.entities.CustomerEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CustomerRepository extends JpaRepository<CustomerEntity, UUID> {

    @EntityGraph(attributePaths = "company")
    Optional<CustomerEntity> findByCompanyCodeAndCode(String companyCode, String code);

    @EntityGraph(attributePaths = "company")
    List<CustomerEntity> findAllByCompanyCodeOrderByCode(String companyCode);
}

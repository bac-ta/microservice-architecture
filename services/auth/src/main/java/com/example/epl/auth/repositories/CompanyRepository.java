package com.example.epl.auth.repositories;

import com.example.epl.auth.entities.CompanyEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CompanyRepository extends JpaRepository<CompanyEntity, UUID> {

    Optional<CompanyEntity> findByCode(String code);

    List<CompanyEntity> findAllByOrderByCode();
}

package com.example.epl.integration.repositories;

import com.example.epl.integration.entities.JobDefinitionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JobDefinitionRepository extends JpaRepository<JobDefinitionEntity, String> {

    List<JobDefinitionEntity> findAllByOrderByCode();
}

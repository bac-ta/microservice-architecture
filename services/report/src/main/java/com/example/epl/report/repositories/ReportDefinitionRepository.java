package com.example.epl.report.repositories;

import com.example.epl.report.entities.ReportDefinitionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface ReportDefinitionRepository extends JpaRepository<ReportDefinitionEntity, UUID> {

    /** Visible rows (RLS: '*' and the current scope); processCode '' = all. */
    @Query("""
            select r from ReportDefinitionEntity r
            where r.enabled and (:processCode = '' or r.processCode = :processCode)
            order by r.code, r.specificity desc
            """)
    List<ReportDefinitionEntity> findVisible(String processCode);
}

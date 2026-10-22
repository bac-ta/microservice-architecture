package com.example.epl.report.services;

import com.example.epl.report.entities.ReportDefinitionEntity;
import com.example.epl.report.mappers.ReportMappers;
import com.example.epl.report.repositories.ReportDefinitionRepository;
import com.example.epl.report.v1.ReportDefinition;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;

/** Report definitions visible to the caller, most specific scope first (same rule as effective_report_definition). */
@Service
public class ReportCatalogService {

    private final ReportDefinitionRepository reportDefinitionRepository;

    public ReportCatalogService(ReportDefinitionRepository reportDefinitionRepository) {
        this.reportDefinitionRepository = reportDefinitionRepository;
    }

    @Transactional(readOnly = true)
    public List<ReportDefinition> list(String processCode) {
        var effective = new LinkedHashMap<String, ReportDefinitionEntity>();
        // ordered by code, specificity desc: the first row of each code wins
        reportDefinitionRepository.findVisible(processCode).forEach(row -> effective.putIfAbsent(row.getCode(), row));
        return effective.values().stream().map(ReportMappers::reportDefinition).toList();
    }
}

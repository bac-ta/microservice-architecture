package com.epl.report.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * rpt.report_definition — scoped config (NULL company / warehouse / customer = '*'), read-only for
 * rpt_app: RLS only lets it read the defaults and the rows of the current scope.
 */
@Getter
@Setter
@Entity
@Immutable
@Table(name = "report_definition")
public class ReportDefinitionEntity {

    @Id
    private UUID id;

    /** customer(4) + warehouse(2) + company(1): the most specific row wins */
    @Column(nullable = false)
    private short specificity;

    @Column(nullable = false, length = 40)
    private String code;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "process_code", nullable = false, length = 20)
    private String processCode;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(nullable = false, columnDefinition = "text[]")
    private List<String> formats = new ArrayList<>();

    /** JSON array: parameter names, or objects with a "name" */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false)
    private List<Object> parameters = new ArrayList<>();

    @Column(nullable = false)
    private boolean enabled;
}

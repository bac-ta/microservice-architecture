package com.epl.integration.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Immutable;

/** intg.job_definition — global catalogue, maintained by migrations (read-only for intg_app). */
@Getter
@Setter
@Entity
@Immutable
@Table(name = "job_definition")
public class JobDefinitionEntity {

    @Id
    @Column(length = 40)
    private String code;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 100)
    private String schedule;

    @Column(nullable = false)
    private boolean enabled;

    @Column(length = 500)
    private String description;
}

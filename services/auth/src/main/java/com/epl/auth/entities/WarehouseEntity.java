package com.epl.auth.entities;

import com.epl.database.AuditedEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

/** iam.warehouse — unique code within its company (e.g. WH01). */
@Getter
@Setter
@Entity
@Table(name = "warehouse")
public class WarehouseEntity extends AuditedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "company_id", nullable = false, updatable = false)
    private CompanyEntity company;

    @Column(nullable = false, updatable = false, length = 4)
    private String code;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(name = "time_zone", nullable = false, length = 40)
    private String timeZone;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(nullable = false, length = 16)
    private String status;
}

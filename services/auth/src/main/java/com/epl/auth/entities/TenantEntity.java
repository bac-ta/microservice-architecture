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

/**
 * iam.tenant — a customer operated in a warehouse. Its id is the tenant_id every other schema stores.
 * The codes are denormalised (JWT grants, logs, events) and never change.
 */
@Getter
@Setter
@Entity
@Table(name = "tenant")
public class TenantEntity extends AuditedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "company_id", nullable = false, updatable = false)
    private CompanyEntity company;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "warehouse_id", nullable = false, updatable = false)
    private WarehouseEntity warehouse;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false, updatable = false)
    private CustomerEntity customer;

    @Column(name = "company_code", nullable = false, updatable = false, length = 6)
    private String companyCode;

    @Column(name = "warehouse_code", nullable = false, updatable = false, length = 4)
    private String warehouseCode;

    @Column(name = "customer_code", nullable = false, updatable = false, length = 13)
    private String customerCode;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(nullable = false, length = 16)
    private String status;
}

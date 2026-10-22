package com.example.epl.billing.entities;

import com.example.database.AuditedEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/** bill.rate — one price period of a rate code; soft-deleted (charge lines keep their copy of the price). */
@Getter
@Setter
@Entity
@Table(name = "rate")
public class RateEntity extends AuditedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "tenant_id", nullable = false, updatable = false)
    private UUID tenantId;

    @Column(nullable = false, updatable = false, length = 20)
    private String code;

    /** IN, OUT, STORAGE, PROCESSING, OTHER */
    @Column(name = "charge_type", nullable = false, length = 12)
    private String chargeType;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 10)
    private String unit;

    @Column(name = "unit_price", nullable = false, precision = 15, scale = 4)
    private BigDecimal unitPrice;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(nullable = false, length = 3)
    private String currency;

    @Column(name = "valid_from", nullable = false, updatable = false)
    private LocalDate validFrom;

    @Column(name = "valid_to")
    private LocalDate validTo;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    /** [valid_from, valid_to] overlaps [from, to]; null = open */
    public boolean overlaps(LocalDate from, LocalDate to) {
        return (to == null || !validFrom.isAfter(to)) && (validTo == null || !validTo.isBefore(from));
    }
}

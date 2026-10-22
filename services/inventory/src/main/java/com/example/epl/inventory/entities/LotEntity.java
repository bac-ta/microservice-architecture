package com.example.epl.inventory.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Immutable;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * inv.lot — one set of picking keys 1..7 of a product. Immutable: stock that changes keys moves to
 * another lot. Created by {@link com.example.epl.inventory.services.LotService#findOrCreate}.
 */
@Getter
@Setter
@Entity
@Immutable
@Table(name = "lot")
public class LotEntity {

    @Id
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "product_code", nullable = false, length = 50)
    private String productCode;

    @Embedded
    private PickKeyColumns pickKeys;

    @Column(name = "lot_no", length = 30)
    private String lotNo;

    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "created_by", nullable = false, length = 50)
    private String createdBy;
}

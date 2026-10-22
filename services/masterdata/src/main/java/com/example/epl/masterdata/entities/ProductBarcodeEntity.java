package com.example.epl.masterdata.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

/** md.product_barcode — barcode -> product (and packing level), unique per tenant. */
@Getter
@Setter
@Entity
@Table(name = "product_barcode")
@IdClass(ProductBarcodeEntity.Id.class)
@EntityListeners(AuditingEntityListener.class)
public class ProductBarcodeEntity {

    @jakarta.persistence.Id
    @Column(name = "tenant_id")
    private UUID tenantId;

    @jakarta.persistence.Id
    @Column(length = 50)
    private String barcode;

    @Column(name = "product_code", nullable = false, length = 50)
    private String productCode;

    /** PIECE, BALL, CASE, PALLET */
    @Column(name = "uom_level", nullable = false, length = 10)
    private String uomLevel = "PIECE";

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @CreatedBy
    @Column(name = "created_by", nullable = false, updatable = false, length = 50)
    private String createdBy;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Id implements Serializable {
        private UUID tenantId;
        private String barcode;
    }
}

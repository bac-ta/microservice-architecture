package com.example.epl.report.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

/** rpt.printer — warehouse-scoped (shared by every customer of the warehouse). */
@Getter
@Setter
@Entity
@Table(name = "printer")
@IdClass(PrinterEntity.Id.class)
@EntityListeners(AuditingEntityListener.class)
public class PrinterEntity {

    @jakarta.persistence.Id
    @Column(name = "warehouse_id")
    private UUID warehouseId;

    @jakarta.persistence.Id
    @Column(length = 20)
    private String code;

    @Column(nullable = false, length = 100)
    private String name;

    /** LABEL, DOCUMENT */
    @Column(nullable = false, length = 8)
    private String kind;

    @Column(nullable = false, length = 300)
    private String endpoint;

    @Column(nullable = false)
    private boolean enabled;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @LastModifiedBy
    @Column(name = "updated_by", nullable = false, length = 50)
    private String updatedBy;

    @Version
    @Column(nullable = false)
    private int version;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Id implements Serializable {
        private UUID warehouseId;
        private String code;
    }
}

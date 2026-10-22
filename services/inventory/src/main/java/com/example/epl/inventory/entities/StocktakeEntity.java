package com.example.epl.inventory.entities;

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
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/** inv.stocktake header: INSTRUCTED -> COUNTING -> COUNTED -> CONFIRMED, or CANCELLED. */
@Getter
@Setter
@Entity
@Table(name = "stocktake")
public class StocktakeEntity extends AuditedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "tenant_id", nullable = false, updatable = false)
    private UUID tenantId;

    @Column(name = "warehouse_id", nullable = false, updatable = false)
    private UUID warehouseId;

    @Column(name = "stocktake_no", nullable = false, updatable = false, length = 20)
    private String stocktakeNo;

    @Column(name = "stocktake_date", nullable = false)
    private LocalDate stocktakeDate;

    @Column(name = "confirmed_date")
    private LocalDate confirmedDate;

    @Column(nullable = false, length = 16)
    private String status;

    @Column(nullable = false, length = 2)
    private String kind;

    @Column(name = "target_kind", nullable = false, length = 2)
    private String targetKind;

    @Column(name = "include_empty_locations", nullable = false)
    private boolean includeEmptyLocations;

    @Column(name = "update_stock_on_confirm", nullable = false)
    private boolean updateStockOnConfirm;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false)
    private Map<String, Object> criteria = new LinkedHashMap<>();

    @Column(name = "planned_qty", nullable = false, precision = 15, scale = 3)
    private BigDecimal plannedQty = BigDecimal.ZERO;

    @Column(name = "counted_qty", nullable = false, precision = 15, scale = 3)
    private BigDecimal countedQty = BigDecimal.ZERO;

    @Column(length = 500)
    private String remarks;
}

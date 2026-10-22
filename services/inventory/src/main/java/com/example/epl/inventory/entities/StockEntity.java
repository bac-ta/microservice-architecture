package com.example.epl.inventory.entities;

import com.example.database.AuditedEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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

/**
 * inv.stock — goods of one receipt line at one location, with their lot (picking keys, inv.lot).
 * Read here; changed only by the stock operations
 * (adjust, relocate …), each writing the movement ledger in the same transaction.
 */
@Getter
@Setter
@Entity
@Table(name = "stock")
public class StockEntity extends AuditedEntity {

    @Id
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "warehouse_id", nullable = false)
    private UUID warehouseId;

    @Column(name = "receipt_no", nullable = false, length = 20)
    private String receiptNo;

    @Column(name = "receipt_line", nullable = false)
    private int receiptLine;

    @Column(name = "receipt_seq", nullable = false)
    private int receiptSeq;

    @Column(name = "stock_kind", nullable = false, length = 1)
    private String stockKind;

    @Column(name = "received_on", nullable = false)
    private LocalDate receivedOn;

    @Column(name = "product_code", nullable = false, length = 50)
    private String productCode;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "origin_country", length = 2)
    private String originCountry;

    /** picking keys 1..7 (inv.lot); same product, enforced by fk_stock_lot */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "lot_id", nullable = false)
    private LotEntity lot;

    @Column(name = "location_code", nullable = false, length = 18)
    private String locationCode;

    @Column(name = "area_code", nullable = false, length = 3)
    private String areaCode;

    @Column(name = "sub_inventory_code", nullable = false, length = 20)
    private String subInventoryCode;

    @Column(name = "physical_qty", nullable = false, precision = 12, scale = 3)
    private BigDecimal physicalQty;

    @Column(name = "available_qty", nullable = false, precision = 12, scale = 3)
    private BigDecimal availableQty;

    @Column(name = "allocated_qty", nullable = false, precision = 12, scale = 3)
    private BigDecimal allocatedQty;

    @Column(name = "units_per_case")
    private Integer unitsPerCase;

    @Column(name = "units_per_ball")
    private Integer unitsPerBall;

    @Column(nullable = false)
    private boolean bonded;

    @Column(name = "on_hold", nullable = false)
    private boolean onHold;

    @Column(nullable = false)
    private boolean locked;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false)
    private Map<String, Object> ext = new LinkedHashMap<>();
}

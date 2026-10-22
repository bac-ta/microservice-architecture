package com.example.epl.outbound.entities;

import com.example.database.AuditedEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
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
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/** outb.shipment_line — ordered quantity; allocated / shipped totals are kept by allocation and confirmation. */
@Getter
@Setter
@Entity
@Table(name = "shipment_line")
public class ShipmentLineEntity extends AuditedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "tenant_id", nullable = false, updatable = false)
    private UUID tenantId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "shipment_id", nullable = false, updatable = false)
    private ShipmentEntity shipment;

    @Column(name = "line_no", nullable = false)
    private int lineNo;

    @Column(nullable = false, length = 20)
    private String status;

    @Column(name = "product_code", nullable = false, length = 50)
    private String productCode;

    @Column(name = "product_name", length = 200)
    private String productName;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "origin_country", length = 2)
    private String originCountry;

    @Column(name = "units_per_case")
    private Integer unitsPerCase;

    @Column(name = "units_per_ball")
    private Integer unitsPerBall;

    @Column(name = "ordered_case_qty", nullable = false)
    private int orderedCaseQty;

    @Column(name = "ordered_ball_qty", nullable = false)
    private int orderedBallQty;

    @Column(name = "ordered_piece_qty", nullable = false, precision = 12, scale = 3)
    private BigDecimal orderedPieceQty = BigDecimal.ZERO;

    @Column(name = "ordered_total_qty", nullable = false, precision = 12, scale = 3)
    private BigDecimal orderedTotalQty;

    @Column(name = "allocated_total_qty", nullable = false, precision = 12, scale = 3)
    private BigDecimal allocatedTotalQty = BigDecimal.ZERO;

    @Column(name = "shipped_total_qty", nullable = false, precision = 12, scale = 3)
    private BigDecimal shippedTotalQty = BigDecimal.ZERO;

    @Column(name = "sub_inventory_code", length = 20)
    private String subInventoryCode;

    @Embedded
    private PickKeyColumns pickKeys = new PickKeyColumns();

    @Column(name = "allocation_kind", length = 10)
    private String allocationKind;

    @Column(name = "not_shippable", nullable = false)
    private boolean notShippable;

    @Column(name = "customer_line_seq")
    private Integer customerLineSeq;

    @Column(name = "warehouse_instructions", length = 500)
    private String warehouseInstructions;

    @Column(name = "delivery_note_remarks", length = 500)
    private String deliveryNoteRemarks;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false)
    private Map<String, Object> ext = new LinkedHashMap<>();
}

package com.epl.inbound.entities;

import com.epl.database.AuditedEntity;
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

/** inb.arrival_line — planned quantity; received_total_qty is the sum of its inspection results. */
@Getter
@Setter
@Entity
@Table(name = "arrival_line")
public class ArrivalLineEntity extends AuditedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "tenant_id", nullable = false, updatable = false)
    private UUID tenantId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "arrival_id", nullable = false, updatable = false)
    private ArrivalEntity arrival;

    @Column(name = "line_no", nullable = false)
    private int lineNo;

    @Column(nullable = false, length = 16)
    private String status;

    @Column(nullable = false)
    private boolean inspected;

    @Column(name = "product_code", nullable = false, length = 50)
    private String productCode;

    @Column(name = "product_name", length = 200)
    private String productName;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "origin_country", length = 2)
    private String originCountry;

    @Column(name = "supplier_product_code", length = 50)
    private String supplierProductCode;

    @Column(name = "units_per_case")
    private Integer unitsPerCase;

    @Column(name = "units_per_ball")
    private Integer unitsPerBall;

    @Column(name = "planned_case_qty", nullable = false)
    private int plannedCaseQty;

    @Column(name = "planned_ball_qty", nullable = false)
    private int plannedBallQty;

    @Column(name = "planned_piece_qty", nullable = false, precision = 12, scale = 3)
    private BigDecimal plannedPieceQty = BigDecimal.ZERO;

    @Column(name = "planned_total_qty", nullable = false, precision = 12, scale = 3)
    private BigDecimal plannedTotalQty;

    @Column(name = "received_total_qty", nullable = false, precision = 12, scale = 3)
    private BigDecimal receivedTotalQty = BigDecimal.ZERO;

    @Column(name = "sub_inventory_code", length = 20)
    private String subInventoryCode;

    @Embedded
    private PickKeyColumns pickKeys = new PickKeyColumns();

    @Column(name = "on_hold", nullable = false)
    private boolean onHold;

    @Column(name = "customer_line_seq")
    private Integer customerLineSeq;

    @Column(length = 500)
    private String remarks;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false)
    private Map<String, Object> ext = new LinkedHashMap<>();
}

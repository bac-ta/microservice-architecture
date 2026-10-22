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

import java.math.BigDecimal;
import java.util.UUID;

/** inv.stocktake_line — book quantity at instruction time and the counted quantity. Read-only here. */
@Getter
@Setter
@Entity
@Table(name = "stocktake_line")
public class StocktakeLineEntity extends AuditedEntity {

    @Id
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "stocktake_id", nullable = false)
    private UUID stocktakeId;

    @Column(name = "count_no", nullable = false)
    private int countNo;

    @Column(name = "stock_id")
    private UUID stockId;

    @Column(name = "receipt_no", length = 20)
    private String receiptNo;

    @Column(name = "receipt_line")
    private Integer receiptLine;

    @Column(name = "receipt_seq")
    private Integer receiptSeq;

    @Column(name = "product_code", length = 50)
    private String productCode;

    /** picking keys 1..7 of this line; null = empty location / found stock without a lot yet */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lot_id")
    private LotEntity lot;

    @Column(name = "location_code", nullable = false, length = 18)
    private String locationCode;

    @Column(name = "sub_inventory_code", length = 20)
    private String subInventoryCode;

    @Column(name = "units_per_case")
    private Integer unitsPerCase;

    @Column(name = "units_per_ball")
    private Integer unitsPerBall;

    @Column(name = "book_case_qty", nullable = false)
    private int bookCaseQty;

    @Column(name = "book_ball_qty", nullable = false)
    private int bookBallQty;

    @Column(name = "book_piece_qty", nullable = false, precision = 12, scale = 3)
    private BigDecimal bookPieceQty;

    @Column(name = "book_total_qty", nullable = false, precision = 12, scale = 3)
    private BigDecimal bookTotalQty;

    @Column(name = "counted_case_qty")
    private Integer countedCaseQty;

    @Column(name = "counted_ball_qty")
    private Integer countedBallQty;

    @Column(name = "counted_piece_qty", precision = 12, scale = 3)
    private BigDecimal countedPieceQty;

    @Column(name = "counted_total_qty", precision = 12, scale = 3)
    private BigDecimal countedTotalQty;

    /** generated: counted_total_qty - book_total_qty */
    @Column(name = "diff_total_qty", insertable = false, updatable = false, precision = 12, scale = 3)
    private BigDecimal diffTotalQty;

    @Column(length = 500)
    private String remarks;
}

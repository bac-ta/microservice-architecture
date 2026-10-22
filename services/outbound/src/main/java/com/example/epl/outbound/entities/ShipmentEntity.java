package com.example.epl.outbound.entities;

import com.example.database.AuditedEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** outb.shipment with its lines: ORDERED -> (PARTIALLY_)ALLOCATED -> PICKING -> PICKED -> PACKED -> SHIPPED, or CANCELLED. */
@Getter
@Setter
@Entity
@Table(name = "shipment")
public class ShipmentEntity extends AuditedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "tenant_id", nullable = false, updatable = false)
    private UUID tenantId;

    @Column(name = "warehouse_id", nullable = false, updatable = false)
    private UUID warehouseId;

    @Column(name = "shipment_no", nullable = false, updatable = false, length = 20)
    private String shipmentNo;

    @Column(name = "scheduled_date", nullable = false)
    private LocalDate scheduledDate;

    @Column(name = "transaction_kind", nullable = false, length = 3)
    private String transactionKind;

    @Column(nullable = false, length = 20)
    private String status;

    @Column(name = "shipped_date")
    private LocalDate shippedDate;

    @Column(name = "confirmed_date")
    private LocalDate confirmedDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "picking_batch_id", insertable = false, updatable = false)
    private PickingBatchEntity pickingBatch;

    @Column(name = "invoice_no", length = 30)
    private String invoiceNo;

    @Column(name = "customer_ref", length = 30)
    private String customerRef;

    @Column(name = "po_no", length = 30)
    private String poNo;

    // party snapshots (epl.common.v1.Party without its code) as printed on the documents

    @Column(name = "ship_to_code", length = 20)
    private String shipToCode;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "ship_to", nullable = false)
    private Map<String, Object> shipTo = new LinkedHashMap<>();

    @Column(name = "final_ship_to_code", length = 20)
    private String finalShipToCode;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "final_ship_to", nullable = false)
    private Map<String, Object> finalShipTo = new LinkedHashMap<>();

    @Column(name = "shipper_code", length = 20)
    private String shipperCode;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false)
    private Map<String, Object> shipper = new LinkedHashMap<>();

    @Column(name = "bill_to_code", length = 20)
    private String billToCode;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "bill_to", nullable = false)
    private Map<String, Object> billTo = new LinkedHashMap<>();

    @Column(name = "carrier_code", length = 10)
    private String carrierCode;

    @Column(name = "warehouse_instructions", length = 500)
    private String warehouseInstructions;

    @Column(name = "delivery_note_remarks", length = 500)
    private String deliveryNoteRemarks;

    @Column(name = "waybill_remarks", length = 500)
    private String waybillRemarks;

    @Column(name = "total_weight", precision = 12, scale = 3)
    private BigDecimal totalWeight;

    @Column(name = "total_volume", precision = 12, scale = 4)
    private BigDecimal totalVolume;

    @Column(name = "cancel_reason_code", length = 3)
    private String cancelReasonCode;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false)
    private Map<String, Object> ext = new LinkedHashMap<>();

    @OneToMany(mappedBy = "shipment", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("lineNo")
    private List<ShipmentLineEntity> lines = new ArrayList<>();
}

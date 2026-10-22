package com.example.epl.inbound.entities;

import com.example.database.AuditedEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** inb.arrival with its lines: PLANNED -> INSPECTING -> INSPECTED -> CONFIRMED, or CANCELLED. */
@Getter
@Setter
@Entity
@Table(name = "arrival")
public class ArrivalEntity extends AuditedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "tenant_id", nullable = false, updatable = false)
    private UUID tenantId;

    @Column(name = "warehouse_id", nullable = false, updatable = false)
    private UUID warehouseId;

    @Column(name = "arrival_no", nullable = false, updatable = false, length = 20)
    private String arrivalNo;

    @Column(name = "scheduled_date", nullable = false)
    private LocalDate scheduledDate;

    @Column(name = "transaction_kind", nullable = false, length = 3)
    private String transactionKind;

    @Column(nullable = false, length = 16)
    private String status;

    @Column(name = "arrived_date")
    private LocalDate arrivedDate;

    @Column(name = "confirmed_date")
    private LocalDate confirmedDate;

    @Column(name = "invoice_no", length = 30)
    private String invoiceNo;

    @Column(name = "customer_ref", length = 30)
    private String customerRef;

    @Column(name = "po_no", length = 30)
    private String poNo;

    @Column(name = "supplier_code", length = 20)
    private String supplierCode;

    /** epl.common.v1.Party snapshot as printed on the document */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false)
    private Map<String, Object> supplier = new LinkedHashMap<>();

    @Column(nullable = false)
    private boolean bonded;

    @Column(name = "bl_no", length = 30)
    private String blNo;

    @Column(name = "container_no", length = 20)
    private String containerNo;

    @Column(name = "transport_code", length = 10)
    private String transportCode;

    @Column(name = "return_shipment_no", length = 20)
    private String returnShipmentNo;

    @Column(name = "cancel_reason_code", length = 3)
    private String cancelReasonCode;

    @Column(name = "result_sent_at")
    private Instant resultSentAt;

    @Column(length = 500)
    private String remarks;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false)
    private Map<String, Object> ext = new LinkedHashMap<>();

    @OneToMany(mappedBy = "arrival", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("lineNo")
    private List<ArrivalLineEntity> lines = new ArrayList<>();
}

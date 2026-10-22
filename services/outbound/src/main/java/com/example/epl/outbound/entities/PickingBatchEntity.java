package com.example.epl.outbound.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Immutable;

import java.util.UUID;

/** outb.picking_batch, read here for the batch numbers shown on a shipment (created by PickingService). */
@Getter
@Setter
@Entity
@Immutable
@Table(name = "picking_batch")
public class PickingBatchEntity {

    @Id
    private UUID id;

    @Column(name = "batch_no", nullable = false, length = 20)
    private String batchNo;

    @Column(name = "total_picking_no", length = 20)
    private String totalPickingNo;
}

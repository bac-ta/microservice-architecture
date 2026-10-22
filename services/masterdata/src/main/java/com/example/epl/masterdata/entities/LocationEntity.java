package com.example.epl.masterdata.entities;

import com.example.database.AuditedEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/** md.location — area + rack + position + level, warehouse-scoped, soft-deleted. */
@Getter
@Setter
@Entity
@Table(name = "location")
@IdClass(WarehouseCodeId.class)
public class LocationEntity extends AuditedEntity {

    @Id
    @Column(name = "warehouse_id")
    private UUID warehouseId;

    @Id
    @Column(length = 18)
    private String code;

    @Column(name = "area_code", nullable = false, length = 3)
    private String areaCode;

    @Column(name = "rack_code", nullable = false, length = 10)
    private String rackCode;

    @Column(name = "position_code", nullable = false, length = 3)
    private String positionCode;

    @Column(name = "level_code", nullable = false, length = 2)
    private String levelCode;

    @Column(name = "zone_code", length = 2)
    private String zoneCode;

    @Column(name = "no_allocation", nullable = false)
    private boolean noAllocation;

    @Column(name = "no_replenishment", nullable = false)
    private boolean noReplenishment;

    @Column(name = "single_product_only", nullable = false)
    private boolean singleProductOnly;

    @Column(name = "single_pick_key_only", nullable = false)
    private boolean singlePickKeyOnly;

    @Column(name = "allocation_priority")
    private Integer allocationPriority;

    @Column(name = "picking_sequence")
    private Integer pickingSequence;

    @Column(name = "temperature_control", length = 2)
    private String temperatureControl;

    @Column(name = "dedicated_product_code", length = 50)
    private String dedicatedProductCode;

    @Column(name = "sub_inventory_code", length = 20)
    private String subInventoryCode;

    @Column(name = "bond_kind", length = 2)
    private String bondKind;

    @Column(length = 50)
    private String remarks;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false)
    private Map<String, Object> ext = new LinkedHashMap<>();

    @Column(name = "deleted_at")
    private Instant deletedAt;
}

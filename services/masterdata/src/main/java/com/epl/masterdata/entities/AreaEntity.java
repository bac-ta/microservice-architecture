package com.epl.masterdata.entities;

import com.epl.database.AuditedEntity;
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

/** md.area — warehouse-scoped, soft-deleted (deleted_at). */
@Getter
@Setter
@Entity
@Table(name = "area")
@IdClass(WarehouseCodeId.class)
public class AreaEntity extends AuditedEntity {

    @Id
    @Column(name = "warehouse_id")
    private UUID warehouseId;

    @Id
    @Column(length = 3)
    private String code;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(name = "picking_group_code", length = 3)
    private String pickingGroupCode;

    @Column(name = "temperature_kind", length = 2)
    private String temperatureKind;

    @Column(name = "no_allocation", nullable = false)
    private boolean noAllocation;

    @Column(name = "no_replenishment", nullable = false)
    private boolean noReplenishment;

    @Column(name = "single_product_only", nullable = false)
    private boolean singleProductOnly;

    @Column(name = "single_pick_key_only", nullable = false)
    private boolean singlePickKeyOnly;

    @Column(nullable = false)
    private boolean locked;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false)
    private Map<String, Object> ext = new LinkedHashMap<>();

    @Column(name = "deleted_at")
    private Instant deletedAt;
}

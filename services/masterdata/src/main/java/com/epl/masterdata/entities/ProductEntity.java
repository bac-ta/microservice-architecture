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

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * md.product — tenant-scoped, soft-deleted. Only the columns of epl.masterdata.v1.Product are mapped;
 * the others (prices, dimensions, replenishment …) keep their values / defaults.
 */
@Getter
@Setter
@Entity
@Table(name = "product")
@IdClass(TenantCodeId.class)
public class ProductEntity extends AuditedEntity {

    @Id
    @Column(name = "tenant_id")
    private UUID tenantId;

    @Id
    @Column(length = 50)
    private String code;

    /** NAM1..NAM4 */
    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(nullable = false, columnDefinition = "text[]")
    private List<String> names = new ArrayList<>();

    @Column(name = "product_group_code", length = 15)
    private String productGroupCode;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(nullable = false, columnDefinition = "text[]")
    private List<String> hierarchy = new ArrayList<>();

    @Column(name = "units_per_case")
    private Integer unitsPerCase;

    @Column(name = "units_per_ball")
    private Integer unitsPerBall;

    @Column(name = "cases_per_pallet")
    private Integer casesPerPallet;

    @Column(name = "uom_piece", length = 3)
    private String uomPiece;

    @Column(name = "uom_case", length = 3)
    private String uomCase;

    @Column(name = "uom_ball", length = 3)
    private String uomBall;

    @Column(name = "case_weight", precision = 12, scale = 6)
    private BigDecimal caseWeight;

    @Column(name = "case_volume", precision = 12, scale = 6)
    private BigDecimal caseVolume;

    @Column(name = "piece_weight", precision = 12, scale = 6)
    private BigDecimal pieceWeight;

    @Column(name = "piece_volume", precision = 12, scale = 6)
    private BigDecimal pieceVolume;

    @Column(name = "storage_kind", length = 2)
    private String storageKind;

    @Column(name = "abc_class", length = 1)
    private String abcClass;

    @Column(name = "expiry_kind", length = 1)
    private String expiryKind;

    @Column(name = "expiry_days")
    private Integer expiryDays;

    @Column(name = "not_shippable", nullable = false)
    private boolean notShippable;

    @Column(name = "is_kit_parent", nullable = false)
    private boolean kitParent;

    @Column(name = "hs_code", length = 18)
    private String hsCode;

    @Column(name = "bond_kind", length = 2)
    private String bondKind;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "origin_country", length = 2)
    private String originCountry;

    @Column(name = "recommended_location", length = 18)
    private String recommendedLocation;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false)
    private Map<String, Object> ext = new LinkedHashMap<>();

    @Column(name = "deleted_at")
    private Instant deletedAt;
}

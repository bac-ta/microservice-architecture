package com.epl.auth.entities;

import com.epl.database.AuditedEntity;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapKeyColumn;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/** iam.app_user with its ABAC subject attributes (iam.user_attribute). */
@Getter
@Setter
@Entity
@Table(name = "app_user")
public class AppUserEntity extends AuditedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 50)
    private String username;

    @Column(length = 254)
    private String email;

    @Column(name = "display_name", nullable = false, length = 200)
    private String displayName;

    /** INTERNAL, CUSTOMER, SYSTEM */
    @Column(name = "user_type", nullable = false, length = 16)
    private String userType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "home_company_id")
    private CompanyEntity homeCompany;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "home_customer_id")
    private CustomerEntity homeCustomer;

    @Column(name = "department_code", length = 20)
    private String departmentCode;

    @Column(nullable = false, length = 10)
    private String locale;

    @Column(name = "time_zone", length = 40)
    private String timeZone;

    /** ACTIVE, LOCKED, DISABLED */
    @Column(nullable = false, length = 16)
    private String status;

    @Column(name = "token_version", nullable = false)
    private int tokenVersion;

    @Column(name = "failed_login_count", nullable = false)
    private int failedLoginCount;

    @Column(name = "locked_until")
    private Instant lockedUntil;

    @Column(name = "last_login_at")
    private Instant lastLoginAt;

    @ElementCollection
    @CollectionTable(name = "user_attribute", joinColumns = @JoinColumn(name = "user_id"))
    @MapKeyColumn(name = "attr_key", length = 50)
    private Map<String, UserAttribute> attributes = new LinkedHashMap<>();
}

package com.epl.auth.entities;

import com.epl.database.AuditedEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.UUID;

/** iam.company — top of the tenant registry (e.g. C001). */
@Getter
@Setter
@Entity
@Table(name = "company")
public class CompanyEntity extends AuditedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, updatable = false, length = 6)
    private String code;

    @Column(nullable = false, length = 200)
    private String name;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "country_code", length = 2)
    private String countryCode;

    @Column(name = "time_zone", nullable = false, length = 40)
    private String timeZone;

    @Column(nullable = false, length = 16)
    private String status;
}

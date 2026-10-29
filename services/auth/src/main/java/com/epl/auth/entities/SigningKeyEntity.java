package com.epl.auth.entities;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;

/** iam.signing_key — public half of a JWT signing key, published as JWKS; the private key is only referenced. */
@Getter
@Setter
@Entity
@Table(name = "signing_key")
public class SigningKeyEntity {

    @Id
    @Column(length = 64)
    private String kid;

    /** ES256, RS256, EdDSA */
    @Column(nullable = false, length = 10)
    private String algorithm;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "public_jwk", nullable = false)
    private JsonNode publicJwk;

    @Column(name = "private_key_ref", nullable = false, length = 300)
    private String privateKeyRef;

    /** NEXT, ACTIVE, RETIRED */
    @Column(nullable = false, length = 16)
    private String status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "activated_at")
    private Instant activatedAt;

    @Column(name = "retired_at")
    private Instant retiredAt;
}

package com.example.epl.auth.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

/** iam.user_credential — password hash of a user (never the password). */
@Getter
@Setter
@Entity
@Table(name = "user_credential")
public class UserCredentialEntity {

    @Id
    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "hash_algorithm", nullable = false, length = 20)
    private String hashAlgorithm;

    @Column(name = "must_change", nullable = false)
    private boolean mustChange;

    @Column(name = "expires_at")
    private Instant expiresAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}

package com.epl.auth.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

/** iam.login_audit — one row per login, refresh, logout or failure (append-only). */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "login_audit")
public class LoginAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "occurred_at", nullable = false, updatable = false)
    private Instant occurredAt;

    @Column(nullable = false, updatable = false, length = 50)
    private String username;

    @Column(name = "user_id", updatable = false)
    private UUID userId;

    @Column(name = "client_id", updatable = false, length = 50)
    private String clientId;

    /** LOGIN_OK, LOGIN_FAILED, LOCKED, REFRESH, REUSE_DETECTED, LOGOUT */
    @Column(nullable = false, updatable = false, length = 30)
    private String event;

    public LoginAuditEntity(String username, UUID userId, String clientId, String event) {
        this.occurredAt = Instant.now();
        this.username = username;
        this.userId = userId;
        this.clientId = clientId;
        this.event = event;
    }
}

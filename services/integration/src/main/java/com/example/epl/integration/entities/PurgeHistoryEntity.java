package com.example.epl.integration.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Immutable;

import java.time.Instant;
import java.util.UUID;

/** intg.purge_history — what a purge run deleted, per table. Written by the purge job, read here. */
@Getter
@Setter
@Entity
@Immutable
@Table(name = "purge_history")
public class PurgeHistoryEntity {

    @Id
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "table_name", nullable = false, length = 100)
    private String tableName;

    @Column(name = "deleted_rows", nullable = false)
    private long deletedRows;

    @Column(nullable = false, length = 8)
    private String result;

    @Column(name = "started_at", nullable = false)
    private Instant startedAt;

    @Column(name = "finished_at")
    private Instant finishedAt;

    @Column(name = "error_message", length = 1000)
    private String errorMessage;
}

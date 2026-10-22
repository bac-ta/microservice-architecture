-- =============================================================================
-- Integration database — owned by service-integration
-- PostgreSQL 16
--
-- File uploads, EDI send / receive, batch jobs and data purge.
-- File contents (uploaded CSV, EDI payloads) live in object storage (S3);
-- tables hold only their metadata and storage_ref.
--
-- tenant_id is a real foreign key to the iam registry (REFERENCES from iam/V4).
-- Runs as intg_owner after bootstrap.sql and the iam migrations.
-- =============================================================================

SET search_path = intg;

GRANT USAGE ON SCHEMA intg TO intg_app;
ALTER DEFAULT PRIVILEGES IN SCHEMA intg GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO intg_app;
ALTER DEFAULT PRIVILEGES IN SCHEMA intg GRANT USAGE, SELECT ON SEQUENCES TO intg_app;
-- cross-tenant admin / batch role (BYPASSRLS)
ALTER DEFAULT PRIVILEGES IN SCHEMA intg GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO epl_admin;

-- -----------------------------------------------------------------------------
-- File uploads (orders, masters, results)
-- -----------------------------------------------------------------------------
CREATE TABLE upload (
    id             uuid          PRIMARY KEY DEFAULT gen_random_uuid(),   -- UploadResult.upload_id
    tenant_id      uuid          NOT NULL REFERENCES iam.tenant (id),
    file_kind      varchar(30)   NOT NULL,            -- ARRIVAL, SHIPMENT, PRODUCT … (UPPER_SNAKE)
    file_name      varchar(255)  NOT NULL,
    content_type   varchar(100),
    size_bytes     bigint        NOT NULL CHECK (size_bytes >= 0),
    sha256         bytea         NOT NULL,
    storage_ref    varchar(500)  NOT NULL,            -- s3://bucket/key
    validate_only  boolean       NOT NULL DEFAULT false,
    status         varchar(12)   NOT NULL DEFAULT 'RECEIVED' CHECK (status IN
                   ('RECEIVED', 'VALIDATING', 'IMPORTING', 'DONE', 'FAILED')),
    total_rows     integer,
    accepted_rows  integer       NOT NULL DEFAULT 0,
    rejected_rows  integer       NOT NULL DEFAULT 0,
    started_at     timestamptz,
    finished_at    timestamptz,
    error_message  varchar(1000),
    created_at     timestamptz   NOT NULL DEFAULT now(),
    created_by     varchar(50)   NOT NULL,
    UNIQUE (tenant_id, id)
);
CREATE INDEX ix_upload_created ON upload (tenant_id, created_at DESC);

-- All row errors of an upload (GetUploadErrors pages through them)
CREATE TABLE upload_error (
    id             bigint        GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    tenant_id      uuid          NOT NULL,
    upload_id      uuid          NOT NULL,
    row_no         integer       NOT NULL,
    field          varchar(100),
    message_code   varchar(50)   NOT NULL,
    message_args   text[]        NOT NULL DEFAULT '{}',
    FOREIGN KEY (tenant_id, upload_id) REFERENCES upload (tenant_id, id) ON DELETE CASCADE
);
CREATE INDEX ix_upload_error_upload ON upload_error (upload_id, row_no);

-- -----------------------------------------------------------------------------
-- EDI messages, sent and received
-- -----------------------------------------------------------------------------
CREATE TABLE edi_message (
    id             uuid          PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id      uuid          NOT NULL REFERENCES iam.tenant (id),
    direction      varchar(8)    NOT NULL CHECK (direction IN ('SEND', 'RECEIVE')),
    edi_code       varchar(20)   NOT NULL,            -- message type, e.g. ARRIVAL_RESULT
    partner        varchar(50)   NOT NULL,
    status         varchar(8)    NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'OK', 'ERROR')),
    batch_no       varchar(30),
    record_count   integer       NOT NULL DEFAULT 0,
    storage_ref    varchar(500),
    source_ref     varchar(100),                      -- document numbers / event id that produced it
    resend_of_id   uuid          REFERENCES edi_message (id),   -- Resend creates a new row
    processed_at   timestamptz,
    error_message  varchar(1000),
    created_at     timestamptz   NOT NULL DEFAULT now(),
    created_by     varchar(50)   NOT NULL
);
CREATE INDEX ix_edi_message_search ON edi_message (tenant_id, direction, edi_code, created_at DESC);
CREATE INDEX ix_edi_message_error  ON edi_message (tenant_id, created_at) WHERE status = 'ERROR';

-- -----------------------------------------------------------------------------
-- Batch jobs and data purge
-- -----------------------------------------------------------------------------

-- Job catalogue (not tenant-specific); scheduling itself is done by the scheduler / K8s CronJob
CREATE TABLE job_definition (
    code         varchar(40)   PRIMARY KEY,           -- PURGE, EDI_RECEIVE, MONTH_END …
    name         varchar(100)  NOT NULL,
    schedule     varchar(100),                        -- cron expression, informational
    enabled      boolean       NOT NULL DEFAULT true,
    description  varchar(500)
);

CREATE TABLE job_run (
    id           uuid          PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id    uuid          NOT NULL REFERENCES iam.tenant (id),
    job_code     varchar(40)   NOT NULL REFERENCES job_definition (code),
    status       varchar(8)    NOT NULL DEFAULT 'QUEUED' CHECK (status IN ('QUEUED', 'RUNNING', 'DONE', 'FAILED')),
    parameters   jsonb         NOT NULL DEFAULT '{}',
    started_at   timestamptz,
    finished_at  timestamptz,
    message      varchar(1000),
    created_at   timestamptz   NOT NULL DEFAULT now(),
    created_by   varchar(50)   NOT NULL,
    UNIQUE (tenant_id, id)
);
CREATE INDEX ix_job_run_code ON job_run (tenant_id, job_code, created_at DESC);

-- What RunPurge may delete: one row per table of a data group, owned by the service that owns the table
CREATE TABLE purge_rule (
    table_group     varchar(30)   NOT NULL,           -- ARRIVAL, SHIPMENT, MOVEMENT, LOG …
    service         varchar(40)   NOT NULL,           -- service that runs the delete (inbound, outbound …)
    table_name      varchar(100)  NOT NULL,           -- schema.table
    date_column     varchar(60)   NOT NULL,
    retention_days  integer       NOT NULL CHECK (retention_days > 0),
    enabled         boolean       NOT NULL DEFAULT true,
    PRIMARY KEY (table_group, table_name)
);

CREATE TABLE purge_history (
    id             bigint        GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    tenant_id      uuid          NOT NULL,
    job_run_id     uuid          NOT NULL,
    table_name     varchar(100)  NOT NULL,
    older_than     date          NOT NULL,
    dry_run        boolean       NOT NULL DEFAULT false,
    deleted_rows   bigint        NOT NULL DEFAULT 0,
    result         varchar(8)    NOT NULL CHECK (result IN ('OK', 'ERROR', 'SKIPPED')),
    started_at     timestamptz   NOT NULL,
    finished_at    timestamptz,
    error_message  varchar(1000),
    FOREIGN KEY (tenant_id, job_run_id) REFERENCES job_run (tenant_id, id)
);
CREATE INDEX ix_purge_history_started ON purge_history (tenant_id, started_at DESC);

-- -----------------------------------------------------------------------------
-- Row-Level Security (job_definition and purge_rule are global catalogues)
-- -----------------------------------------------------------------------------
ALTER TABLE upload        ENABLE ROW LEVEL SECURITY;
ALTER TABLE upload_error  ENABLE ROW LEVEL SECURITY;
ALTER TABLE edi_message   ENABLE ROW LEVEL SECURITY;
ALTER TABLE job_run       ENABLE ROW LEVEL SECURITY;
ALTER TABLE purge_history ENABLE ROW LEVEL SECURITY;

ALTER TABLE upload        FORCE ROW LEVEL SECURITY;
ALTER TABLE upload_error  FORCE ROW LEVEL SECURITY;
ALTER TABLE edi_message   FORCE ROW LEVEL SECURITY;
ALTER TABLE job_run       FORCE ROW LEVEL SECURITY;
ALTER TABLE purge_history FORCE ROW LEVEL SECURITY;

-- USING filters reads/updates/deletes; WITH CHECK stops writing rows into another tenant
CREATE POLICY p_tenant ON upload        USING (tenant_id = app.ctx('tenant_id')) WITH CHECK (tenant_id = app.ctx('tenant_id'));
CREATE POLICY p_tenant ON upload_error  USING (tenant_id = app.ctx('tenant_id')) WITH CHECK (tenant_id = app.ctx('tenant_id'));
CREATE POLICY p_tenant ON edi_message   USING (tenant_id = app.ctx('tenant_id')) WITH CHECK (tenant_id = app.ctx('tenant_id'));
CREATE POLICY p_tenant ON job_run       USING (tenant_id = app.ctx('tenant_id')) WITH CHECK (tenant_id = app.ctx('tenant_id'));
CREATE POLICY p_tenant ON purge_history USING (tenant_id = app.ctx('tenant_id')) WITH CHECK (tenant_id = app.ctx('tenant_id'));

-- Catalogues are maintained by migrations / epl_admin; read-only for the service
REVOKE INSERT, UPDATE, DELETE ON job_definition, purge_rule FROM intg_app;

-- -----------------------------------------------------------------------------
-- Integration
-- -----------------------------------------------------------------------------
CREATE TABLE outbox_event (
    id              uuid         PRIMARY KEY DEFAULT gen_random_uuid(),
    aggregate_type  varchar(60)  NOT NULL,
    aggregate_id    varchar(100) NOT NULL,
    event_type      varchar(100) NOT NULL,       -- integration.upload.accepted, integration.purge.requested …
    tenant_id       uuid         NOT NULL REFERENCES iam.tenant (id),
    payload         jsonb        NOT NULL,
    created_at      timestamptz  NOT NULL DEFAULT now(),
    published_at    timestamptz
);
CREATE INDEX ix_outbox_unpublished ON outbox_event (created_at) WHERE published_at IS NULL;

-- Idempotent consumption (inbound.arrival.confirmed → EDI result, …)
CREATE TABLE processed_event (
    event_id      uuid         PRIMARY KEY,
    consumer      varchar(60)  NOT NULL,
    processed_at  timestamptz  NOT NULL DEFAULT now()
);

-- -----------------------------------------------------------------------------
-- Reference data
-- -----------------------------------------------------------------------------
INSERT INTO job_definition (code, name, schedule, description) VALUES
    ('PURGE',       'Data purge',        '0 2 * * *',    'Deletes data older than its retention (purge_rule)'),
    ('EDI_RECEIVE', 'EDI receive',       '*/5 * * * *',  'Polls partners for incoming EDI messages'),
    ('EDI_SEND',    'EDI send',          '*/5 * * * *',  'Sends pending EDI messages');

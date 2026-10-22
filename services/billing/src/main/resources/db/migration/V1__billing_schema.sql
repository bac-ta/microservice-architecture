-- =============================================================================
-- Billing database — owned by service-billing
-- PostgreSQL 16
--
-- Rates → charge lines (from inbound / outbound / inventory events and manual entry)
-- → monthly charge statement per bill-to → month-end close with stock balances
-- (storage charges).
--
-- Billing never reads other schemas: it builds charge lines and month-end balances from the
-- Kafka events it consumes (processed_event makes that idempotent; the charge flags come
-- from the transaction kind in the event payload).
--
-- tenant_id is a real foreign key to the iam registry (REFERENCES from iam/V4).
-- Runs as bill_owner after bootstrap.sql and the iam migrations.
-- =============================================================================

SET search_path = bill;

GRANT USAGE ON SCHEMA bill TO bill_app;
ALTER DEFAULT PRIVILEGES IN SCHEMA bill GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO bill_app;
ALTER DEFAULT PRIVILEGES IN SCHEMA bill GRANT USAGE, SELECT ON SEQUENCES TO bill_app;
-- cross-tenant admin / batch role (BYPASSRLS)
ALTER DEFAULT PRIVILEGES IN SCHEMA bill GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO epl_admin;

-- -----------------------------------------------------------------------------
-- Rate master: price per unit for a charge type, valid over a date range.
-- A new price is a new row with a later valid_from; ranges of one code must not overlap
-- (checked by the service when it closes the previous row).
-- -----------------------------------------------------------------------------
CREATE TABLE rate (
    id           uuid           PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id    uuid           NOT NULL REFERENCES iam.tenant (id),
    code         varchar(20)    NOT NULL,
    charge_type  varchar(12)    NOT NULL CHECK (charge_type IN ('IN', 'OUT', 'STORAGE', 'PROCESSING', 'OTHER')),
    name         varchar(100)   NOT NULL,
    unit         varchar(10)    NOT NULL,            -- CS, PC, M3, KG, PALLET …
    unit_price   numeric(15,4)  NOT NULL CHECK (unit_price >= 0),
    currency     char(3)        NOT NULL,            -- ISO 4217
    valid_from   date           NOT NULL,
    valid_to     date,
    created_at   timestamptz    NOT NULL DEFAULT now(),
    created_by   varchar(50)    NOT NULL,
    updated_at   timestamptz    NOT NULL DEFAULT now(),
    updated_by   varchar(50)    NOT NULL,
    deleted_at   timestamptz,                        -- DeleteRate: rates already used stay readable
    version      integer        NOT NULL DEFAULT 0,
    UNIQUE (tenant_id, code, valid_from),
    CHECK (valid_to IS NULL OR valid_to >= valid_from)
);
CREATE INDEX ix_rate_type ON rate (tenant_id, charge_type, valid_from) WHERE deleted_at IS NULL;

-- -----------------------------------------------------------------------------
-- Charge statement: one per tenant, bill-to and month
-- -----------------------------------------------------------------------------
CREATE TABLE charge_statement (
    id            uuid           PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id     uuid           NOT NULL REFERENCES iam.tenant (id),
    period        char(7)        NOT NULL CHECK (period ~ '^[0-9]{4}-(0[1-9]|1[0-2])$'),   -- YYYY-MM
    bill_to_code  varchar(20)    NOT NULL,
    status        varchar(8)     NOT NULL DEFAULT 'OPEN' CHECK (status IN ('OPEN', 'CLOSED')),
    currency      char(3)        NOT NULL,
    total_amount  numeric(15,2)  NOT NULL DEFAULT 0,    -- sum of charge_line.amount
    closed_at     timestamptz,
    created_at    timestamptz    NOT NULL DEFAULT now(),
    created_by    varchar(50)    NOT NULL,
    updated_at    timestamptz    NOT NULL DEFAULT now(),
    updated_by    varchar(50)    NOT NULL,
    version       integer        NOT NULL DEFAULT 0,
    UNIQUE (tenant_id, period, bill_to_code),
    UNIQUE (tenant_id, id),
    CHECK ((status = 'CLOSED') = (closed_at IS NOT NULL))
);

CREATE TABLE charge_line (
    id               uuid           PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id        uuid           NOT NULL,
    statement_id     uuid           NOT NULL,
    line_no          integer        NOT NULL,
    charge_type      varchar(12)    NOT NULL CHECK (charge_type IN ('IN', 'OUT', 'STORAGE', 'PROCESSING', 'OTHER')),
    rate_code        varchar(20),
    description      varchar(200),
    quantity         numeric(15,3)  NOT NULL,
    unit             varchar(10)    NOT NULL,
    unit_price       numeric(15,4)  NOT NULL,          -- copied from the rate valid on the charge date
    amount           numeric(15,2)  NOT NULL,
    currency         char(3)        NOT NULL,
    charge_date      date           NOT NULL,
    source_doc_no    varchar(20),                      -- shipment / arrival / operation number
    -- event that produced the line; NULL = entered manually on EPL-BL-210W
    source_event_id  uuid,
    created_at       timestamptz    NOT NULL DEFAULT now(),
    created_by       varchar(50)    NOT NULL,
    updated_at       timestamptz    NOT NULL DEFAULT now(),
    updated_by       varchar(50)    NOT NULL,
    version          integer        NOT NULL DEFAULT 0,
    FOREIGN KEY (tenant_id, statement_id) REFERENCES charge_statement (tenant_id, id) ON DELETE CASCADE,
    UNIQUE (statement_id, line_no)
);
-- one event charges a given rate once, even when it is delivered twice
CREATE UNIQUE INDEX ux_charge_line_event ON charge_line (source_event_id, rate_code) WHERE source_event_id IS NOT NULL;
CREATE INDEX ix_charge_line_doc ON charge_line (tenant_id, source_doc_no);

-- -----------------------------------------------------------------------------
-- Month-end close
-- -----------------------------------------------------------------------------
CREATE TABLE month_end_run (
    id           uuid          PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id    uuid          NOT NULL REFERENCES iam.tenant (id),
    period       char(7)       NOT NULL CHECK (period ~ '^[0-9]{4}-(0[1-9]|1[0-2])$'),
    status       varchar(12)   NOT NULL DEFAULT 'RUNNING' CHECK (status IN ('RUNNING', 'DONE', 'FAILED')),
    run_no       integer       NOT NULL DEFAULT 1,     -- > 1 when rerun
    started_at   timestamptz   NOT NULL DEFAULT now(),
    finished_at  timestamptz,
    message      varchar(1000),
    started_by   varchar(50)   NOT NULL,
    UNIQUE (tenant_id, period, run_no),
    CHECK ((status = 'RUNNING') = (finished_at IS NULL))
);
-- at most one run in progress per tenant and month
CREATE UNIQUE INDEX ux_month_end_run_running ON month_end_run (tenant_id, period) WHERE status = 'RUNNING';

-- Closing stock per product and sub-inventory, built from inventory events;
-- the next month's opening = this month's closing.
CREATE TABLE month_end_balance (
    tenant_id           uuid           NOT NULL REFERENCES iam.tenant (id),
    period              char(7)        NOT NULL CHECK (period ~ '^[0-9]{4}-(0[1-9]|1[0-2])$'),
    product_code        varchar(50)    NOT NULL,
    sub_inventory_code  varchar(20)    NOT NULL,
    opening_qty         numeric(15,3)  NOT NULL DEFAULT 0,
    in_qty              numeric(15,3)  NOT NULL DEFAULT 0,
    out_qty             numeric(15,3)  NOT NULL DEFAULT 0,
    adjust_qty          numeric(15,3)  NOT NULL DEFAULT 0,  -- signed
    closing_qty         numeric(15,3)  GENERATED ALWAYS AS (opening_qty + in_qty - out_qty + adjust_qty) STORED,
    -- storage base used by STORAGE rates (e.g. average or 3-period stock), filled at close
    storage_qty         numeric(15,3),
    updated_at          timestamptz    NOT NULL DEFAULT now(),
    PRIMARY KEY (tenant_id, period, product_code, sub_inventory_code)
);

-- -----------------------------------------------------------------------------
-- Row-Level Security
-- -----------------------------------------------------------------------------
ALTER TABLE rate              ENABLE ROW LEVEL SECURITY;
ALTER TABLE charge_statement  ENABLE ROW LEVEL SECURITY;
ALTER TABLE charge_line       ENABLE ROW LEVEL SECURITY;
ALTER TABLE month_end_run     ENABLE ROW LEVEL SECURITY;
ALTER TABLE month_end_balance ENABLE ROW LEVEL SECURITY;

ALTER TABLE rate              FORCE ROW LEVEL SECURITY;
ALTER TABLE charge_statement  FORCE ROW LEVEL SECURITY;
ALTER TABLE charge_line       FORCE ROW LEVEL SECURITY;
ALTER TABLE month_end_run     FORCE ROW LEVEL SECURITY;
ALTER TABLE month_end_balance FORCE ROW LEVEL SECURITY;

-- USING filters reads/updates/deletes; WITH CHECK stops writing rows into another tenant
CREATE POLICY p_tenant ON rate              USING (tenant_id = app.ctx('tenant_id')) WITH CHECK (tenant_id = app.ctx('tenant_id'));
CREATE POLICY p_tenant ON charge_statement  USING (tenant_id = app.ctx('tenant_id')) WITH CHECK (tenant_id = app.ctx('tenant_id'));
CREATE POLICY p_tenant ON charge_line       USING (tenant_id = app.ctx('tenant_id')) WITH CHECK (tenant_id = app.ctx('tenant_id'));
CREATE POLICY p_tenant ON month_end_run     USING (tenant_id = app.ctx('tenant_id')) WITH CHECK (tenant_id = app.ctx('tenant_id'));
CREATE POLICY p_tenant ON month_end_balance USING (tenant_id = app.ctx('tenant_id')) WITH CHECK (tenant_id = app.ctx('tenant_id'));

-- -----------------------------------------------------------------------------
-- Integration
-- -----------------------------------------------------------------------------
CREATE TABLE outbox_event (
    id              uuid         PRIMARY KEY DEFAULT gen_random_uuid(),
    aggregate_type  varchar(60)  NOT NULL,
    aggregate_id    varchar(100) NOT NULL,
    event_type      varchar(100) NOT NULL,       -- billing.month.closed, billing.statement.closed …
    tenant_id       uuid         NOT NULL REFERENCES iam.tenant (id),
    payload         jsonb        NOT NULL,
    created_at      timestamptz  NOT NULL DEFAULT now(),
    published_at    timestamptz
);
CREATE INDEX ix_outbox_unpublished ON outbox_event (created_at) WHERE published_at IS NULL;

-- Idempotent consumption of inbound.arrival.confirmed, outbound.shipment.confirmed, stock.adjusted …
CREATE TABLE processed_event (
    event_id      uuid         PRIMARY KEY,
    consumer      varchar(60)  NOT NULL,
    processed_at  timestamptz  NOT NULL DEFAULT now()
);

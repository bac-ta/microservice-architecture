-- =============================================================================
-- Inbound database — owned by service-inbound
-- PostgreSQL 16
--
-- Arrival plan → inspection → put-away → confirmation.
-- Every inspection result becomes one stock lot in inventory on confirmation:
--   receipt_no = arrival_no, receipt_line = line_no, receipt_seq = inspection_result.receipt_seq
-- (event inbound.arrival.confirmed, written to outbox_event in the confirming transaction).
--
-- tenant_id / warehouse_id are real foreign keys to the iam registry (REFERENCES from iam/V4).
-- product_code, location_code, sub_inventory_code, transaction_kind and party codes are
-- validated against service-masterdata over gRPC, with no FK.
--
-- Runs as inb_owner after bootstrap.sql and the iam migrations.
-- =============================================================================

SET search_path = inb;

GRANT USAGE ON SCHEMA inb TO inb_app;
ALTER DEFAULT PRIVILEGES IN SCHEMA inb GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO inb_app;
ALTER DEFAULT PRIVILEGES IN SCHEMA inb GRANT USAGE, SELECT ON SEQUENCES TO inb_app;
-- cross-tenant admin / batch role (BYPASSRLS)
ALTER DEFAULT PRIVILEGES IN SCHEMA inb GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO epl_admin;

-- -----------------------------------------------------------------------------
-- Arrival (receiving document)
-- -----------------------------------------------------------------------------
CREATE TABLE arrival (
    id                  uuid           PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id           uuid           NOT NULL,
    warehouse_id        uuid           NOT NULL,
    arrival_no          varchar(20)    NOT NULL,
    scheduled_date      date           NOT NULL,
    transaction_kind    varchar(3)     NOT NULL,     -- masterdata.transaction_kind.code
    status              varchar(16)    NOT NULL DEFAULT 'PLANNED' CHECK (status IN
                        ('PLANNED', 'INSPECTING', 'INSPECTED', 'CONFIRMED', 'CANCELLED')),
    arrived_date        date,
    confirmed_date      date,
    invoice_no          varchar(30),
    customer_ref        varchar(30),
    po_no               varchar(30),
    -- party snapshot as printed on the document: {"names":[...], "address":{...}}
    supplier_code       varchar(20),
    supplier            jsonb          NOT NULL DEFAULT '{}',
    bonded              boolean        NOT NULL DEFAULT false,
    bl_no               varchar(30),
    container_no        varchar(20),
    transport_code      varchar(10),
    return_shipment_no  varchar(20),                 -- set for customer returns
    cancel_reason_code  varchar(3),
    result_sent_at      timestamptz,                 -- SendArrivalResult
    result_channel      varchar(10)    CHECK (result_channel IN ('EDI', 'CSV', 'MAIL')),
    remarks             varchar(500),
    ext                 jsonb          NOT NULL DEFAULT '{}',
    created_at          timestamptz    NOT NULL DEFAULT now(),
    created_by          varchar(50)    NOT NULL,
    updated_at          timestamptz    NOT NULL DEFAULT now(),
    updated_by          varchar(50)    NOT NULL,
    version             integer        NOT NULL DEFAULT 0,
    FOREIGN KEY (tenant_id, warehouse_id) REFERENCES iam.tenant (id, warehouse_id),
    UNIQUE (tenant_id, arrival_no),
    UNIQUE (tenant_id, id),                          -- target for (tenant_id, arrival_id) FKs
    CHECK ((status = 'CONFIRMED') = (confirmed_date IS NOT NULL)),
    CHECK ((status = 'CANCELLED') = (cancel_reason_code IS NOT NULL))
);
CREATE INDEX ix_arrival_scheduled ON arrival (tenant_id, scheduled_date, status);
CREATE INDEX ix_arrival_invoice   ON arrival (tenant_id, invoice_no) WHERE invoice_no IS NOT NULL;
CREATE INDEX ix_arrival_customer_ref ON arrival (tenant_id, customer_ref) WHERE customer_ref IS NOT NULL;
CREATE INDEX ix_arrival_supplier  ON arrival (tenant_id, supplier_code);

CREATE TABLE arrival_line (
    id                     uuid           PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id              uuid           NOT NULL,
    arrival_id             uuid           NOT NULL,
    line_no                integer        NOT NULL,
    status                 varchar(16)    NOT NULL DEFAULT 'PLANNED' CHECK (status IN
                           ('PLANNED', 'INSPECTING', 'INSPECTED', 'CONFIRMED', 'CANCELLED')),
    inspected              boolean        NOT NULL DEFAULT false,
    product_code           varchar(50)    NOT NULL,
    product_name           varchar(200),                 -- snapshot for the document
    origin_country         char(2),
    supplier_product_code  varchar(50),
    units_per_case         integer,
    units_per_ball         integer,
    planned_case_qty       integer        NOT NULL DEFAULT 0,
    planned_ball_qty       integer        NOT NULL DEFAULT 0,
    planned_piece_qty      numeric(12,3)  NOT NULL DEFAULT 0,
    planned_total_qty      numeric(12,3)  NOT NULL,
    received_total_qty     numeric(12,3)  NOT NULL DEFAULT 0,   -- sum of inspection_result.total_qty
    sub_inventory_code     varchar(20),
    pick_key1              varchar(30),                  -- requested; actual values per inspection_result
    pick_key2              varchar(30),
    pick_key3              varchar(30),
    pick_key4              varchar(30),
    pick_key5              varchar(30),
    pick_key6              varchar(30),
    pick_key7              varchar(30),
    on_hold                boolean        NOT NULL DEFAULT false,
    customer_line_seq      integer,
    remarks                varchar(500),
    ext                    jsonb          NOT NULL DEFAULT '{}',   -- CIT / COIL customer fields
    created_at             timestamptz    NOT NULL DEFAULT now(),
    created_by             varchar(50)    NOT NULL,
    updated_at             timestamptz    NOT NULL DEFAULT now(),
    updated_by             varchar(50)    NOT NULL,
    version                integer        NOT NULL DEFAULT 0,
    FOREIGN KEY (tenant_id, arrival_id) REFERENCES arrival (tenant_id, id) ON DELETE CASCADE,
    UNIQUE (arrival_id, line_no),
    UNIQUE (tenant_id, id),
    CONSTRAINT ck_arrival_line_qty_non_negative CHECK (planned_total_qty >= 0 AND received_total_qty >= 0)
);
CREATE INDEX ix_arrival_line_product ON arrival_line (tenant_id, product_code);

-- One recorded result = one future stock lot (receipt_seq). Several per line when the
-- goods arrive with different lots / expiry dates or on several pallets (cargo_id).
CREATE TABLE inspection_result (
    id                  uuid           PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id           uuid           NOT NULL,
    arrival_line_id     uuid           NOT NULL,
    receipt_seq         integer        NOT NULL,
    cargo_id            varchar(20),                 -- pallet / cargo label scanned on HHT
    units_per_case      integer,
    units_per_ball      integer,
    case_qty            integer        NOT NULL DEFAULT 0,
    ball_qty            integer        NOT NULL DEFAULT 0,
    piece_qty           numeric(12,3)  NOT NULL DEFAULT 0,
    total_qty           numeric(12,3)  NOT NULL CHECK (total_qty > 0),
    pick_key1           varchar(30),                 -- actual lot / expiry
    pick_key2           varchar(30),
    pick_key3           varchar(30),
    pick_key4           varchar(30),
    pick_key5           varchar(30),
    pick_key6           varchar(30),
    pick_key7           varchar(30),
    sub_inventory_code  varchar(20)    NOT NULL,
    on_hold             boolean        NOT NULL DEFAULT false,
    putaway_total_qty   numeric(12,3)  NOT NULL DEFAULT 0,   -- sum of putaway.total_qty
    inspected_at        timestamptz    NOT NULL DEFAULT now(),
    inspected_by        varchar(50)    NOT NULL,
    version             integer        NOT NULL DEFAULT 0,
    FOREIGN KEY (tenant_id, arrival_line_id) REFERENCES arrival_line (tenant_id, id) ON DELETE CASCADE,
    UNIQUE (arrival_line_id, receipt_seq),
    UNIQUE (tenant_id, id),
    CHECK (putaway_total_qty >= 0 AND putaway_total_qty <= total_qty)
);
CREATE UNIQUE INDEX ux_inspection_result_cargo ON inspection_result (tenant_id, cargo_id) WHERE cargo_id IS NOT NULL;

-- Put-away of an inspected cargo to a location (HHT). A cargo can be split over locations.
CREATE TABLE putaway (
    id                    uuid           PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id             uuid           NOT NULL,
    inspection_result_id  uuid           NOT NULL,
    location_code         varchar(18)    NOT NULL,
    case_qty              integer        NOT NULL DEFAULT 0,
    ball_qty              integer        NOT NULL DEFAULT 0,
    piece_qty             numeric(12,3)  NOT NULL DEFAULT 0,
    total_qty             numeric(12,3)  NOT NULL CHECK (total_qty > 0),
    put_at                timestamptz    NOT NULL DEFAULT now(),
    put_by                varchar(50)    NOT NULL,
    FOREIGN KEY (tenant_id, inspection_result_id) REFERENCES inspection_result (tenant_id, id) ON DELETE CASCADE
);
CREATE INDEX ix_putaway_result ON putaway (inspection_result_id);

-- Serial numbers captured at receipt (RegisterSerials); copied to inv.serial_number on confirmation
CREATE TABLE arrival_serial (
    tenant_id        uuid          NOT NULL,
    arrival_line_id  uuid          NOT NULL,
    serial_no        varchar(50)   NOT NULL,
    created_at       timestamptz   NOT NULL DEFAULT now(),
    created_by       varchar(50)   NOT NULL,
    PRIMARY KEY (arrival_line_id, serial_no),
    FOREIGN KEY (tenant_id, arrival_line_id) REFERENCES arrival_line (tenant_id, id) ON DELETE CASCADE
);

-- -----------------------------------------------------------------------------
-- Document numbering (ARRIVAL …), same rules as inv.number_sequence
-- -----------------------------------------------------------------------------
CREATE TABLE number_sequence (
    tenant_id      uuid         NOT NULL REFERENCES iam.tenant (id),
    code           varchar(10)  NOT NULL,
    prefix         varchar(3)   NOT NULL DEFAULT '',
    start_value    bigint       NOT NULL DEFAULT 1,
    end_value      bigint       NOT NULL,
    current_value  bigint       NOT NULL,
    pad_length     smallint     NOT NULL DEFAULT 6,
    allow_wrap     boolean      NOT NULL DEFAULT true,
    updated_at     timestamptz  NOT NULL DEFAULT now(),
    PRIMARY KEY (tenant_id, code),
    CHECK (start_value <= current_value AND current_value <= end_value)
);

-- -----------------------------------------------------------------------------
-- Integration
-- -----------------------------------------------------------------------------
CREATE TABLE outbox_event (
    id              uuid         PRIMARY KEY DEFAULT gen_random_uuid(),
    aggregate_type  varchar(60)  NOT NULL,
    aggregate_id    varchar(100) NOT NULL,
    event_type      varchar(100) NOT NULL,       -- inbound.arrival.confirmed, inbound.arrival.cancelled …
    tenant_id       uuid         NOT NULL REFERENCES iam.tenant (id),
    payload         jsonb        NOT NULL,
    created_at      timestamptz  NOT NULL DEFAULT now(),
    published_at    timestamptz
);

-- Idempotent consumption of Kafka events from other services (e.g. outbound.shipment.returned)
CREATE TABLE processed_event (
    event_id      uuid         PRIMARY KEY,
    consumer      varchar(60)  NOT NULL,
    processed_at  timestamptz  NOT NULL DEFAULT now()
);

-- -----------------------------------------------------------------------------
-- Row-Level Security
-- -----------------------------------------------------------------------------
ALTER TABLE arrival           ENABLE ROW LEVEL SECURITY;
ALTER TABLE arrival_line      ENABLE ROW LEVEL SECURITY;
ALTER TABLE inspection_result ENABLE ROW LEVEL SECURITY;
ALTER TABLE putaway           ENABLE ROW LEVEL SECURITY;
ALTER TABLE arrival_serial    ENABLE ROW LEVEL SECURITY;
ALTER TABLE number_sequence   ENABLE ROW LEVEL SECURITY;

ALTER TABLE arrival           FORCE ROW LEVEL SECURITY;
ALTER TABLE arrival_line      FORCE ROW LEVEL SECURITY;
ALTER TABLE inspection_result FORCE ROW LEVEL SECURITY;
ALTER TABLE putaway           FORCE ROW LEVEL SECURITY;
ALTER TABLE arrival_serial    FORCE ROW LEVEL SECURITY;
ALTER TABLE number_sequence   FORCE ROW LEVEL SECURITY;

-- USING filters reads/updates/deletes; WITH CHECK stops writing rows into another tenant
CREATE POLICY p_tenant ON arrival           USING (tenant_id = app.ctx('tenant_id')) WITH CHECK (tenant_id = app.ctx('tenant_id'));
CREATE POLICY p_tenant ON arrival_line      USING (tenant_id = app.ctx('tenant_id')) WITH CHECK (tenant_id = app.ctx('tenant_id'));
CREATE POLICY p_tenant ON inspection_result USING (tenant_id = app.ctx('tenant_id')) WITH CHECK (tenant_id = app.ctx('tenant_id'));
CREATE POLICY p_tenant ON putaway           USING (tenant_id = app.ctx('tenant_id')) WITH CHECK (tenant_id = app.ctx('tenant_id'));
CREATE POLICY p_tenant ON arrival_serial    USING (tenant_id = app.ctx('tenant_id')) WITH CHECK (tenant_id = app.ctx('tenant_id'));
CREATE POLICY p_tenant ON number_sequence   USING (tenant_id = app.ctx('tenant_id')) WITH CHECK (tenant_id = app.ctx('tenant_id'));

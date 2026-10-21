-- =============================================================================
-- Inventory database — owned by service-inventory
-- PostgreSQL 16
--
-- tenant_id / warehouse_id are real foreign keys to the iam registry.
-- No foreign keys into md: product_code, location_code, sub_inventory_code and
-- transaction_kind are validated by the service against service-masterdata
-- (gRPC + cache), so stock history survives master changes and the two
-- services can be deployed and migrated independently.
--
-- Runs as inv_owner after bootstrap.sql and the iam migrations.
-- =============================================================================

SET search_path = inv;

GRANT USAGE ON SCHEMA inv TO inv_app;
ALTER DEFAULT PRIVILEGES IN SCHEMA inv GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO inv_app;
ALTER DEFAULT PRIVILEGES IN SCHEMA inv GRANT USAGE, SELECT ON SEQUENCES TO inv_app;
-- cross-tenant admin / batch role (BYPASSRLS)
ALTER DEFAULT PRIVILEGES IN SCHEMA inv GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO epl_admin;

-- -----------------------------------------------------------------------------
-- Stock: one row per receipt lot at one location.
-- A partial relocation splits the row and allocates a new receipt_seq.
-- -----------------------------------------------------------------------------
CREATE TABLE stock (
    id                  uuid           PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id           uuid           NOT NULL,
    warehouse_id        uuid           NOT NULL,
    receipt_no          varchar(20)    NOT NULL,
    receipt_line        integer        NOT NULL,
    receipt_seq         integer        NOT NULL,
    stock_kind          varchar(1)     NOT NULL,
    received_on         date           NOT NULL,
    product_code        varchar(50)    NOT NULL,
    origin_country      char(2),
    pick_key1           varchar(30),                 -- meaning defined by masterdata.picking_key_def
    pick_key2           varchar(30),
    pick_key3           varchar(30),
    pick_key4           varchar(30),
    pick_key5           varchar(30),
    pick_key6           varchar(30),
    pick_key7           varchar(30),
    location_code       varchar(18)    NOT NULL,
    area_code           varchar(3)     NOT NULL,     -- denormalised from location for range queries
    sub_inventory_code  varchar(20)    NOT NULL,
    -- quantities in pieces; physical = available + allocated
    physical_qty        numeric(12,3)  NOT NULL,
    available_qty       numeric(12,3)  NOT NULL,
    allocated_qty       numeric(12,3)  NOT NULL DEFAULT 0,
    original_qty        numeric(12,3),
    units_per_case      integer,
    units_per_ball      integer,
    uom_piece           varchar(3),
    uom_case            varchar(3),
    uom_ball            varchar(3),
    bonded              boolean        NOT NULL DEFAULT false,
    on_hold             boolean        NOT NULL DEFAULT false,
    locked              boolean        NOT NULL DEFAULT false,
    ext                 jsonb          NOT NULL DEFAULT '{}',     -- PSSA fields, CIT1..15, CFG*
    created_at          timestamptz    NOT NULL DEFAULT now(),
    created_by          varchar(50)    NOT NULL,
    updated_at          timestamptz    NOT NULL DEFAULT now(),
    updated_by          varchar(50)    NOT NULL,
    version             integer        NOT NULL DEFAULT 0,        -- optimistic lock
    FOREIGN KEY (tenant_id, warehouse_id) REFERENCES iam.tenant (id, warehouse_id),
    UNIQUE (tenant_id, receipt_no, receipt_line, receipt_seq),
    CONSTRAINT ck_stock_qty_non_negative CHECK (physical_qty >= 0 AND available_qty >= 0 AND allocated_qty >= 0),
    CONSTRAINT ck_stock_qty_balance      CHECK (physical_qty = available_qty + allocated_qty)
);
CREATE INDEX ix_stock_product  ON stock (tenant_id, product_code);
CREATE INDEX ix_stock_location ON stock (warehouse_id, location_code);
CREATE INDEX ix_stock_area     ON stock (tenant_id, area_code, location_code);
-- zero stock rows are kept; keep them out of hot indexes instead
CREATE INDEX ix_stock_available ON stock (tenant_id, product_code, received_on)
    WHERE available_qty > 0 AND NOT on_hold AND NOT locked;

-- -----------------------------------------------------------------------------
-- Movement ledger (append-only). One business operation = operation_no;
-- a transfer writes a FROM leg and a TO leg.
-- -----------------------------------------------------------------------------
CREATE TABLE stock_movement (
    id                  uuid           NOT NULL DEFAULT gen_random_uuid(),
    tenant_id           uuid           NOT NULL,
    warehouse_id        uuid           NOT NULL,
    operation_no        varchar(20)    NOT NULL,
    operation_line      integer        NOT NULL,
    operation_seq       integer        NOT NULL,
    movement_type       varchar(30)    NOT NULL CHECK (movement_type IN
                        ('ADJUST', 'RELOCATE', 'PICK_KEY_CHANGE', 'SUB_INVENTORY_CHANGE',
                         'DIVIDE', 'HOLD_CHANGE', 'STOCKTAKE', 'RECEIPT', 'SHIPMENT')),
    leg                 varchar(6)     NOT NULL CHECK (leg IN ('FROM', 'TO', 'SINGLE')),
    transaction_kind    varchar(3)     NOT NULL,     -- masterdata.transaction_kind.code
    operation_date      date           NOT NULL,     -- business date in warehouse time zone
    stock_id            uuid,                        -- NULL if the lot no longer exists
    receipt_no          varchar(20)    NOT NULL,
    receipt_line        integer        NOT NULL,
    receipt_seq         integer        NOT NULL,
    product_code        varchar(50)    NOT NULL,
    origin_country      char(2),
    pick_key1           varchar(30),
    pick_key2           varchar(30),
    pick_key3           varchar(30),
    pick_key4           varchar(30),
    pick_key5           varchar(30),
    pick_key6           varchar(30),
    pick_key7           varchar(30),
    location_code       varchar(18)    NOT NULL,
    area_code           varchar(3)     NOT NULL,
    sub_inventory_code  varchar(20)    NOT NULL,
    on_hold             boolean        NOT NULL DEFAULT false,
    units_per_case      integer,
    units_per_ball      integer,
    case_qty            integer,
    ball_qty            integer,
    piece_qty           numeric(12,3),
    total_qty           numeric(12,3)  NOT NULL,     -- signed: + in, - out
    reason_code         varchar(3),
    remarks             varchar(500),
    source_ref          varchar(100),                -- stocktake no, upload id, gRPC request id …
    ext                 jsonb          NOT NULL DEFAULT '{}',
    created_at          timestamptz    NOT NULL DEFAULT now(),
    created_by          varchar(50)    NOT NULL,
    PRIMARY KEY (id, operation_date),
    FOREIGN KEY (tenant_id, warehouse_id) REFERENCES iam.tenant (id, warehouse_id),
    UNIQUE (tenant_id, operation_no, operation_line, operation_seq, leg, operation_date)
) PARTITION BY RANGE (operation_date);
-- Yearly or monthly partitions created by a job; default catches the rest.
CREATE TABLE stock_movement_default PARTITION OF stock_movement DEFAULT;

CREATE INDEX ix_movement_product  ON stock_movement (tenant_id, product_code, operation_date);
CREATE INDEX ix_movement_location ON stock_movement (tenant_id, location_code, operation_date);
CREATE INDEX ix_movement_receipt  ON stock_movement (tenant_id, receipt_no, receipt_line, receipt_seq);

-- -----------------------------------------------------------------------------
-- Stocktake
-- -----------------------------------------------------------------------------
CREATE TABLE stocktake (
    id                       uuid           PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id                uuid           NOT NULL,
    warehouse_id             uuid           NOT NULL,
    stocktake_no             varchar(20)    NOT NULL,
    stocktake_date           date           NOT NULL,
    confirmed_date           date,
    status                   varchar(16)    NOT NULL DEFAULT 'INSTRUCTED' CHECK (status IN
                             ('INSTRUCTED', 'COUNTING', 'COUNTED', 'CONFIRMED', 'CANCELLED')),
    kind                     varchar(2)     NOT NULL,     -- full / cycle / ABC …
    target_kind              varchar(2)     NOT NULL,
    zero_stock_check_kind    varchar(2),
    include_empty_locations  boolean        NOT NULL DEFAULT false,
    print_finish_label       boolean        NOT NULL DEFAULT false,
    show_counted_qty         boolean        NOT NULL DEFAULT false,
    update_stock_on_confirm  boolean        NOT NULL DEFAULT true,
    -- selection criteria: location ranges, product/group, receipt-date range,
    -- sub-inventory, hold, pick keys, ABC classes and thresholds
    criteria                 jsonb          NOT NULL DEFAULT '{}',
    planned_qty              numeric(15,3)  NOT NULL DEFAULT 0,
    counted_qty              numeric(15,3)  NOT NULL DEFAULT 0,
    finish_label_counter     integer        NOT NULL DEFAULT 0,
    remarks                  varchar(500),
    created_at               timestamptz    NOT NULL DEFAULT now(),
    created_by               varchar(50)    NOT NULL,
    updated_at               timestamptz    NOT NULL DEFAULT now(),
    updated_by               varchar(50)    NOT NULL,
    version                  integer        NOT NULL DEFAULT 0,
    FOREIGN KEY (tenant_id, warehouse_id) REFERENCES iam.tenant (id, warehouse_id),
    UNIQUE (tenant_id, stocktake_no),
    UNIQUE (tenant_id, id),
    CHECK ((status = 'CONFIRMED') = (confirmed_date IS NOT NULL))
);
CREATE INDEX ix_stocktake_status ON stocktake (tenant_id, status, stocktake_date);

CREATE TABLE stocktake_line (
    id                  uuid           PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id           uuid           NOT NULL REFERENCES iam.tenant (id),
    stocktake_id        uuid           NOT NULL,
    count_no            integer        NOT NULL DEFAULT 1,
    entry_kind          varchar(2)     NOT NULL,     -- PC / HHT / upload
    data_kind           varchar(2)     NOT NULL,     -- from stock / found / empty loc
    stock_id            uuid,                        -- NULL for found stock or empty location
    receipt_no          varchar(20),
    receipt_line        integer,
    receipt_seq         integer,
    stock_kind          varchar(1),
    received_on         date,
    product_code        varchar(50),
    origin_country      char(2),
    pick_key1           varchar(30),
    pick_key2           varchar(30),
    pick_key3           varchar(30),
    pick_key4           varchar(30),
    pick_key5           varchar(30),
    pick_key6           varchar(30),
    pick_key7           varchar(30),
    location_code       varchar(18)    NOT NULL,
    zone_code           varchar(2),
    area_code           varchar(3)     NOT NULL,
    sub_inventory_code  varchar(20),
    on_hold             boolean        NOT NULL DEFAULT false,
    bonded              boolean        NOT NULL DEFAULT false,
    units_per_case      integer,
    units_per_ball      integer,
    -- system (book) quantity at instruction time
    book_case_qty       integer        NOT NULL DEFAULT 0,
    book_ball_qty       integer        NOT NULL DEFAULT 0,
    book_piece_qty      numeric(12,3)  NOT NULL DEFAULT 0,
    book_total_qty      numeric(12,3)  NOT NULL DEFAULT 0,
    -- counted quantity (NULL until counted)
    counted_case_qty    integer,
    counted_ball_qty    integer,
    counted_piece_qty   numeric(12,3),
    counted_total_qty   numeric(12,3),
    diff_total_qty      numeric(12,3)  GENERATED ALWAYS AS (counted_total_qty - book_total_qty) STORED,
    counted_at          timestamptz,
    counted_by          varchar(50),
    remarks             varchar(500),
    created_at          timestamptz    NOT NULL DEFAULT now(),
    created_by          varchar(50)    NOT NULL,
    updated_at          timestamptz    NOT NULL DEFAULT now(),
    updated_by          varchar(50)    NOT NULL,
    version             integer        NOT NULL DEFAULT 0,
    -- same tenant as the header
    FOREIGN KEY (tenant_id, stocktake_id) REFERENCES stocktake (tenant_id, id) ON DELETE CASCADE
);
CREATE INDEX ix_stocktake_line_header   ON stocktake_line (stocktake_id, location_code);
CREATE INDEX ix_stocktake_line_diff     ON stocktake_line (stocktake_id) WHERE diff_total_qty <> 0;

-- -----------------------------------------------------------------------------
-- Serial numbers
-- -----------------------------------------------------------------------------
CREATE TABLE serial_number (
    id                  uuid           PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id           uuid           NOT NULL REFERENCES iam.tenant (id),
    serial_no           varchar(50)    NOT NULL,
    product_code        varchar(50)    NOT NULL,
    origin_country      char(2),
    receipt_no          varchar(20),
    receipt_line        integer,
    receipt_seq         integer,
    status              varchar(1)     NOT NULL,     -- in stock / shipped …
    capture_kind        varchar(1),
    qty                 numeric(12,3)  NOT NULL DEFAULT 1,
    -- last in/out document that touched the serial
    doc_no              varchar(20),
    doc_line            integer,
    doc_seq             integer,
    doc_kind            varchar(2),                  -- AV / SP
    transaction_kind    varchar(3),
    transaction_date    date,
    carton_id           varchar(20),
    from_edi            boolean        NOT NULL DEFAULT false,
    edi_received_batch  varchar(30),
    edi_received_on     date,
    edi_sent_batch      varchar(30),
    edi_sent_on         date,
    ext                 jsonb          NOT NULL DEFAULT '{}',
    created_at          timestamptz    NOT NULL DEFAULT now(),
    created_by          varchar(50)    NOT NULL,
    updated_at          timestamptz    NOT NULL DEFAULT now(),
    updated_by          varchar(50)    NOT NULL,
    version             integer        NOT NULL DEFAULT 0,
    UNIQUE (tenant_id, product_code, serial_no)
);
CREATE INDEX ix_serial_receipt ON serial_number (tenant_id, receipt_no, receipt_line, receipt_seq);
CREATE INDEX ix_serial_doc     ON serial_number (tenant_id, doc_kind, doc_no);

-- -----------------------------------------------------------------------------
-- Document numbering (inventory codes only).
-- Take a number with:
--   UPDATE number_sequence SET current_value = <wrap logic> ... RETURNING ...
-- inside the business transaction (row lock serialises concurrent callers).
-- -----------------------------------------------------------------------------
CREATE TABLE number_sequence (
    tenant_id      uuid         NOT NULL REFERENCES iam.tenant (id),
    code           varchar(10)  NOT NULL,          -- STOCKTAKE, RELOCATION, ADJUST …
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
-- Row-Level Security
-- -----------------------------------------------------------------------------
ALTER TABLE stock           ENABLE ROW LEVEL SECURITY;
ALTER TABLE stock_movement  ENABLE ROW LEVEL SECURITY;
ALTER TABLE stocktake       ENABLE ROW LEVEL SECURITY;
ALTER TABLE stocktake_line  ENABLE ROW LEVEL SECURITY;
ALTER TABLE serial_number   ENABLE ROW LEVEL SECURITY;
ALTER TABLE number_sequence ENABLE ROW LEVEL SECURITY;

ALTER TABLE stock           FORCE ROW LEVEL SECURITY;
ALTER TABLE stock_movement  FORCE ROW LEVEL SECURITY;
ALTER TABLE stocktake       FORCE ROW LEVEL SECURITY;
ALTER TABLE stocktake_line  FORCE ROW LEVEL SECURITY;
ALTER TABLE serial_number   FORCE ROW LEVEL SECURITY;
ALTER TABLE number_sequence FORCE ROW LEVEL SECURITY;

-- USING filters reads/updates/deletes; WITH CHECK stops writing rows into another tenant
CREATE POLICY p_tenant ON stock           USING (tenant_id = app.ctx('tenant_id')) WITH CHECK (tenant_id = app.ctx('tenant_id'));
CREATE POLICY p_tenant ON stock_movement  USING (tenant_id = app.ctx('tenant_id')) WITH CHECK (tenant_id = app.ctx('tenant_id'));
CREATE POLICY p_tenant ON stocktake       USING (tenant_id = app.ctx('tenant_id')) WITH CHECK (tenant_id = app.ctx('tenant_id'));
CREATE POLICY p_tenant ON stocktake_line  USING (tenant_id = app.ctx('tenant_id')) WITH CHECK (tenant_id = app.ctx('tenant_id'));
CREATE POLICY p_tenant ON serial_number   USING (tenant_id = app.ctx('tenant_id')) WITH CHECK (tenant_id = app.ctx('tenant_id'));
CREATE POLICY p_tenant ON number_sequence USING (tenant_id = app.ctx('tenant_id')) WITH CHECK (tenant_id = app.ctx('tenant_id'));

-- -----------------------------------------------------------------------------
-- Integration
-- -----------------------------------------------------------------------------
CREATE TABLE outbox_event (
    id              uuid         PRIMARY KEY DEFAULT gen_random_uuid(),
    aggregate_type  varchar(60)  NOT NULL,
    aggregate_id    varchar(100) NOT NULL,
    event_type      varchar(100) NOT NULL,       -- stock.adjusted, stocktake.confirmed …
    tenant_id       uuid         NOT NULL REFERENCES iam.tenant (id),
    payload         jsonb        NOT NULL,
    created_at      timestamptz  NOT NULL DEFAULT now(),
    published_at    timestamptz
);
CREATE INDEX ix_outbox_unpublished ON outbox_event (created_at) WHERE published_at IS NULL;

-- Idempotent consumption of Kafka events from other services (e.g. shipment.confirmed)
CREATE TABLE processed_event (
    event_id      uuid         PRIMARY KEY,
    consumer      varchar(60)  NOT NULL,
    processed_at  timestamptz  NOT NULL DEFAULT now()
);

-- The movement ledger is append-only for the runtime role
REVOKE UPDATE, DELETE ON stock_movement, stock_movement_default FROM inv_app;

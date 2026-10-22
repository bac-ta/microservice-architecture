-- =============================================================================
-- Outbound database — owned by service-outbound
-- PostgreSQL 16
--
-- Shipping order → allocation → picking → packing → confirmation → result sending.
-- Allocation reserves stock lots in inventory over gRPC; allocation.stock_id is the
-- inv.stock id, kept without a FK (other schema, the lot may be split or deleted later).
-- Confirmation writes outbound.shipment.confirmed to outbox_event; inventory consumes it.
--
-- tenant_id / warehouse_id are real foreign keys to the iam registry (REFERENCES from iam/V4).
-- product, location, sub-inventory, transaction kind, carrier and party codes are
-- validated against service-masterdata over gRPC, with no FK.
--
-- Runs as outb_owner after bootstrap.sql and the iam migrations.
-- =============================================================================

SET search_path = outb;

GRANT USAGE ON SCHEMA outb TO outb_app;
ALTER DEFAULT PRIVILEGES IN SCHEMA outb GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO outb_app;
ALTER DEFAULT PRIVILEGES IN SCHEMA outb GRANT USAGE, SELECT ON SEQUENCES TO outb_app;
-- cross-tenant admin / batch role (BYPASSRLS)
ALTER DEFAULT PRIVILEGES IN SCHEMA outb GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO epl_admin;

-- -----------------------------------------------------------------------------
-- Picking batch: one pick list for one or many shipments (total picking)
-- -----------------------------------------------------------------------------
CREATE TABLE picking_batch (
    id                uuid          PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id         uuid          NOT NULL,
    warehouse_id      uuid          NOT NULL,
    batch_no          varchar(20)   NOT NULL,
    total_picking     boolean       NOT NULL DEFAULT false,
    total_picking_no  varchar(20),
    status            varchar(16)   NOT NULL DEFAULT 'CREATED' CHECK (status IN
                      ('CREATED', 'PICKING', 'PICKED', 'CANCELLED')),
    created_at        timestamptz   NOT NULL DEFAULT now(),
    created_by        varchar(50)   NOT NULL,
    updated_at        timestamptz   NOT NULL DEFAULT now(),
    updated_by        varchar(50)   NOT NULL,
    version           integer       NOT NULL DEFAULT 0,
    FOREIGN KEY (tenant_id, warehouse_id) REFERENCES iam.tenant (id, warehouse_id),
    UNIQUE (tenant_id, batch_no),
    UNIQUE (tenant_id, id),
    CHECK (total_picking = (total_picking_no IS NOT NULL))
);

-- -----------------------------------------------------------------------------
-- Shipment (shipping order)
-- -----------------------------------------------------------------------------
CREATE TABLE shipment (
    id                      uuid           PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id               uuid           NOT NULL,
    warehouse_id            uuid           NOT NULL,
    shipment_no             varchar(20)    NOT NULL,
    scheduled_date          date           NOT NULL,
    transaction_kind        varchar(3)     NOT NULL,     -- masterdata.transaction_kind.code
    status                  varchar(20)    NOT NULL DEFAULT 'ORDERED' CHECK (status IN
                            ('ORDERED', 'PARTIALLY_ALLOCATED', 'ALLOCATED', 'PICKING', 'PICKED',
                             'PACKED', 'SHIPPED', 'CANCELLED')),
    shipped_date            date,
    confirmed_date          date,
    picking_batch_id        uuid,
    invoice_no              varchar(30),
    customer_ref            varchar(30),
    po_no                   varchar(30),
    -- party snapshots as printed on the documents: {"names":[...], "address":{...}}
    ship_to_code            varchar(20),
    ship_to                 jsonb          NOT NULL DEFAULT '{}',
    final_ship_to_code      varchar(20),
    final_ship_to           jsonb          NOT NULL DEFAULT '{}',
    shipper_code            varchar(20),                 -- consignor (SP_*)
    shipper                 jsonb          NOT NULL DEFAULT '{}',
    bill_to_code            varchar(20),
    bill_to                 jsonb          NOT NULL DEFAULT '{}',
    carrier_code            varchar(10),
    warehouse_instructions  varchar(500),
    delivery_note_remarks   varchar(500),
    waybill_remarks         varchar(500),
    total_weight            numeric(12,3),               -- kg
    total_volume            numeric(12,4),               -- m3
    cancel_reason_code      varchar(3),
    result_sent_at          timestamptz,                 -- SendShipmentResult
    result_channel          varchar(10)    CHECK (result_channel IN ('EDI', 'CSV', 'MAIL')),
    ext                     jsonb          NOT NULL DEFAULT '{}',
    created_at              timestamptz    NOT NULL DEFAULT now(),
    created_by              varchar(50)    NOT NULL,
    updated_at              timestamptz    NOT NULL DEFAULT now(),
    updated_by              varchar(50)    NOT NULL,
    version                 integer        NOT NULL DEFAULT 0,
    FOREIGN KEY (tenant_id, warehouse_id) REFERENCES iam.tenant (id, warehouse_id),
    FOREIGN KEY (tenant_id, picking_batch_id) REFERENCES picking_batch (tenant_id, id),
    UNIQUE (tenant_id, shipment_no),
    UNIQUE (tenant_id, id),
    CHECK ((status = 'SHIPPED') = (confirmed_date IS NOT NULL)),
    CHECK ((status = 'CANCELLED') = (cancel_reason_code IS NOT NULL))
);
CREATE INDEX ix_shipment_scheduled    ON shipment (tenant_id, scheduled_date, status);
CREATE INDEX ix_shipment_ship_to      ON shipment (tenant_id, ship_to_code);
CREATE INDEX ix_shipment_batch        ON shipment (picking_batch_id) WHERE picking_batch_id IS NOT NULL;
CREATE INDEX ix_shipment_invoice      ON shipment (tenant_id, invoice_no) WHERE invoice_no IS NOT NULL;
CREATE INDEX ix_shipment_customer_ref ON shipment (tenant_id, customer_ref) WHERE customer_ref IS NOT NULL;

CREATE TABLE shipment_line (
    id                      uuid           PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id               uuid           NOT NULL,
    shipment_id             uuid           NOT NULL,
    line_no                 integer        NOT NULL,
    status                  varchar(20)    NOT NULL DEFAULT 'ORDERED' CHECK (status IN
                            ('ORDERED', 'PARTIALLY_ALLOCATED', 'ALLOCATED', 'PICKED', 'PACKED',
                             'SHIPPED', 'CANCELLED')),
    product_code            varchar(50)    NOT NULL,
    product_name            varchar(200),                -- snapshot for the documents
    origin_country          char(2),
    units_per_case          integer,
    units_per_ball          integer,
    ordered_case_qty        integer        NOT NULL DEFAULT 0,
    ordered_ball_qty        integer        NOT NULL DEFAULT 0,
    ordered_piece_qty       numeric(12,3)  NOT NULL DEFAULT 0,
    ordered_total_qty       numeric(12,3)  NOT NULL CHECK (ordered_total_qty > 0),
    allocated_total_qty     numeric(12,3)  NOT NULL DEFAULT 0,   -- sum of live allocations
    shipped_total_qty       numeric(12,3)  NOT NULL DEFAULT 0,
    sub_inventory_code      varchar(20),
    pick_key1               varchar(30),                 -- requested lot / expiry, NULL = any
    pick_key2               varchar(30),
    pick_key3               varchar(30),
    pick_key4               varchar(30),
    pick_key5               varchar(30),
    pick_key6               varchar(30),
    pick_key7               varchar(30),
    allocation_kind         varchar(10),                 -- FIFO, FEFO, LOT …
    not_shippable           boolean        NOT NULL DEFAULT false,
    customer_line_seq       integer,
    warehouse_instructions  varchar(500),
    delivery_note_remarks   varchar(500),
    ext                     jsonb          NOT NULL DEFAULT '{}',
    created_at              timestamptz    NOT NULL DEFAULT now(),
    created_by              varchar(50)    NOT NULL,
    updated_at              timestamptz    NOT NULL DEFAULT now(),
    updated_by              varchar(50)    NOT NULL,
    version                 integer        NOT NULL DEFAULT 0,
    FOREIGN KEY (tenant_id, shipment_id) REFERENCES shipment (tenant_id, id) ON DELETE CASCADE,
    UNIQUE (shipment_id, line_no),
    UNIQUE (tenant_id, id),
    CHECK (allocated_total_qty >= 0 AND allocated_total_qty <= ordered_total_qty),
    CHECK (shipped_total_qty >= 0 AND shipped_total_qty <= allocated_total_qty)
);
CREATE INDEX ix_shipment_line_product ON shipment_line (tenant_id, product_code);

-- -----------------------------------------------------------------------------
-- Allocation = one reserved stock lot for one line; it is also the pick task.
-- -----------------------------------------------------------------------------
CREATE TABLE allocation (
    id                  uuid           PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id           uuid           NOT NULL,
    shipment_line_id    uuid           NOT NULL,
    stock_id            uuid           NOT NULL,         -- inv.stock.id (no FK, other schema)
    receipt_no          varchar(20)    NOT NULL,         -- lot identity, survives stock splits
    receipt_line        integer        NOT NULL,
    receipt_seq         integer        NOT NULL,
    location_code       varchar(18)    NOT NULL,
    pick_key1           varchar(30),
    pick_key2           varchar(30),
    pick_key3           varchar(30),
    pick_key4           varchar(30),
    pick_key5           varchar(30),
    pick_key6           varchar(30),
    pick_key7           varchar(30),
    allocated_qty       numeric(12,3)  NOT NULL CHECK (allocated_qty > 0),
    picked_qty          numeric(12,3)  NOT NULL DEFAULT 0,   -- sum of pick_record.qty
    status              varchar(16)    NOT NULL DEFAULT 'ALLOCATED' CHECK (status IN
                        ('ALLOCATED', 'PICKED', 'SHIPPED', 'CANCELLED')),
    picking_batch_id    uuid,
    picking_sequence    integer,                         -- from md.location picking order
    created_at          timestamptz    NOT NULL DEFAULT now(),
    created_by          varchar(50)    NOT NULL,
    updated_at          timestamptz    NOT NULL DEFAULT now(),
    updated_by          varchar(50)    NOT NULL,
    version             integer        NOT NULL DEFAULT 0,
    FOREIGN KEY (tenant_id, shipment_line_id) REFERENCES shipment_line (tenant_id, id) ON DELETE CASCADE,
    FOREIGN KEY (tenant_id, picking_batch_id) REFERENCES picking_batch (tenant_id, id),
    UNIQUE (tenant_id, id),
    CHECK (picked_qty >= 0 AND picked_qty <= allocated_qty)
);
CREATE INDEX ix_allocation_line  ON allocation (shipment_line_id);
CREATE INDEX ix_allocation_stock ON allocation (tenant_id, stock_id) WHERE status = 'ALLOCATED';
CREATE INDEX ix_allocation_batch ON allocation (picking_batch_id, picking_sequence) WHERE picking_batch_id IS NOT NULL;

-- Each scan on the HHT (RecordPick); append-only
CREATE TABLE pick_record (
    id                uuid           PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id         uuid           NOT NULL,
    allocation_id     uuid           NOT NULL,
    scanned_location  varchar(18),
    scanned_cargo_id  varchar(20),
    qty               numeric(12,3)  NOT NULL CHECK (qty > 0),
    picked_at         timestamptz    NOT NULL DEFAULT now(),
    picked_by         varchar(50)    NOT NULL,
    FOREIGN KEY (tenant_id, allocation_id) REFERENCES allocation (tenant_id, id) ON DELETE CASCADE
);
CREATE INDEX ix_pick_record_allocation ON pick_record (allocation_id);

-- -----------------------------------------------------------------------------
-- Packing
-- -----------------------------------------------------------------------------
CREATE TABLE carton (
    id                     uuid           PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id              uuid           NOT NULL,
    shipment_id            uuid           NOT NULL,
    carton_no              varchar(20)    NOT NULL,      -- Carton.carton_id, printed on the label
    packing_material_code  varchar(10),
    weight                 numeric(12,3),
    volume                 numeric(12,4),
    created_at             timestamptz    NOT NULL DEFAULT now(),
    created_by             varchar(50)    NOT NULL,
    updated_at             timestamptz    NOT NULL DEFAULT now(),
    updated_by             varchar(50)    NOT NULL,
    version                integer        NOT NULL DEFAULT 0,
    FOREIGN KEY (tenant_id, shipment_id) REFERENCES shipment (tenant_id, id) ON DELETE CASCADE,
    UNIQUE (tenant_id, carton_no),
    UNIQUE (tenant_id, id)
);
CREATE INDEX ix_carton_shipment ON carton (shipment_id);

CREATE TABLE carton_item (
    id                uuid           PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id         uuid           NOT NULL,
    carton_id         uuid           NOT NULL,
    shipment_line_id  uuid           NOT NULL,
    product_code      varchar(50)    NOT NULL,
    pick_key1         varchar(30),
    pick_key2         varchar(30),
    pick_key3         varchar(30),
    pick_key4         varchar(30),
    pick_key5         varchar(30),
    pick_key6         varchar(30),
    pick_key7         varchar(30),
    qty               numeric(12,3)  NOT NULL CHECK (qty > 0),
    serial_numbers    text[]         NOT NULL DEFAULT '{}',
    FOREIGN KEY (tenant_id, carton_id) REFERENCES carton (tenant_id, id) ON DELETE CASCADE,
    FOREIGN KEY (tenant_id, shipment_line_id) REFERENCES shipment_line (tenant_id, id)
);
CREATE INDEX ix_carton_item_carton ON carton_item (carton_id);
CREATE INDEX ix_carton_item_line   ON carton_item (shipment_line_id);

-- -----------------------------------------------------------------------------
-- Document numbering (SHIPMENT, BATCH, TOTAL_PICK, CARTON …), same rules as inv.number_sequence
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
-- Row-Level Security
-- -----------------------------------------------------------------------------
ALTER TABLE picking_batch   ENABLE ROW LEVEL SECURITY;
ALTER TABLE shipment        ENABLE ROW LEVEL SECURITY;
ALTER TABLE shipment_line   ENABLE ROW LEVEL SECURITY;
ALTER TABLE allocation      ENABLE ROW LEVEL SECURITY;
ALTER TABLE pick_record     ENABLE ROW LEVEL SECURITY;
ALTER TABLE carton          ENABLE ROW LEVEL SECURITY;
ALTER TABLE carton_item     ENABLE ROW LEVEL SECURITY;
ALTER TABLE number_sequence ENABLE ROW LEVEL SECURITY;

ALTER TABLE picking_batch   FORCE ROW LEVEL SECURITY;
ALTER TABLE shipment        FORCE ROW LEVEL SECURITY;
ALTER TABLE shipment_line   FORCE ROW LEVEL SECURITY;
ALTER TABLE allocation      FORCE ROW LEVEL SECURITY;
ALTER TABLE pick_record     FORCE ROW LEVEL SECURITY;
ALTER TABLE carton          FORCE ROW LEVEL SECURITY;
ALTER TABLE carton_item     FORCE ROW LEVEL SECURITY;
ALTER TABLE number_sequence FORCE ROW LEVEL SECURITY;

-- USING filters reads/updates/deletes; WITH CHECK stops writing rows into another tenant
CREATE POLICY p_tenant ON picking_batch   USING (tenant_id = app.ctx('tenant_id')) WITH CHECK (tenant_id = app.ctx('tenant_id'));
CREATE POLICY p_tenant ON shipment        USING (tenant_id = app.ctx('tenant_id')) WITH CHECK (tenant_id = app.ctx('tenant_id'));
CREATE POLICY p_tenant ON shipment_line   USING (tenant_id = app.ctx('tenant_id')) WITH CHECK (tenant_id = app.ctx('tenant_id'));
CREATE POLICY p_tenant ON allocation      USING (tenant_id = app.ctx('tenant_id')) WITH CHECK (tenant_id = app.ctx('tenant_id'));
CREATE POLICY p_tenant ON pick_record     USING (tenant_id = app.ctx('tenant_id')) WITH CHECK (tenant_id = app.ctx('tenant_id'));
CREATE POLICY p_tenant ON carton          USING (tenant_id = app.ctx('tenant_id')) WITH CHECK (tenant_id = app.ctx('tenant_id'));
CREATE POLICY p_tenant ON carton_item     USING (tenant_id = app.ctx('tenant_id')) WITH CHECK (tenant_id = app.ctx('tenant_id'));
CREATE POLICY p_tenant ON number_sequence USING (tenant_id = app.ctx('tenant_id')) WITH CHECK (tenant_id = app.ctx('tenant_id'));

-- -----------------------------------------------------------------------------
-- Integration
-- -----------------------------------------------------------------------------
CREATE TABLE outbox_event (
    id              uuid         PRIMARY KEY DEFAULT gen_random_uuid(),
    aggregate_type  varchar(60)  NOT NULL,
    aggregate_id    varchar(100) NOT NULL,
    event_type      varchar(100) NOT NULL,       -- outbound.shipment.allocated, outbound.shipment.confirmed …
    tenant_id       uuid         NOT NULL REFERENCES iam.tenant (id),
    payload         jsonb        NOT NULL,
    created_at      timestamptz  NOT NULL DEFAULT now(),
    published_at    timestamptz
);
CREATE INDEX ix_outbox_unpublished ON outbox_event (created_at) WHERE published_at IS NULL;

-- Idempotent consumption of Kafka events from other services (e.g. stock.adjusted)
CREATE TABLE processed_event (
    event_id      uuid         PRIMARY KEY,
    consumer      varchar(60)  NOT NULL,
    processed_at  timestamptz  NOT NULL DEFAULT now()
);

-- Picks are append-only for the runtime role
REVOKE UPDATE, DELETE ON pick_record FROM outb_app;

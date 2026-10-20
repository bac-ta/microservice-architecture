-- =============================================================================
-- Master data database — owned by service-masterdata
-- PostgreSQL 16
--
-- Replaces (legacy SGWH0001):
--   GWH_TM_WHS (operational part), GWH_TM_AREA, GWH_TM_LOC, GWH_TM_PROD, GWH_TM_PBAR,
--   GWH_TM_SBIV, GWH_TM_PIK, GWH_TM_TRN, GWH_TM_STS, GWH_TM_PRCC, GWH_TM_CODE,
--   GWH_TM_RSN, GWH_TM_CUST (≈249 behaviour switches -> tenant_setting)
--
-- Three kinds of tables:
--   1. warehouse-scoped  (warehouse_id)  — locations are shared by all customers in a warehouse
--   2. tenant-scoped     (tenant_id)     — products, sub-inventories
--   3. config with scope (company_id / warehouse_id / customer_id, NULL = '*')
--      resolved "most specific wins", like the legacy '*' rows.
--
-- Warehouse / tenant / scope columns are real foreign keys to the iam registry.
-- Runs as md_owner after bootstrap.sql and the iam migrations.
-- =============================================================================

SET search_path = md;

GRANT USAGE ON SCHEMA md TO md_app;
ALTER DEFAULT PRIVILEGES IN SCHEMA md GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO md_app;
ALTER DEFAULT PRIVILEGES IN SCHEMA md GRANT USAGE, SELECT ON SEQUENCES TO md_app;
-- cross-tenant admin / batch role (BYPASSRLS)
ALTER DEFAULT PRIVILEGES IN SCHEMA md GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO epl_admin;

-- -----------------------------------------------------------------------------
-- Warehouse-scoped
-- -----------------------------------------------------------------------------

-- Operational warehouse data (legacy GWH_TM_WHS minus identity)
CREATE TABLE warehouse_profile (
    warehouse_id     uuid          PRIMARY KEY REFERENCES iam.warehouse (id),
    physical_code    varchar(4),
    postal_code      varchar(10),
    address_lines    text[]        NOT NULL DEFAULT '{}',   -- WHS_ADR1..5
    city_name        varchar(50),
    state_code       varchar(2),
    country_code     char(2),
    phone            varchar(20),
    latitude         numeric(8,5),
    longitude        numeric(8,5),
    -- how a location code splits into area/rack/position/level (WHS_ABYT, WHS_RBP1..4)
    location_format  jsonb         NOT NULL DEFAULT '{}',
    updated_at       timestamptz   NOT NULL DEFAULT now(),
    updated_by       varchar(50)   NOT NULL,
    version          integer       NOT NULL DEFAULT 0
);

CREATE TABLE area (
    warehouse_id           uuid         NOT NULL REFERENCES iam.warehouse (id),
    code                   varchar(3)   NOT NULL,
    name                   varchar(50)  NOT NULL,
    packing_style_kind     varchar(2),
    recommend_kind         varchar(2),
    picking_group_code     varchar(3),
    temperature_kind       varchar(2),
    management_kind        varchar(1),
    no_allocation          boolean      NOT NULL DEFAULT false,
    no_replenishment       boolean      NOT NULL DEFAULT false,
    single_product_only    boolean      NOT NULL DEFAULT false,
    single_pick_key_only   boolean      NOT NULL DEFAULT false,
    locked                 boolean      NOT NULL DEFAULT false,
    ext                    jsonb        NOT NULL DEFAULT '{}',
    created_at             timestamptz  NOT NULL DEFAULT now(),
    created_by             varchar(50)  NOT NULL,
    updated_at             timestamptz  NOT NULL DEFAULT now(),
    updated_by             varchar(50)  NOT NULL,
    deleted_at             timestamptz,
    version                integer      NOT NULL DEFAULT 0,
    PRIMARY KEY (warehouse_id, code)
);

CREATE TABLE location (
    warehouse_id            uuid           NOT NULL,
    code                    varchar(18)    NOT NULL,
    area_code               varchar(3)     NOT NULL,
    rack_code               varchar(10)    NOT NULL,
    position_code           varchar(3)     NOT NULL,
    level_code              varchar(2)     NOT NULL,
    zone_code               varchar(2),
    management_kind         varchar(1),
    no_allocation           boolean        NOT NULL DEFAULT false,
    no_replenishment        boolean        NOT NULL DEFAULT false,
    single_product_only     boolean        NOT NULL DEFAULT false,
    single_pick_key_only    boolean        NOT NULL DEFAULT false,
    allocation_priority     integer,
    picking_sequence        integer,
    depth                   numeric(12,6),
    width                   numeric(12,6),
    height                  numeric(12,6),
    temperature_control     varchar(2),
    dedicated_product_code  varchar(50),
    sub_inventory_code      varchar(20),
    bond_kind               varchar(2),
    remarks                 varchar(50),
    ext                     jsonb          NOT NULL DEFAULT '{}',   -- LOC_CIT1..15, CFG*
    created_at              timestamptz    NOT NULL DEFAULT now(),
    created_by              varchar(50)    NOT NULL,
    updated_at              timestamptz    NOT NULL DEFAULT now(),
    updated_by              varchar(50)    NOT NULL,
    deleted_at              timestamptz,
    version                 integer        NOT NULL DEFAULT 0,
    PRIMARY KEY (warehouse_id, code),
    FOREIGN KEY (warehouse_id, area_code) REFERENCES area (warehouse_id, code),
    UNIQUE (warehouse_id, area_code, rack_code, position_code, level_code)
);
CREATE INDEX ix_location_picking ON location (warehouse_id, picking_sequence);

-- -----------------------------------------------------------------------------
-- Tenant-scoped
-- -----------------------------------------------------------------------------

CREATE TABLE product (
    tenant_id                uuid           NOT NULL REFERENCES iam.tenant (id),
    code                     varchar(50)    NOT NULL,
    names                    text[]         NOT NULL,                -- PROD_NAM1..4
    product_group_code       varchar(15),
    hierarchy                text[]         NOT NULL DEFAULT '{}',   -- PROD_HRC1..3
    -- packing
    units_per_case           integer        CHECK (units_per_case > 0),
    units_per_ball           integer        CHECK (units_per_ball > 0),
    uom_piece                varchar(3),
    uom_case                 varchar(3),
    uom_ball                 varchar(3),
    uom_length               varchar(3),
    uom_weight               varchar(3),
    cases_per_pallet         integer,
    case_weight              numeric(12,6),
    case_volume              numeric(12,6),
    case_length              numeric(12,6),
    case_width               numeric(12,6),
    case_height              numeric(12,6),
    piece_weight             numeric(12,6),
    piece_volume             numeric(12,6),
    piece_length             numeric(12,6),
    piece_width              numeric(12,6),
    piece_height             numeric(12,6),
    gross_weight             numeric(12,6),
    net_weight               numeric(12,6),
    -- prices
    deposit_price            numeric(14,4),
    deposit_currency         char(3),
    purchase_price           numeric(14,4),
    purchase_currency        char(3),
    sales_price              numeric(14,4),
    sales_currency           char(3),
    -- handling rules
    default_allocation_kind  varchar(1),
    not_shippable            boolean        NOT NULL DEFAULT false,
    storage_kind             varchar(2),
    abc_class                varchar(1),
    cycle_count_group        varchar(20),
    expiry_kind              varchar(1),
    expiry_days              integer,
    ship_by_days             integer,
    ship_by_unit             varchar(2),
    stock_limit_days         integer,
    stock_limit_unit         varchar(2),
    is_kit_parent            boolean        NOT NULL DEFAULT false,
    kit_stock_managed        boolean        NOT NULL DEFAULT false,
    cross_dock_kind          varchar(2),
    has_accessories          boolean        NOT NULL DEFAULT false,
    -- customs / bond
    hs_code                  varchar(18),
    bond_kind                varchar(2),
    customs_status           varchar(2),
    origin_country           char(2),
    -- put-away / replenishment suggestions (location codes are warehouse-scoped)
    recommended_location     varchar(18),
    replenish_location       varchar(18),
    replenish_max_qty        integer,
    replenish_base_qty       integer,
    ext                      jsonb          NOT NULL DEFAULT '{}',   -- the long tail of 191 legacy columns
    created_at               timestamptz    NOT NULL DEFAULT now(),
    created_by               varchar(50)    NOT NULL,
    updated_at               timestamptz    NOT NULL DEFAULT now(),
    updated_by               varchar(50)    NOT NULL,
    deleted_at               timestamptz,
    version                  integer        NOT NULL DEFAULT 0,
    PRIMARY KEY (tenant_id, code),
    CHECK (cardinality(names) BETWEEN 1 AND 4)
);
CREATE INDEX ix_product_group ON product (tenant_id, product_group_code);

-- legacy GWH_TM_PBAR
CREATE TABLE product_barcode (
    tenant_id     uuid         NOT NULL,
    barcode       varchar(50)  NOT NULL,
    product_code  varchar(50)  NOT NULL,
    uom_level     varchar(10)  NOT NULL DEFAULT 'PIECE' CHECK (uom_level IN ('PIECE', 'BALL', 'CASE', 'PALLET')),
    created_at    timestamptz  NOT NULL DEFAULT now(),
    created_by    varchar(50)  NOT NULL,
    PRIMARY KEY (tenant_id, barcode),
    FOREIGN KEY (tenant_id, product_code) REFERENCES product (tenant_id, code)
);

-- legacy GWH_TM_SBIV (grade / sub-inventory)
CREATE TABLE sub_inventory (
    tenant_id   uuid         NOT NULL REFERENCES iam.tenant (id),
    code        varchar(20)  NOT NULL,
    name        varchar(50)  NOT NULL,
    is_default  boolean      NOT NULL DEFAULT false,
    ext         jsonb        NOT NULL DEFAULT '{}',
    created_at  timestamptz  NOT NULL DEFAULT now(),
    created_by  varchar(50)  NOT NULL,
    updated_at  timestamptz  NOT NULL DEFAULT now(),
    updated_by  varchar(50)  NOT NULL,
    deleted_at  timestamptz,
    PRIMARY KEY (tenant_id, code)
);
CREATE UNIQUE INDEX ux_sub_inventory_default ON sub_inventory (tenant_id) WHERE is_default AND deleted_at IS NULL;

-- -----------------------------------------------------------------------------
-- Scoped configuration (NULL scope column = '*')
-- specificity: customer(4) > warehouse(2) > company(1); highest wins.
-- -----------------------------------------------------------------------------

-- Behaviour switches per customer/warehouse/company (legacy GWH_TM_CUST flags, CFG1-15 …)
CREATE TABLE tenant_setting (
    id            uuid          PRIMARY KEY DEFAULT gen_random_uuid(),
    company_id    uuid,
    warehouse_id  uuid,
    customer_id   uuid,
    specificity   smallint      GENERATED ALWAYS AS (
                      (CASE WHEN company_id   IS NULL THEN 0 ELSE 1 END)
                    + (CASE WHEN warehouse_id IS NULL THEN 0 ELSE 2 END)
                    + (CASE WHEN customer_id  IS NULL THEN 0 ELSE 4 END)) STORED,
    setting_key   varchar(100)  NOT NULL,      -- e.g. inventory.stocktake.update_stock_on_confirm
    value         jsonb         NOT NULL,
    description   varchar(500),
    updated_at    timestamptz   NOT NULL DEFAULT now(),
    updated_by    varchar(50)   NOT NULL,
    version       integer       NOT NULL DEFAULT 0,
    FOREIGN KEY (company_id)               REFERENCES iam.company (id),
    FOREIGN KEY (company_id, warehouse_id) REFERENCES iam.warehouse (company_id, id),
    FOREIGN KEY (company_id, customer_id)  REFERENCES iam.customer (company_id, id),
    CHECK (warehouse_id IS NULL OR company_id IS NOT NULL),
    CHECK (customer_id  IS NULL OR company_id IS NOT NULL),
    UNIQUE NULLS NOT DISTINCT (setting_key, company_id, warehouse_id, customer_id)
);

-- legacy GWH_TM_PIK: definition of picking keys 1..7 (lot no, expiry, …)
CREATE TABLE picking_key_def (
    id                    uuid         PRIMARY KEY DEFAULT gen_random_uuid(),
    company_id            uuid,
    warehouse_id          uuid,
    customer_id           uuid,
    specificity           smallint     GENERATED ALWAYS AS (
                              (CASE WHEN company_id   IS NULL THEN 0 ELSE 1 END)
                            + (CASE WHEN warehouse_id IS NULL THEN 0 ELSE 2 END)
                            + (CASE WHEN customer_id  IS NULL THEN 0 ELSE 4 END)) STORED,
    key_no                smallint     NOT NULL CHECK (key_no BETWEEN 1 AND 7),
    in_use                boolean      NOT NULL DEFAULT false,
    name                  varchar(50),
    data_type             varchar(2),             -- PIK_TYPE_KND
    max_length            smallint,
    date_format           varchar(2),
    allocation_pattern    varchar(2),
    required_on_receipt   boolean      NOT NULL DEFAULT false,
    required_on_ship      boolean      NOT NULL DEFAULT false,
    auto_receipt_date     boolean      NOT NULL DEFAULT false,
    expiry_control        boolean      NOT NULL DEFAULT false,
    best_by_kind          varchar(1),
    scan_rules            jsonb        NOT NULL DEFAULT '{}',   -- AVSC/PISC/PASC/LOSC/LCSC/RLSC kinds
    barcode_parse         jsonb        NOT NULL DEFAULT '{}',   -- fixed (start,len) / variable (sep,pos)
    updated_at            timestamptz  NOT NULL DEFAULT now(),
    updated_by            varchar(50)  NOT NULL,
    FOREIGN KEY (company_id)               REFERENCES iam.company (id),
    FOREIGN KEY (company_id, warehouse_id) REFERENCES iam.warehouse (company_id, id),
    FOREIGN KEY (company_id, customer_id)  REFERENCES iam.customer (company_id, id),
    CHECK (warehouse_id IS NULL OR company_id IS NOT NULL),
    CHECK (customer_id  IS NULL OR company_id IS NOT NULL),
    UNIQUE NULLS NOT DISTINCT (key_no, company_id, warehouse_id, customer_id)
);

-- legacy GWH_TM_TRN (91 columns of flags -> usage / charge arrays)
CREATE TABLE transaction_kind (
    id                  uuid          PRIMARY KEY DEFAULT gen_random_uuid(),
    company_id          uuid,
    warehouse_id        uuid,
    customer_id         uuid,
    specificity         smallint      GENERATED ALWAYS AS (
                            (CASE WHEN company_id   IS NULL THEN 0 ELSE 1 END)
                          + (CASE WHEN warehouse_id IS NULL THEN 0 ELSE 2 END)
                          + (CASE WHEN customer_id  IS NULL THEN 0 ELSE 4 END)) STORED,
    code                varchar(3)    NOT NULL,
    name                varchar(50)   NOT NULL,
    -- where the kind may be used: RECEIPT, PUT_IN, SHIP, ISSUE, ADJUST, RELOCATE, PRODUCT_TRANSFER,
    -- KIT_IN, KIT_OUT, DISASSEMBLE_IN, DISASSEMBLE_OUT, DIVIDE
    usages              text[]        NOT NULL DEFAULT '{}',
    -- which warehouse charges it triggers: IN, OUT, STORAGE, PROCESSING
    charge_types        text[]        NOT NULL DEFAULT '{}',
    reason_required     boolean       NOT NULL DEFAULT false,
    allow_surplus       boolean       NOT NULL DEFAULT false,
    allow_shortage      boolean       NOT NULL DEFAULT false,
    allow_pick_key_diff boolean       NOT NULL DEFAULT false,
    options             jsonb         NOT NULL DEFAULT '{}',    -- remaining flags (KPI, bond ledger, …)
    updated_at          timestamptz   NOT NULL DEFAULT now(),
    updated_by          varchar(50)   NOT NULL,
    FOREIGN KEY (company_id)               REFERENCES iam.company (id),
    FOREIGN KEY (company_id, warehouse_id) REFERENCES iam.warehouse (company_id, id),
    FOREIGN KEY (company_id, customer_id)  REFERENCES iam.customer (company_id, id),
    CHECK (warehouse_id IS NULL OR company_id IS NOT NULL),
    CHECK (customer_id  IS NULL OR company_id IS NOT NULL),
    UNIQUE NULLS NOT DISTINCT (code, company_id, warehouse_id, customer_id)
);

-- legacy GWH_TM_STS
CREATE TABLE status_def (
    id             uuid          PRIMARY KEY DEFAULT gen_random_uuid(),
    company_id     uuid,
    warehouse_id   uuid,
    customer_id    uuid,
    specificity    smallint      GENERATED ALWAYS AS (
                       (CASE WHEN company_id   IS NULL THEN 0 ELSE 1 END)
                     + (CASE WHEN warehouse_id IS NULL THEN 0 ELSE 2 END)
                     + (CASE WHEN customer_id  IS NULL THEN 0 ELSE 4 END)) STORED,
    business_code  varchar(2)    NOT NULL,      -- AV, SP, STK …
    status_code    varchar(3)    NOT NULL,
    label_key      varchar(100)  NOT NULL,
    work_state     varchar(1),
    options        jsonb         NOT NULL DEFAULT '{}',
    updated_at     timestamptz   NOT NULL DEFAULT now(),
    updated_by     varchar(50)   NOT NULL,
    FOREIGN KEY (company_id)               REFERENCES iam.company (id),
    FOREIGN KEY (company_id, warehouse_id) REFERENCES iam.warehouse (company_id, id),
    FOREIGN KEY (company_id, customer_id)  REFERENCES iam.customer (company_id, id),
    CHECK (warehouse_id IS NULL OR company_id IS NOT NULL),
    CHECK (customer_id  IS NULL OR company_id IS NOT NULL),
    UNIQUE NULLS NOT DISTINCT (business_code, status_code, company_id, warehouse_id, customer_id)
);

-- legacy GWH_TM_PRCC: a process may start only from required_status
CREATE TABLE process_control (
    id                 uuid          PRIMARY KEY DEFAULT gen_random_uuid(),
    company_id         uuid,
    warehouse_id       uuid,
    customer_id        uuid,
    specificity        smallint      GENERATED ALWAYS AS (
                           (CASE WHEN company_id   IS NULL THEN 0 ELSE 1 END)
                         + (CASE WHEN warehouse_id IS NULL THEN 0 ELSE 2 END)
                         + (CASE WHEN customer_id  IS NULL THEN 0 ELSE 4 END)) STORED,
    process_code       varchar(20)   NOT NULL,
    required_status    varchar(3)    NOT NULL,
    processing_status  varchar(3),
    finished_status    varchar(3)    NOT NULL,
    updated_at         timestamptz   NOT NULL DEFAULT now(),
    updated_by         varchar(50)   NOT NULL,
    FOREIGN KEY (company_id)               REFERENCES iam.company (id),
    FOREIGN KEY (company_id, warehouse_id) REFERENCES iam.warehouse (company_id, id),
    FOREIGN KEY (company_id, customer_id)  REFERENCES iam.customer (company_id, id),
    CHECK (warehouse_id IS NULL OR company_id IS NOT NULL),
    CHECK (customer_id  IS NULL OR company_id IS NOT NULL),
    UNIQUE NULLS NOT DISTINCT (process_code, company_id, warehouse_id, customer_id)
);

-- legacy GWH_TM_CODE (+ GWH_TM_RSN as kind 'REASON')
CREATE TABLE code_list (
    id            uuid          PRIMARY KEY DEFAULT gen_random_uuid(),
    company_id    uuid,
    warehouse_id  uuid,
    customer_id   uuid,
    specificity   smallint      GENERATED ALWAYS AS (
                      (CASE WHEN company_id   IS NULL THEN 0 ELSE 1 END)
                    + (CASE WHEN warehouse_id IS NULL THEN 0 ELSE 2 END)
                    + (CASE WHEN customer_id  IS NULL THEN 0 ELSE 4 END)) STORED,
    kind          varchar(10)   NOT NULL,
    code          varchar(20)   NOT NULL,
    label_key     varchar(100)  NOT NULL,
    sort_order    integer       NOT NULL DEFAULT 0,
    attributes    jsonb         NOT NULL DEFAULT '{}',     -- INFO1..5, NUM1..3
    is_standard   boolean       NOT NULL DEFAULT true,
    enabled       boolean       NOT NULL DEFAULT true,
    updated_at    timestamptz   NOT NULL DEFAULT now(),
    updated_by    varchar(50)   NOT NULL,
    FOREIGN KEY (company_id)               REFERENCES iam.company (id),
    FOREIGN KEY (company_id, warehouse_id) REFERENCES iam.warehouse (company_id, id),
    FOREIGN KEY (company_id, customer_id)  REFERENCES iam.customer (company_id, id),
    CHECK (warehouse_id IS NULL OR company_id IS NOT NULL),
    CHECK (customer_id  IS NULL OR company_id IS NOT NULL),
    UNIQUE NULLS NOT DISTINCT (kind, code, company_id, warehouse_id, customer_id)
);

-- -----------------------------------------------------------------------------
-- Row-Level Security
-- The service connects as a non-owner role; FORCE makes RLS apply to the owner too.
-- -----------------------------------------------------------------------------
ALTER TABLE area              ENABLE ROW LEVEL SECURITY;
ALTER TABLE location          ENABLE ROW LEVEL SECURITY;
ALTER TABLE warehouse_profile ENABLE ROW LEVEL SECURITY;
ALTER TABLE product           ENABLE ROW LEVEL SECURITY;
ALTER TABLE product_barcode   ENABLE ROW LEVEL SECURITY;
ALTER TABLE sub_inventory     ENABLE ROW LEVEL SECURITY;
ALTER TABLE tenant_setting    ENABLE ROW LEVEL SECURITY;
ALTER TABLE picking_key_def   ENABLE ROW LEVEL SECURITY;
ALTER TABLE transaction_kind  ENABLE ROW LEVEL SECURITY;
ALTER TABLE status_def        ENABLE ROW LEVEL SECURITY;
ALTER TABLE process_control   ENABLE ROW LEVEL SECURITY;
ALTER TABLE code_list         ENABLE ROW LEVEL SECURITY;

ALTER TABLE area              FORCE ROW LEVEL SECURITY;
ALTER TABLE location          FORCE ROW LEVEL SECURITY;
ALTER TABLE warehouse_profile FORCE ROW LEVEL SECURITY;
ALTER TABLE product           FORCE ROW LEVEL SECURITY;
ALTER TABLE product_barcode   FORCE ROW LEVEL SECURITY;
ALTER TABLE sub_inventory     FORCE ROW LEVEL SECURITY;
ALTER TABLE tenant_setting    FORCE ROW LEVEL SECURITY;
ALTER TABLE picking_key_def   FORCE ROW LEVEL SECURITY;
ALTER TABLE transaction_kind  FORCE ROW LEVEL SECURITY;
ALTER TABLE status_def        FORCE ROW LEVEL SECURITY;
ALTER TABLE process_control   FORCE ROW LEVEL SECURITY;
ALTER TABLE code_list         FORCE ROW LEVEL SECURITY;

CREATE POLICY p_warehouse ON area              USING (warehouse_id = app.ctx('warehouse_id'));
CREATE POLICY p_warehouse ON location          USING (warehouse_id = app.ctx('warehouse_id'));
CREATE POLICY p_warehouse ON warehouse_profile USING (warehouse_id = app.ctx('warehouse_id'));

CREATE POLICY p_tenant ON product         USING (tenant_id = app.ctx('tenant_id'));
CREATE POLICY p_tenant ON product_barcode USING (tenant_id = app.ctx('tenant_id'));
CREATE POLICY p_tenant ON sub_inventory   USING (tenant_id = app.ctx('tenant_id'));

-- Scoped config: the service role may only READ defaults ('*') and the rows of the
-- current scope. There is no write policy, so writes are denied; configuration is
-- maintained through the admin API, which connects as a separate BYPASSRLS role.
CREATE POLICY p_scope_read  ON tenant_setting   FOR SELECT USING (app.in_scope(company_id, warehouse_id, customer_id));
CREATE POLICY p_scope_read  ON picking_key_def  FOR SELECT USING (app.in_scope(company_id, warehouse_id, customer_id));
CREATE POLICY p_scope_read  ON transaction_kind FOR SELECT USING (app.in_scope(company_id, warehouse_id, customer_id));
CREATE POLICY p_scope_read  ON status_def       FOR SELECT USING (app.in_scope(company_id, warehouse_id, customer_id));
CREATE POLICY p_scope_read  ON process_control  FOR SELECT USING (app.in_scope(company_id, warehouse_id, customer_id));
CREATE POLICY p_scope_read  ON code_list        FOR SELECT USING (app.in_scope(company_id, warehouse_id, customer_id));

-- -----------------------------------------------------------------------------
-- Most-specific-wins resolution (replaces ROW_NUMBER() OVER … ORDER BY LENGTH(cpny||whs||cust))
-- Example for tenant_setting; the Java resolver uses the same ORDER BY and caches the result.
-- -----------------------------------------------------------------------------
CREATE VIEW effective_tenant_setting WITH (security_invoker = true) AS
SELECT DISTINCT ON (setting_key) setting_key, value, specificity, id
FROM tenant_setting
WHERE app.in_scope(company_id, warehouse_id, customer_id)
ORDER BY setting_key, specificity DESC;

-- -----------------------------------------------------------------------------
-- Integration
-- -----------------------------------------------------------------------------
CREATE TABLE outbox_event (
    id              uuid         PRIMARY KEY DEFAULT gen_random_uuid(),
    aggregate_type  varchar(60)  NOT NULL,
    aggregate_id    varchar(100) NOT NULL,
    event_type      varchar(100) NOT NULL,
    tenant_id       uuid,
    payload         jsonb        NOT NULL,
    created_at      timestamptz  NOT NULL DEFAULT now(),
    published_at    timestamptz
);
CREATE INDEX ix_outbox_unpublished ON outbox_event (created_at) WHERE published_at IS NULL;

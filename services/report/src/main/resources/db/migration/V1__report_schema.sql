-- =============================================================================
-- Report database — owned by service-report
-- PostgreSQL 16
--
-- Owns report / label definitions, printers, print history and the KPI read model.
-- Report data itself comes from the other services (gRPC at render time); KPI tables
-- are filled from their Kafka events (processed_event makes that idempotent).
-- Rendered files live in object storage (S3); print_job holds the storage_ref.
--
-- Definitions are scoped config like md (NULL scope column = '*', most specific wins).
-- Scope / warehouse / tenant columns are real foreign keys to the iam registry (iam/V4).
-- Runs as rpt_owner after bootstrap.sql and the iam migrations.
-- =============================================================================

SET search_path = rpt;

GRANT USAGE ON SCHEMA rpt TO rpt_app;
ALTER DEFAULT PRIVILEGES IN SCHEMA rpt GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO rpt_app;
ALTER DEFAULT PRIVILEGES IN SCHEMA rpt GRANT USAGE, SELECT ON SEQUENCES TO rpt_app;
-- cross-tenant admin / batch role (BYPASSRLS)
ALTER DEFAULT PRIVILEGES IN SCHEMA rpt GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO epl_admin;

-- -----------------------------------------------------------------------------
-- Scoped definitions (NULL scope column = '*')
-- specificity: customer(4) > warehouse(2) > company(1); highest wins.
-- -----------------------------------------------------------------------------
CREATE TABLE report_definition (
    id            uuid          PRIMARY KEY DEFAULT gen_random_uuid(),
    company_id    uuid,
    warehouse_id  uuid,
    customer_id   uuid,
    specificity   smallint      GENERATED ALWAYS AS (
                      (CASE WHEN company_id   IS NULL THEN 0 ELSE 1 END)
                    + (CASE WHEN warehouse_id IS NULL THEN 0 ELSE 2 END)
                    + (CASE WHEN customer_id  IS NULL THEN 0 ELSE 4 END)) STORED,
    code          varchar(40)   NOT NULL,             -- PICKING_LIST, DELIVERY_NOTE …
    name          varchar(100)  NOT NULL,
    process_code  varchar(20)   NOT NULL,             -- business step that offers it
    formats       text[]        NOT NULL CHECK (formats <@ ARRAY['PDF', 'XLSX', 'CSV'] AND cardinality(formats) > 0),
    parameters    jsonb         NOT NULL DEFAULT '[]',   -- parameter names and types
    template_ref  varchar(500)  NOT NULL,             -- template in object storage
    enabled       boolean       NOT NULL DEFAULT true,
    updated_at    timestamptz   NOT NULL DEFAULT now(),
    updated_by    varchar(50)   NOT NULL,
    version       integer       NOT NULL DEFAULT 0,
    FOREIGN KEY (company_id)               REFERENCES iam.company (id),
    FOREIGN KEY (company_id, warehouse_id) REFERENCES iam.warehouse (company_id, id),
    FOREIGN KEY (company_id, customer_id)  REFERENCES iam.customer (company_id, id),
    CHECK (warehouse_id IS NULL OR company_id IS NOT NULL),
    CHECK (customer_id  IS NULL OR company_id IS NOT NULL),
    UNIQUE NULLS NOT DISTINCT (code, company_id, warehouse_id, customer_id)
);
CREATE INDEX ix_report_definition_process ON report_definition (process_code) WHERE enabled;

CREATE TABLE label_definition (
    id            uuid          PRIMARY KEY DEFAULT gen_random_uuid(),
    company_id    uuid,
    warehouse_id  uuid,
    customer_id   uuid,
    specificity   smallint      GENERATED ALWAYS AS (
                      (CASE WHEN company_id   IS NULL THEN 0 ELSE 1 END)
                    + (CASE WHEN warehouse_id IS NULL THEN 0 ELSE 2 END)
                    + (CASE WHEN customer_id  IS NULL THEN 0 ELSE 4 END)) STORED,
    code          varchar(40)   NOT NULL,             -- CARGO_ID, PRODUCT, CARTON …
    name          varchar(100)  NOT NULL,
    language      varchar(10)   NOT NULL DEFAULT 'ZPL' CHECK (language IN ('ZPL', 'EPL', 'PDF')),
    template_ref  varchar(500)  NOT NULL,
    enabled       boolean       NOT NULL DEFAULT true,
    updated_at    timestamptz   NOT NULL DEFAULT now(),
    updated_by    varchar(50)   NOT NULL,
    version       integer       NOT NULL DEFAULT 0,
    FOREIGN KEY (company_id)               REFERENCES iam.company (id),
    FOREIGN KEY (company_id, warehouse_id) REFERENCES iam.warehouse (company_id, id),
    FOREIGN KEY (company_id, customer_id)  REFERENCES iam.customer (company_id, id),
    CHECK (warehouse_id IS NULL OR company_id IS NOT NULL),
    CHECK (customer_id  IS NULL OR company_id IS NOT NULL),
    UNIQUE NULLS NOT DISTINCT (code, company_id, warehouse_id, customer_id)
);

-- -----------------------------------------------------------------------------
-- Printers (warehouse-scoped, shared by every customer of the warehouse)
-- -----------------------------------------------------------------------------
CREATE TABLE printer (
    warehouse_id  uuid          NOT NULL REFERENCES iam.warehouse (id),
    code          varchar(20)   NOT NULL,             -- PrintLabelsRequest.printer_id
    name          varchar(100)  NOT NULL,
    kind          varchar(8)    NOT NULL CHECK (kind IN ('LABEL', 'DOCUMENT')),
    endpoint      varchar(300)  NOT NULL,             -- ipp://… / raw tcp host:port / print server queue
    enabled       boolean       NOT NULL DEFAULT true,
    updated_at    timestamptz   NOT NULL DEFAULT now(),
    updated_by    varchar(50)   NOT NULL,
    version       integer       NOT NULL DEFAULT 0,
    PRIMARY KEY (warehouse_id, code)
);

-- Default label printer of a handheld / PC (empty printer_id in PrintLabels)
CREATE TABLE device_printer (
    warehouse_id  uuid          NOT NULL,
    device_id     varchar(50)   NOT NULL,
    printer_code  varchar(20)   NOT NULL,
    updated_at    timestamptz   NOT NULL DEFAULT now(),
    updated_by    varchar(50)   NOT NULL,
    PRIMARY KEY (warehouse_id, device_id),
    FOREIGN KEY (warehouse_id, printer_code) REFERENCES printer (warehouse_id, code)
);

-- -----------------------------------------------------------------------------
-- Print / render history
-- -----------------------------------------------------------------------------
CREATE TABLE print_job (
    id             uuid          PRIMARY KEY DEFAULT gen_random_uuid(),   -- PrintLabelsResponse.print_job_id
    tenant_id      uuid          NOT NULL,
    warehouse_id   uuid          NOT NULL,
    kind           varchar(8)    NOT NULL CHECK (kind IN ('REPORT', 'LABEL')),
    report_code    varchar(40),
    label_code     varchar(40),
    format         varchar(4)    CHECK (format IN ('PDF', 'XLSX', 'CSV')),
    locale         varchar(10),
    printer_code   varchar(20),                       -- NULL = rendered for download
    parameters     jsonb         NOT NULL DEFAULT '{}',
    item_count     integer       NOT NULL DEFAULT 0,
    copies         integer       NOT NULL DEFAULT 1 CHECK (copies > 0),
    status         varchar(8)    NOT NULL DEFAULT 'QUEUED' CHECK (status IN ('QUEUED', 'RUNNING', 'DONE', 'FAILED')),
    storage_ref    varchar(500),
    error_message  varchar(1000),
    requested_at   timestamptz   NOT NULL DEFAULT now(),
    requested_by   varchar(50)   NOT NULL,
    finished_at    timestamptz,
    FOREIGN KEY (tenant_id, warehouse_id) REFERENCES iam.tenant (id, warehouse_id),
    FOREIGN KEY (warehouse_id, printer_code) REFERENCES printer (warehouse_id, code),
    CHECK ((kind = 'REPORT') = (report_code IS NOT NULL)),
    CHECK ((kind = 'LABEL')  = (label_code  IS NOT NULL))
);
CREATE INDEX ix_print_job_requested ON print_job (tenant_id, requested_at DESC);

-- -----------------------------------------------------------------------------
-- KPI read model: daily stock and shipped quantity per product, from inventory and
-- outbound events. One partition per year, created by a job; default catches the rest.
-- -----------------------------------------------------------------------------
CREATE TABLE kpi_daily_stock (
    tenant_id           uuid           NOT NULL REFERENCES iam.tenant (id),
    stat_date           date           NOT NULL,
    product_code        varchar(50)    NOT NULL,
    product_group_code  varchar(20),
    closing_qty         numeric(15,3)  NOT NULL DEFAULT 0,
    received_qty        numeric(15,3)  NOT NULL DEFAULT 0,
    shipped_qty         numeric(15,3)  NOT NULL DEFAULT 0,
    updated_at          timestamptz    NOT NULL DEFAULT now(),
    PRIMARY KEY (tenant_id, stat_date, product_code)
) PARTITION BY RANGE (stat_date);
CREATE TABLE kpi_daily_stock_default PARTITION OF kpi_daily_stock DEFAULT;
CREATE INDEX ix_kpi_daily_stock_group ON kpi_daily_stock (tenant_id, product_group_code, stat_date);

-- -----------------------------------------------------------------------------
-- Row-Level Security
-- -----------------------------------------------------------------------------
ALTER TABLE report_definition ENABLE ROW LEVEL SECURITY;
ALTER TABLE label_definition  ENABLE ROW LEVEL SECURITY;
ALTER TABLE printer           ENABLE ROW LEVEL SECURITY;
ALTER TABLE device_printer    ENABLE ROW LEVEL SECURITY;
ALTER TABLE print_job         ENABLE ROW LEVEL SECURITY;
ALTER TABLE kpi_daily_stock   ENABLE ROW LEVEL SECURITY;

ALTER TABLE report_definition FORCE ROW LEVEL SECURITY;
ALTER TABLE label_definition  FORCE ROW LEVEL SECURITY;
ALTER TABLE printer           FORCE ROW LEVEL SECURITY;
ALTER TABLE device_printer    FORCE ROW LEVEL SECURITY;
ALTER TABLE print_job         FORCE ROW LEVEL SECURITY;
ALTER TABLE kpi_daily_stock   FORCE ROW LEVEL SECURITY;

-- Definitions: read-only for the service (defaults + current scope), maintained by epl_admin
CREATE POLICY p_scope_read ON report_definition FOR SELECT USING (app.in_scope(company_id, warehouse_id, customer_id));
CREATE POLICY p_scope_read ON label_definition  FOR SELECT USING (app.in_scope(company_id, warehouse_id, customer_id));

CREATE POLICY p_warehouse ON printer        USING (warehouse_id = app.ctx('warehouse_id'));
CREATE POLICY p_warehouse ON device_printer USING (warehouse_id = app.ctx('warehouse_id'));

-- USING filters reads/updates/deletes; WITH CHECK stops writing rows into another tenant
CREATE POLICY p_tenant ON print_job       USING (tenant_id = app.ctx('tenant_id')) WITH CHECK (tenant_id = app.ctx('tenant_id'));
CREATE POLICY p_tenant ON kpi_daily_stock USING (tenant_id = app.ctx('tenant_id')) WITH CHECK (tenant_id = app.ctx('tenant_id'));

-- Most-specific-wins resolution, same rule as md.effective_tenant_setting
CREATE VIEW effective_report_definition WITH (security_invoker = true) AS
SELECT DISTINCT ON (code) *
FROM report_definition
WHERE enabled AND app.in_scope(company_id, warehouse_id, customer_id)
ORDER BY code, specificity DESC;

CREATE VIEW effective_label_definition WITH (security_invoker = true) AS
SELECT DISTINCT ON (code) *
FROM label_definition
WHERE enabled AND app.in_scope(company_id, warehouse_id, customer_id)
ORDER BY code, specificity DESC;

-- -----------------------------------------------------------------------------
-- Integration (report publishes nothing, so no outbox)
-- -----------------------------------------------------------------------------

-- Idempotent consumption of stock.adjusted, inbound.arrival.confirmed, outbound.shipment.confirmed …
CREATE TABLE processed_event (
    event_id      uuid         PRIMARY KEY,
    consumer      varchar(60)  NOT NULL,
    processed_at  timestamptz  NOT NULL DEFAULT now()
);

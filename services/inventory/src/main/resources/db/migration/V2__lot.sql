-- =============================================================================
-- Lots: the picking keys 1..7 of a stock lot become one row of inv.lot, referenced by id.
--
-- Why: a lot is identified by 7 nullable keys whose meaning is configured per tenant
-- (md.picking_key_def). Repeating the 7 columns in every table meant comparing 7 columns with
-- IS NOT DISTINCT FROM everywhere, storing expiry dates as text (FEFO sorted as strings) and
-- having no index for allocation by lot or expiry. Now:
--   * same lot  <=> same lot_id (UNIQUE NULLS NOT DISTINCT on the keys)
--   * lot_no / expiry_date are typed copies of the keys the tenant defines as lot number / expiry
--     (derived from md.picking_key_def by the service when the lot is created)
--   * a lot never changes: ChangePickKey moves the stock to another lot
--
-- Documents keep the raw keys: arrival / shipment lines are requests ("any lot" when empty) and
-- inspection results are what was scanned before a lot exists.
--
-- Pre-release change: stock and stock_movement are empty. Adding lot_id NOT NULL fails on a table
-- with rows, so this migration cannot silently drop picking keys of existing stock.
-- =============================================================================

SET search_path = inv;

CREATE TABLE lot (
    id            uuid          PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id     uuid          NOT NULL REFERENCES iam.tenant (id),
    product_code  varchar(50)   NOT NULL,
    pick_key1     varchar(30),
    pick_key2     varchar(30),
    pick_key3     varchar(30),
    pick_key4     varchar(30),
    pick_key5     varchar(30),
    pick_key6     varchar(30),
    pick_key7     varchar(30),
    lot_no        varchar(30),                       -- the key defined as lot number, if any
    expiry_date   date,                              -- the key with expiry_control, parsed
    created_at    timestamptz   NOT NULL DEFAULT now(),
    created_by    varchar(50)   NOT NULL,
    UNIQUE NULLS NOT DISTINCT (tenant_id, product_code,
                               pick_key1, pick_key2, pick_key3, pick_key4, pick_key5, pick_key6, pick_key7),
    UNIQUE (tenant_id, product_code, id)             -- target for (tenant_id, product_code, lot_id) FKs
);
-- FEFO allocation and lot inquiries
CREATE INDEX ix_lot_expiry ON lot (tenant_id, product_code, expiry_date) WHERE expiry_date IS NOT NULL;
CREATE INDEX ix_lot_no     ON lot (tenant_id, lot_no) WHERE lot_no IS NOT NULL;

ALTER TABLE lot ENABLE ROW LEVEL SECURITY;
ALTER TABLE lot FORCE ROW LEVEL SECURITY;
CREATE POLICY p_tenant ON lot USING (tenant_id = app.ctx('tenant_id')) WITH CHECK (tenant_id = app.ctx('tenant_id'));

-- A lot is immutable for the runtime role (it is the identity of stock and history)
REVOKE UPDATE, DELETE ON lot FROM inv_app;

-- -----------------------------------------------------------------------------
-- stock / stock_movement: lot_id replaces the picking keys (same product enforced by the FK)
-- -----------------------------------------------------------------------------
ALTER TABLE stock
    ADD COLUMN lot_id uuid NOT NULL,
    ADD CONSTRAINT fk_stock_lot FOREIGN KEY (tenant_id, product_code, lot_id) REFERENCES lot (tenant_id, product_code, id),
    DROP COLUMN pick_key1, DROP COLUMN pick_key2, DROP COLUMN pick_key3, DROP COLUMN pick_key4,
    DROP COLUMN pick_key5, DROP COLUMN pick_key6, DROP COLUMN pick_key7;
CREATE INDEX ix_stock_lot ON stock (lot_id) WHERE physical_qty > 0;

ALTER TABLE stock_movement
    ADD COLUMN lot_id uuid NOT NULL,
    ADD CONSTRAINT fk_movement_lot FOREIGN KEY (tenant_id, product_code, lot_id) REFERENCES lot (tenant_id, product_code, id),
    DROP COLUMN pick_key1, DROP COLUMN pick_key2, DROP COLUMN pick_key3, DROP COLUMN pick_key4,
    DROP COLUMN pick_key5, DROP COLUMN pick_key6, DROP COLUMN pick_key7;
CREATE INDEX ix_movement_lot ON stock_movement (tenant_id, lot_id, operation_date);

-- stocktake_line: NULL lot = empty location (or found stock whose lot is not known yet)
ALTER TABLE stocktake_line
    ADD COLUMN lot_id uuid,
    ADD CONSTRAINT fk_stocktake_line_lot FOREIGN KEY (tenant_id, product_code, lot_id) REFERENCES lot (tenant_id, product_code, id),
    DROP COLUMN pick_key1, DROP COLUMN pick_key2, DROP COLUMN pick_key3, DROP COLUMN pick_key4,
    DROP COLUMN pick_key5, DROP COLUMN pick_key6, DROP COLUMN pick_key7;

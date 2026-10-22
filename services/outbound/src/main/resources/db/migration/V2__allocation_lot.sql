-- =============================================================================
-- Allocations and packed items reference the reserved lot (inv.lot.id, see inv/V2__lot.sql)
-- instead of repeating picking keys 1..7.
--
-- lot_id has no FK (other schema), like stock_id. The lot itself never changes, so the jsonb
-- copy of its keys (lot_no, expiry_date, pick_key1..7) is safe for pick lists and labels without
-- asking inventory; it is for display only: lots are compared by lot_id.
--
-- shipment_line keeps its raw keys: they are the customer's request (empty = any lot).
-- Pre-release change: allocation and carton_item are empty (lot_id NOT NULL fails otherwise).
-- =============================================================================

SET search_path = outb;

ALTER TABLE allocation
    ADD COLUMN lot_id uuid NOT NULL,
    ADD COLUMN lot jsonb NOT NULL DEFAULT '{}',
    DROP COLUMN pick_key1, DROP COLUMN pick_key2, DROP COLUMN pick_key3, DROP COLUMN pick_key4,
    DROP COLUMN pick_key5, DROP COLUMN pick_key6, DROP COLUMN pick_key7;
CREATE INDEX ix_allocation_lot ON allocation (tenant_id, lot_id) WHERE status = 'ALLOCATED';

ALTER TABLE carton_item
    ADD COLUMN lot_id uuid NOT NULL,
    ADD COLUMN lot jsonb NOT NULL DEFAULT '{}',
    DROP COLUMN pick_key1, DROP COLUMN pick_key2, DROP COLUMN pick_key3, DROP COLUMN pick_key4,
    DROP COLUMN pick_key5, DROP COLUMN pick_key6, DROP COLUMN pick_key7;

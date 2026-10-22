-- =============================================================================
-- Same as V3 for the schemas added after it: their owners may create foreign keys
-- to the tenant registry (REFERENCES only, nothing for their runtime roles).
-- =============================================================================

GRANT USAGE ON SCHEMA iam TO inb_owner, outb_owner, bill_owner, intg_owner, rpt_owner;
GRANT REFERENCES ON iam.company, iam.warehouse, iam.customer, iam.tenant
    TO inb_owner, outb_owner, bill_owner, intg_owner, rpt_owner;

-- =============================================================================
-- Let the md and inv schema owners create foreign keys to the tenant registry.
-- REFERENCES only: they cannot SELECT or change these tables, and their runtime
-- roles (md_app, inv_app) get nothing here. FK checks run as the iam table owner.
-- =============================================================================

GRANT USAGE ON SCHEMA iam TO md_owner, inv_owner;
GRANT REFERENCES ON iam.company, iam.warehouse, iam.customer, iam.tenant TO md_owner, inv_owner;

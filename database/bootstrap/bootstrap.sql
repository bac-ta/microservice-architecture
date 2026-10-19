-- =============================================================================
-- One-time bootstrap of the single GWH database.
-- Run as a superuser / rds_superuser BEFORE any service migration (not Flyway):
--   psql -d epl -f database/bootstrap/bootstrap.sql
--
-- Layout: one database, one schema per service, two roles per service:
--   <svc>_owner  owns the schema, runs that service's Flyway migrations
--   <svc>_app    runtime role of the service: DML only, subject to Row-Level Security
-- A service's roles have no privileges on other services' schemas, except
-- REFERENCES on the iam tenant tables (granted by iam/V3__cross_schema_grants.sql).
--
-- Passwords are not set here; set them from the secret store, e.g.
--   ALTER ROLE inv_app PASSWORD '...';   (or IAM database authentication on RDS)
-- =============================================================================

-- Roles ----------------------------------------------------------------------
CREATE ROLE iam_owner LOGIN NOSUPERUSER NOCREATEDB NOCREATEROLE NOBYPASSRLS;
CREATE ROLE md_owner  LOGIN NOSUPERUSER NOCREATEDB NOCREATEROLE NOBYPASSRLS;
CREATE ROLE inv_owner LOGIN NOSUPERUSER NOCREATEDB NOCREATEROLE NOBYPASSRLS;

CREATE ROLE iam_app   LOGIN NOSUPERUSER NOCREATEDB NOCREATEROLE NOBYPASSRLS;
CREATE ROLE md_app    LOGIN NOSUPERUSER NOCREATEDB NOCREATEROLE NOBYPASSRLS;
CREATE ROLE inv_app   LOGIN NOSUPERUSER NOCREATEDB NOCREATEROLE NOBYPASSRLS;

-- Cross-tenant admin / batch work (config maintenance, reports, data migration)
CREATE ROLE epl_admin LOGIN NOSUPERUSER NOCREATEDB NOCREATEROLE BYPASSRLS;

-- Nobody creates objects in public
REVOKE ALL ON SCHEMA public FROM PUBLIC;

-- Schemas --------------------------------------------------------------------
CREATE SCHEMA app;                              -- shared helpers, owned by the bootstrap user
CREATE SCHEMA iam AUTHORIZATION iam_owner;
CREATE SCHEMA md  AUTHORIZATION md_owner;
CREATE SCHEMA inv AUTHORIZATION inv_owner;

GRANT USAGE ON SCHEMA app TO iam_owner, md_owner, inv_owner, iam_app, md_app, inv_app, epl_admin;
GRANT USAGE ON SCHEMA iam, md, inv TO epl_admin;

-- Request context for Row-Level Security --------------------------------------
-- Each service sets these per transaction from the verified JWT:
--   SET LOCAL app.tenant_id = '...'; SET LOCAL app.company_id = '...';
--   SET LOCAL app.warehouse_id = '...'; SET LOCAL app.customer_id = '...';
-- A missing setting yields NULL, so RLS policies match nothing (fail closed).
CREATE FUNCTION app.ctx(name text) RETURNS uuid LANGUAGE sql STABLE AS
$$ SELECT nullif(current_setting('app.' || name, true), '')::uuid $$;

-- Scope match for wildcard config rows (NULL scope column = '*')
CREATE FUNCTION app.in_scope(c uuid, w uuid, u uuid) RETURNS boolean LANGUAGE sql STABLE AS
$$ SELECT (c IS NULL OR c = app.ctx('company_id'))
      AND (w IS NULL OR w = app.ctx('warehouse_id'))
      AND (u IS NULL OR u = app.ctx('customer_id')) $$;

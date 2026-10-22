-- =============================================================================
-- Bootstrap of the single EPL database: roles, schemas, shared RLS helpers.
-- Run as a superuser / rds_superuser BEFORE any service migration (not Flyway):
--   psql -d epl -v ON_ERROR_STOP=1 -f database/bootstrap/bootstrap.sql
--
-- Idempotent: re-run it whenever a schema is added to the list below.
--
-- Layout: one database, one schema per service, two roles per service:
--   <schema>_owner  owns the schema, runs that service's Flyway migrations (migrate job)
--   <schema>_app    runtime role of the service: DML only, subject to Row-Level Security
-- A service's roles have no privileges on other services' schemas, except
-- REFERENCES on the iam tenant tables (granted by the iam migrations).
--
-- Passwords are not set here; set them from the secret store, e.g.
--   ALTER ROLE inv_app PASSWORD '...';   (or IAM database authentication on RDS)
-- For local development see database/local/dev-passwords.sql.
-- =============================================================================

-- Cross-tenant admin / batch work (config maintenance, reports, data migration)
DO $$
BEGIN
    IF NOT EXISTS (SELECT FROM pg_roles WHERE rolname = 'epl_admin') THEN
        CREATE ROLE epl_admin LOGIN NOSUPERUSER NOCREATEDB NOCREATEROLE BYPASSRLS;
    END IF;
END $$;

-- Nobody creates objects in public
REVOKE ALL ON SCHEMA public FROM PUBLIC;

-- Shared helpers, owned by the bootstrap user
CREATE SCHEMA IF NOT EXISTS app;
GRANT USAGE ON SCHEMA app TO epl_admin;

-- Service schemas ------------------------------------------------------------
--   schema  service
--   iam     auth            md    masterdata     inv   inventory
--   inb     inbound         outb  outbound       bill  billing
--   intg    integration     rpt   report
DO $$
DECLARE
    s      text;
    owner  text;
    app    text;
    is_superuser boolean := (SELECT rolsuper FROM pg_roles WHERE rolname = current_user);
BEGIN
    FOREACH s IN ARRAY ARRAY['iam', 'md', 'inv', 'inb', 'outb', 'bill', 'intg', 'rpt'] LOOP
        owner := s || '_owner';
        app   := s || '_app';

        IF NOT EXISTS (SELECT FROM pg_roles WHERE rolname = owner) THEN
            EXECUTE format('CREATE ROLE %I LOGIN NOSUPERUSER NOCREATEDB NOCREATEROLE NOBYPASSRLS', owner);
        END IF;
        IF NOT EXISTS (SELECT FROM pg_roles WHERE rolname = app) THEN
            EXECUTE format('CREATE ROLE %I LOGIN NOSUPERUSER NOCREATEDB NOCREATEROLE NOBYPASSRLS', app);
        END IF;
        -- rds_superuser is not a real superuser: it must be a member of a role to set its
        -- default privileges below
        IF NOT is_superuser THEN
            EXECUTE format('GRANT %I TO %I', owner, current_user);
        END IF;

        EXECUTE format('CREATE SCHEMA IF NOT EXISTS %I AUTHORIZATION %I', s, owner);
        EXECUTE format('GRANT USAGE ON SCHEMA app TO %I, %I', owner, app);
        EXECUTE format('GRANT USAGE ON SCHEMA %I TO %I, epl_admin', s, app);

        -- SELECT on everything the owner creates, starting with flyway_schema_history (created
        -- before V1): the running service validates its migrations with its own app role.
        -- DML is granted by each schema's V1, so the history table stays read-only for it.
        EXECUTE format('ALTER DEFAULT PRIVILEGES FOR ROLE %I IN SCHEMA %I GRANT SELECT ON TABLES TO %I',
                       owner, s, app);
        IF to_regclass(s || '.flyway_schema_history') IS NOT NULL THEN
            EXECUTE format('GRANT SELECT ON %I.flyway_schema_history TO %I', s, app);
        END IF;
    END LOOP;
END $$;

-- Request context for Row-Level Security --------------------------------------
-- Each service sets these per transaction from the verified JWT:
--   SET LOCAL app.tenant_id = '...'; SET LOCAL app.company_id = '...';
--   SET LOCAL app.warehouse_id = '...'; SET LOCAL app.customer_id = '...';
-- A missing setting yields NULL, so RLS policies match nothing (fail closed).
CREATE OR REPLACE FUNCTION app.ctx(name text) RETURNS uuid LANGUAGE sql STABLE AS
$$ SELECT nullif(current_setting('app.' || name, true), '')::uuid $$;

-- Scope match for wildcard config rows (NULL scope column = '*')
CREATE OR REPLACE FUNCTION app.in_scope(c uuid, w uuid, u uuid) RETURNS boolean LANGUAGE sql STABLE AS
$$ SELECT (c IS NULL OR c = app.ctx('company_id'))
      AND (w IS NULL OR w = app.ctx('warehouse_id'))
      AND (u IS NULL OR u = app.ctx('customer_id')) $$;

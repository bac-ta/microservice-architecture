-- =============================================================================
-- LOCAL DEVELOPMENT ONLY: password = role name for every EPL role.
-- docker-compose runs it right after bootstrap.sql on a new volume.
-- Real environments take passwords from the secret store (see bootstrap.sql).
-- =============================================================================
DO $$
DECLARE r text;
BEGIN
    FOR r IN SELECT rolname FROM pg_roles
             WHERE rolname = 'epl_admin' OR (rolname ~ '_(owner|app)$' AND rolname !~ '^pg_')
    LOOP
        EXECUTE format('ALTER ROLE %I PASSWORD %L', r, r);
    END LOOP;
END $$;

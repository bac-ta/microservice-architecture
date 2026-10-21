-- =============================================================================
-- IAM database — owned by service-authentication
-- PostgreSQL 16
--
-- Tenancy here is the *registry*: company / warehouse / customer / tenant.
-- Schemas md and inv reference these four tables with real foreign keys
-- (REFERENCES granted in V3). Their services never query iam tables directly.
--
-- Runs as iam_owner after database/bootstrap/bootstrap.sql.
-- =============================================================================

SET search_path = iam;

-- Runtime role gets DML on every table this migration (and later ones) creates
GRANT USAGE ON SCHEMA iam TO iam_app;
ALTER DEFAULT PRIVILEGES IN SCHEMA iam GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO iam_app;
ALTER DEFAULT PRIVILEGES IN SCHEMA iam GRANT USAGE, SELECT ON SEQUENCES TO iam_app;
-- cross-tenant admin / batch role (BYPASSRLS)
ALTER DEFAULT PRIVILEGES IN SCHEMA iam GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO epl_admin;

-- -----------------------------------------------------------------------------
-- Organisation hierarchy = tenant registry
-- -----------------------------------------------------------------------------

-- Company (e.g. C001)
CREATE TABLE company (
    id            uuid         PRIMARY KEY DEFAULT gen_random_uuid(),
    code          varchar(6)   NOT NULL UNIQUE,
    name          varchar(200) NOT NULL,
    country_code  char(2),
    time_zone     varchar(40)  NOT NULL DEFAULT 'UTC',
    status        varchar(16)  NOT NULL DEFAULT 'ACTIVE'
                  CHECK (status IN ('ACTIVE', 'SUSPENDED', 'CLOSED')),
    created_at    timestamptz  NOT NULL DEFAULT now(),
    created_by    varchar(50)  NOT NULL,
    updated_at    timestamptz  NOT NULL DEFAULT now(),
    updated_by    varchar(50)  NOT NULL,
    version       integer      NOT NULL DEFAULT 0
);

-- Warehouse (e.g. WH01) — unique within a company
CREATE TABLE warehouse (
    id            uuid         PRIMARY KEY DEFAULT gen_random_uuid(),
    company_id    uuid         NOT NULL REFERENCES company (id),
    code          varchar(4)   NOT NULL,
    name          varchar(200) NOT NULL,
    time_zone     varchar(40)  NOT NULL,
    start_date    date,
    end_date      date,
    status        varchar(16)  NOT NULL DEFAULT 'ACTIVE'
                  CHECK (status IN ('ACTIVE', 'SUSPENDED', 'CLOSED')),
    created_at    timestamptz  NOT NULL DEFAULT now(),
    created_by    varchar(50)  NOT NULL,
    updated_at    timestamptz  NOT NULL DEFAULT now(),
    updated_by    varchar(50)  NOT NULL,
    version       integer      NOT NULL DEFAULT 0,
    UNIQUE (company_id, code),
    UNIQUE (company_id, id),          -- target for composite FKs below
    CHECK (end_date IS NULL OR start_date IS NULL OR end_date >= start_date)
);

-- Customer (e.g. CUST01) — the shipper/owner of goods, unique within a company
CREATE TABLE customer (
    id            uuid         PRIMARY KEY DEFAULT gen_random_uuid(),
    company_id    uuid         NOT NULL REFERENCES company (id),
    code          varchar(13)  NOT NULL,
    name          varchar(200) NOT NULL,
    status        varchar(16)  NOT NULL DEFAULT 'ACTIVE'
                  CHECK (status IN ('ACTIVE', 'SUSPENDED', 'CLOSED')),
    created_at    timestamptz  NOT NULL DEFAULT now(),
    created_by    varchar(50)  NOT NULL,
    updated_at    timestamptz  NOT NULL DEFAULT now(),
    updated_by    varchar(50)  NOT NULL,
    version       integer      NOT NULL DEFAULT 0,
    UNIQUE (company_id, code),
    UNIQUE (company_id, id)
);

-- A customer operated in a warehouse (company / warehouse / customer).
-- tenant.id is what every service stores as tenant_id.
CREATE TABLE tenant (
    id              uuid        PRIMARY KEY DEFAULT gen_random_uuid(),
    company_id      uuid        NOT NULL,
    warehouse_id    uuid        NOT NULL,
    customer_id     uuid        NOT NULL,
    -- denormalised codes: used in JWT grants, logs and Kafka events
    company_code    varchar(6)  NOT NULL,
    warehouse_code  varchar(4)  NOT NULL,
    customer_code   varchar(13) NOT NULL,
    start_date      date,
    end_date        date,
    status          varchar(16) NOT NULL DEFAULT 'ACTIVE'
                    CHECK (status IN ('ONBOARDING', 'ACTIVE', 'SUSPENDED', 'CLOSED')),
    created_at      timestamptz NOT NULL DEFAULT now(),
    created_by      varchar(50) NOT NULL,
    updated_at      timestamptz NOT NULL DEFAULT now(),
    updated_by      varchar(50) NOT NULL,
    version         integer     NOT NULL DEFAULT 0,
    -- warehouse and customer must belong to the same company
    FOREIGN KEY (company_id, warehouse_id) REFERENCES warehouse (company_id, id),
    FOREIGN KEY (company_id, customer_id)  REFERENCES customer  (company_id, id),
    UNIQUE (warehouse_id, customer_id),
    UNIQUE (company_code, warehouse_code, customer_code),
    UNIQUE (id, warehouse_id)          -- target for (tenant_id, warehouse_id) FKs in inv
);

-- -----------------------------------------------------------------------------
-- Users and credentials
-- -----------------------------------------------------------------------------

CREATE TABLE app_user (
    id                  uuid         PRIMARY KEY DEFAULT gen_random_uuid(),
    username            varchar(50)  NOT NULL,
    email               varchar(254),
    display_name        varchar(200) NOT NULL,
    -- INTERNAL = warehouse operator staff, CUSTOMER = shipper's staff,
    -- SYSTEM   = machine client (batch, EDI, other services)
    user_type           varchar(16)  NOT NULL
                        CHECK (user_type IN ('INTERNAL', 'CUSTOMER', 'SYSTEM')),
    home_company_id     uuid         REFERENCES company (id),
    home_customer_id    uuid         REFERENCES customer (id),  -- set for CUSTOMER users
    department_code     varchar(20),                     -- division / org
    locale              varchar(10)  NOT NULL DEFAULT 'en',
    time_zone           varchar(40),
    status              varchar(16)  NOT NULL DEFAULT 'ACTIVE'
                        CHECK (status IN ('ACTIVE', 'LOCKED', 'DISABLED')),
    -- bump to invalidate every JWT already issued to this user ("tv" claim)
    token_version       integer      NOT NULL DEFAULT 0,
    failed_login_count  integer      NOT NULL DEFAULT 0,
    locked_until        timestamptz,
    last_login_at       timestamptz,
    created_at          timestamptz  NOT NULL DEFAULT now(),
    created_by          varchar(50)  NOT NULL,
    updated_at          timestamptz  NOT NULL DEFAULT now(),
    updated_by          varchar(50)  NOT NULL,
    version             integer      NOT NULL DEFAULT 0,
    CHECK (user_type <> 'CUSTOMER' OR home_customer_id IS NOT NULL)
);
CREATE UNIQUE INDEX ux_app_user_username ON app_user (lower(username));
CREATE UNIQUE INDEX ux_app_user_email    ON app_user (lower(email)) WHERE email IS NOT NULL;

CREATE TABLE user_credential (
    user_id         uuid         PRIMARY KEY REFERENCES app_user (id) ON DELETE CASCADE,
    password_hash   varchar(255) NOT NULL,               -- bcrypt / argon2id, never reversible
    hash_algorithm  varchar(20)  NOT NULL DEFAULT 'BCRYPT',
    must_change     boolean      NOT NULL DEFAULT false,
    expires_at      timestamptz,
    updated_at      timestamptz  NOT NULL DEFAULT now()
);

-- Prevents password reuse
CREATE TABLE password_history (
    user_id        uuid         NOT NULL REFERENCES app_user (id) ON DELETE CASCADE,
    password_hash  varchar(255) NOT NULL,
    created_at     timestamptz  NOT NULL DEFAULT now(),
    PRIMARY KEY (user_id, created_at)
);

-- Refresh tokens: opaque random value, only its SHA-256 is stored.
-- Rotation: every refresh issues a new row in the same family and revokes the old one;
-- presenting a revoked token revokes the whole family (theft detection).
CREATE TABLE refresh_token (
    id            uuid         PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id       uuid         NOT NULL REFERENCES app_user (id) ON DELETE CASCADE,
    family_id     uuid         NOT NULL,
    token_hash    bytea        NOT NULL UNIQUE,
    client_id     varchar(50)  NOT NULL,                 -- PC_WEB, HHT, API ...
    issued_at     timestamptz  NOT NULL DEFAULT now(),
    expires_at    timestamptz  NOT NULL,
    revoked_at    timestamptz,
    replaced_by   uuid         REFERENCES refresh_token (id),
    client_ip     inet,
    user_agent    varchar(300)
);
CREATE INDEX ix_refresh_token_user   ON refresh_token (user_id) WHERE revoked_at IS NULL;
CREATE INDEX ix_refresh_token_family ON refresh_token (family_id);

-- JWT signing keys, published as JWKS. The private key itself lives in KMS / Vault;
-- this table holds only a reference to it.
CREATE TABLE signing_key (
    kid              varchar(64)  PRIMARY KEY,
    algorithm        varchar(10)  NOT NULL DEFAULT 'ES256'
                     CHECK (algorithm IN ('ES256', 'RS256', 'EdDSA')),
    public_jwk       jsonb        NOT NULL,
    private_key_ref  varchar(300) NOT NULL,
    status           varchar(16)  NOT NULL DEFAULT 'ACTIVE'
                     CHECK (status IN ('NEXT', 'ACTIVE', 'RETIRED')),
    created_at       timestamptz  NOT NULL DEFAULT now(),
    activated_at     timestamptz,
    retired_at       timestamptz
);
CREATE UNIQUE INDEX ux_signing_key_one_active ON signing_key (status) WHERE status = 'ACTIVE';

-- Machine clients (other services, EDI gateway, batch) using client_credentials
CREATE TABLE oauth_client (
    client_id          varchar(50)  PRIMARY KEY,
    client_secret_hash varchar(255),                     -- NULL for public clients (PC web, HHT)
    service_user_id    uuid         REFERENCES app_user (id),
    allowed_grants     text[]       NOT NULL,            -- {password, refresh_token, client_credentials}
    access_token_ttl   interval     NOT NULL DEFAULT '15 minutes',
    refresh_token_ttl  interval     NOT NULL DEFAULT '12 hours',
    enabled            boolean      NOT NULL DEFAULT true,
    created_at         timestamptz  NOT NULL DEFAULT now()
);

-- -----------------------------------------------------------------------------
-- Subject attributes (ABAC "subject")
-- -----------------------------------------------------------------------------

-- Named bundle of responsibilities.
-- In ABAC a role is just one subject attribute that policies can test.
CREATE TABLE role (
    code         varchar(50)  PRIMARY KEY,               -- WMS_OPERATOR, WMS_SUPERVISOR, CUSTOMER_VIEWER ...
    name         varchar(200) NOT NULL,
    description  varchar(500),
    created_at   timestamptz  NOT NULL DEFAULT now(),
    created_by   varchar(50)  NOT NULL
);

-- Where a user may work, and with which roles there.
-- NULL warehouse/customer = '*' (all within the company).
CREATE TABLE user_tenant_grant (
    id            uuid         PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id       uuid         NOT NULL REFERENCES app_user (id) ON DELETE CASCADE,
    company_id    uuid         NOT NULL REFERENCES company (id),
    warehouse_id  uuid,
    customer_id   uuid,
    role_code     varchar(50)  NOT NULL REFERENCES role (code),
    valid_from    date         NOT NULL DEFAULT current_date,
    valid_to      date,
    created_at    timestamptz  NOT NULL DEFAULT now(),
    created_by    varchar(50)  NOT NULL,
    FOREIGN KEY (company_id, warehouse_id) REFERENCES warehouse (company_id, id),
    FOREIGN KEY (company_id, customer_id)  REFERENCES customer  (company_id, id),
    UNIQUE NULLS NOT DISTINCT (user_id, company_id, warehouse_id, customer_id, role_code),
    CHECK (valid_to IS NULL OR valid_to >= valid_from)
);
CREATE INDEX ix_user_tenant_grant_user ON user_tenant_grant (user_id);

-- Free-form subject attributes used by policy conditions,
-- e.g. auth_level = 3, max_adjust_qty = 500, shift = "NIGHT".
CREATE TABLE user_attribute (
    user_id     uuid         NOT NULL REFERENCES app_user (id) ON DELETE CASCADE,
    attr_key    varchar(50)  NOT NULL,
    attr_value  jsonb        NOT NULL,
    in_token    boolean      NOT NULL DEFAULT false,      -- copy into the JWT "attrs" claim
    updated_at  timestamptz  NOT NULL DEFAULT now(),
    updated_by  varchar(50)  NOT NULL,
    PRIMARY KEY (user_id, attr_key)
);

-- -----------------------------------------------------------------------------
-- ABAC policy store
-- -----------------------------------------------------------------------------

-- Things that can be protected, e.g. 'inventory.stock', 'inventory.stocktake'
CREATE TABLE resource_type (
    code         varchar(60)  PRIMARY KEY,
    service      varchar(40)  NOT NULL,                  -- owning service
    description  varchar(500),
    -- JSON schema of attributes the owning service exposes to conditions
    -- (e.g. {"status":"string","bonded":"bool","qty":"number"})
    attributes   jsonb        NOT NULL DEFAULT '{}'
);

-- Operations on a resource type; normally one per gRPC method
CREATE TABLE action (
    resource_type  varchar(60)  NOT NULL REFERENCES resource_type (code),
    code           varchar(40)  NOT NULL,                -- read, adjust, relocate, confirm ...
    grpc_method    varchar(200),                         -- epl.inventory.v1.StockService/Adjust
    description    varchar(500),
    PRIMARY KEY (resource_type, code)
);

-- UI screens: EPL-<domain>-<nnn><channel>, e.g. EPL-IV-120W (docs/screen-codes.md)
CREATE TABLE screen (
    code         varchar(12)  PRIMARY KEY CHECK (code ~ '^EPL-[A-Z]{2}-[0-9]{3}[WH]$'),
    domain       char(2)      NOT NULL,                  -- SY, MD, IB, IV, OB, BL, IT, RP
    channel      char(1)      NOT NULL CHECK (channel IN ('W', 'H')),   -- W = web (PC), H = handheld
    name         varchar(200) NOT NULL,
    CHECK (substr(code, 5, 2) = domain AND right(code, 1) = channel)
);

-- Actions a screen calls; the client BatchChecks them to show or hide buttons
CREATE TABLE screen_action (
    screen_code    varchar(12)  NOT NULL REFERENCES screen (code),
    resource_type  varchar(60)  NOT NULL,
    action         varchar(40)  NOT NULL,
    PRIMARY KEY (screen_code, resource_type, action),
    FOREIGN KEY (resource_type, action) REFERENCES action (resource_type, code)
);

-- A rule: "in <scope>, <effect> <actions> on <resource_type> when <condition>".
-- Scope columns NULL = '*' (matches every company / warehouse / customer).
-- Combining algorithm: default DENY; any matching DENY wins; otherwise any PERMIT.
CREATE TABLE policy (
    id             uuid          PRIMARY KEY DEFAULT gen_random_uuid(),
    code           varchar(100)  NOT NULL,
    description    varchar(1000),
    effect         varchar(6)    NOT NULL CHECK (effect IN ('PERMIT', 'DENY')),
    company_id     uuid          REFERENCES company (id),
    warehouse_id   uuid,
    customer_id    uuid,
    resource_type  varchar(60)   NOT NULL REFERENCES resource_type (code),
    actions        text[]        NOT NULL,               -- {'*'} = every action of the type
    -- CEL expression over subject, resource, action, env; NULL = always true.
    -- e.g.  'WMS_SUPERVISOR' in subject.roles && resource.qty <= subject.attrs.max_adjust_qty
    condition      text,
    priority       integer       NOT NULL DEFAULT 0,     -- evaluation order only (for explain/debug)
    enabled        boolean       NOT NULL DEFAULT true,
    valid_from     timestamptz,
    valid_to       timestamptz,
    created_at     timestamptz   NOT NULL DEFAULT now(),
    created_by     varchar(50)   NOT NULL,
    updated_at     timestamptz   NOT NULL DEFAULT now(),
    updated_by     varchar(50)   NOT NULL,
    version        integer       NOT NULL DEFAULT 0,
    FOREIGN KEY (company_id, warehouse_id) REFERENCES warehouse (company_id, id),
    FOREIGN KEY (company_id, customer_id)  REFERENCES customer  (company_id, id),
    CHECK (warehouse_id IS NULL OR company_id IS NOT NULL),
    CHECK (customer_id  IS NULL OR company_id IS NOT NULL),
    CHECK (cardinality(actions) > 0),
    UNIQUE NULLS NOT DISTINCT (code, company_id, warehouse_id, customer_id)
);
CREATE INDEX ix_policy_resource ON policy (resource_type) WHERE enabled;

-- Monotonic revision; services cache policies and reload when it changes
-- (change is also announced on Kafka through outbox_event).
CREATE TABLE policy_revision (
    id          boolean      PRIMARY KEY DEFAULT true CHECK (id),
    revision    bigint       NOT NULL DEFAULT 0,
    changed_at  timestamptz  NOT NULL DEFAULT now()
);
INSERT INTO policy_revision DEFAULT VALUES;

CREATE FUNCTION bump_policy_revision() RETURNS trigger LANGUAGE plpgsql
    SET search_path = iam AS $$
BEGIN
    UPDATE iam.policy_revision SET revision = revision + 1, changed_at = now();
    RETURN NULL;
END $$;

CREATE TRIGGER trg_policy_revision
    AFTER INSERT OR UPDATE OR DELETE ON policy
    FOR EACH STATEMENT EXECUTE FUNCTION bump_policy_revision();

-- Append-only decision log written asynchronously by every service's PEP.
CREATE TABLE authz_decision_log (
    id             bigint        GENERATED ALWAYS AS IDENTITY,
    occurred_at    timestamptz   NOT NULL DEFAULT now(),
    trace_id       varchar(64),
    user_id        uuid,
    tenant_id      uuid,
    resource_type  varchar(60)   NOT NULL,
    action         varchar(40)   NOT NULL,
    resource_id    varchar(100),
    decision       varchar(6)    NOT NULL CHECK (decision IN ('PERMIT', 'DENY')),
    policy_ids     uuid[],                                -- policies that produced the decision
    reason         varchar(500),
    PRIMARY KEY (id, occurred_at)
) PARTITION BY RANGE (occurred_at);
-- Create monthly partitions with pg_partman or a scheduled job, e.g.:
CREATE TABLE authz_decision_log_default PARTITION OF authz_decision_log DEFAULT;

-- -----------------------------------------------------------------------------
-- Security audit and integration
-- -----------------------------------------------------------------------------

CREATE TABLE login_audit (
    id           bigint       GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    occurred_at  timestamptz  NOT NULL DEFAULT now(),
    username     varchar(50)  NOT NULL,
    user_id      uuid,
    client_id    varchar(50),
    event        varchar(30)  NOT NULL,   -- LOGIN_OK, LOGIN_FAILED, LOCKED, REFRESH, REUSE_DETECTED, LOGOUT
    client_ip    inet,
    user_agent   varchar(300)
);
CREATE INDEX ix_login_audit_user ON login_audit (user_id, occurred_at DESC);

-- Transactional outbox: rows are written in the same transaction as the change,
-- a relay publishes them to Kafka (tenant.created, user.grants.changed, policy.changed ...).
CREATE TABLE outbox_event (
    id              uuid         PRIMARY KEY DEFAULT gen_random_uuid(),
    aggregate_type  varchar(60)  NOT NULL,
    aggregate_id    varchar(100) NOT NULL,
    event_type      varchar(100) NOT NULL,
    payload         jsonb        NOT NULL,
    created_at      timestamptz  NOT NULL DEFAULT now(),
    published_at    timestamptz
);
CREATE INDEX ix_outbox_unpublished ON outbox_event (created_at) WHERE published_at IS NULL;

-- =============================================================================
-- Interactive clients of AuthService/Login and /Refresh (LoginRequest.clientId).
-- Public clients: no secret; token lifetimes = the oauth_client defaults.
-- =============================================================================

SET search_path = iam;

INSERT INTO oauth_client (client_id, allowed_grants) VALUES
    ('PC_WEB', '{password,refresh_token}'),
    ('HHT',    '{password,refresh_token}');

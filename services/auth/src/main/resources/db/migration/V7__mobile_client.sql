-- =============================================================================
-- Login client HHT renamed to MOBILE (LoginRequest.clientId).
-- Refresh tokens already issued to HHT keep working under the new id.
-- =============================================================================

SET search_path = iam;

UPDATE oauth_client SET client_id = 'MOBILE' WHERE client_id = 'HHT';
UPDATE refresh_token SET client_id = 'MOBILE' WHERE client_id = 'HHT';

-- =============================================================================
-- Screens added with the CRUD RPCs (docs/screen-codes.md): role maintenance and printers.
-- =============================================================================

SET search_path = iam;

INSERT INTO screen (code, domain, channel, name) VALUES
    ('EPL-SY-140W', 'SY', 'W', 'Roles'),
    ('EPL-RP-410W', 'RP', 'W', 'Printers');

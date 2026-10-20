-- =============================================================================
-- ABAC catalogue: resource types and actions for the inventory area
-- (legacy GwhSrvcLpWeb). legacy_ref = <screen> / <REST endpoint>.
-- Policies themselves are tenant data and are created through the IAM API.
-- =============================================================================

SET search_path = iam;

INSERT INTO role (code, name, description, created_by) VALUES
    ('WMS_ADMIN',       'Warehouse administrator', 'Configures masters, policies and users for a warehouse', 'migration'),
    ('WMS_SUPERVISOR',  'Warehouse supervisor',    'Confirms stocktakes, approves adjustments',               'migration'),
    ('WMS_OPERATOR',    'Warehouse operator',      'Daily PC / HHT operations',                               'migration'),
    ('WMS_BILLING',     'Billing clerk',           'Warehouse charge statements and month-end close',         'migration'),
    ('CUSTOMER_VIEWER', 'Customer viewer',         'Shipper staff: read-only stock and history',              'migration'),
    ('SYSTEM_BATCH',    'Batch / integration',     'Machine identity for jobs and EDI',                       'migration');

INSERT INTO resource_type (code, service, description, attributes) VALUES
    ('inventory.stock', 'service-inventory', 'Stock lot at a location',
        '{"product_code":"string","location_code":"string","sub_inventory_code":"string",
          "bonded":"bool","on_hold":"bool","locked":"bool","qty":"number"}'),
    ('inventory.movement', 'service-inventory', 'Stock movement ledger / in-out history',
        '{"movement_type":"string","transaction_kind":"string"}'),
    ('inventory.stocktake', 'service-inventory', 'Stocktake (physical count)',
        '{"status":"string","kind":"string"}'),
    ('inventory.serial', 'service-inventory', 'Serial numbers',
        '{"status":"string"}'),
    ('inventory.kitting', 'service-inventory', 'Kitting orders',
        '{"status":"string"}'),
    ('billing.charge', 'service-billing', 'Warehouse charge statement and month-end balance',
        '{"period":"string","status":"string"}'),
    ('integration.upload', 'service-integration', 'CSV / EDI uploads',
        '{"file_kind":"string"}'),
    ('integration.edi', 'service-integration', 'EDI send / receive',
        '{"direction":"string"}');

INSERT INTO action (resource_type, code, grpc_method, description, legacy_ref) VALUES
    ('inventory.stock', 'read',             'epl.inventory.v1.StockService/Search',          'Search stock',                         'epl_lpadj_010 / getGwhTjST'),
    ('inventory.stock', 'adjust',           'epl.inventory.v1.StockService/Adjust',          'Stock adjustment (+/-)',              'epl_lpadj_010 / eplLpadj010Reg'),
    ('inventory.stock', 'relocate',         'epl.inventory.v1.StockService/Relocate',        'Relocation by SKU / all SKU',         'epl_lprls_*, epl_lprla_* / eplLprls*Reg, eplLprla*Reg'),
    ('inventory.stock', 'change_pick_key',  'epl.inventory.v1.StockService/ChangePickKey',   'Cargo ID / picking key change',       'epl_lprlc_010 / eplLprlc010Reg'),
    ('inventory.stock', 'change_sub_inventory', 'epl.inventory.v1.StockService/ChangeSubInventory', 'Sub-inventory change',      'epl_lpsbc_010 / eplLpsbc010Reg'),
    ('inventory.stock', 'divide',           'epl.inventory.v1.StockService/Divide',          'Divide cargo ID',                      'epl_lpdiv_010 / eplLpdiv010Reg'),
    ('inventory.stock', 'export',           'epl.inventory.v1.StockService/Export',          'Large stock download',                 'epl_lpski_* / *DwlBigData'),
    ('inventory.movement', 'read',          'epl.inventory.v1.MovementService/Search',       'In/out history by invoice, product, location', 'epl_lpioh/lpioc/lpios/lpiop/lpiol'),
    ('inventory.movement', 'export',        'epl.inventory.v1.MovementService/Export',       'In/out history download',              'epl_lpioh/lpioc/lpiop / *DwlBigData'),
    ('inventory.stocktake', 'read',         'epl.inventory.v1.StocktakeService/Get',         'Stocktake inquiry',                    'epl_lptki_010..050 / *Get'),
    ('inventory.stocktake', 'create',       'epl.inventory.v1.StocktakeService/Create',      'Stocktake instruction',                'epl_lptki_040 / eplLptki040Ini'),
    ('inventory.stocktake', 'record_count', 'epl.inventory.v1.StocktakeService/RecordCount', 'Enter count results',                  'epl_lptki_060..080, epl_lptks_010 / *Reg'),
    ('inventory.stocktake', 'confirm',      'epl.inventory.v1.StocktakeService/Confirm',     'Confirm and update stock',             'epl_lptki_010 / eplLptki010Cnf, epl_lptku_010'),
    ('inventory.stocktake', 'cancel',       'epl.inventory.v1.StocktakeService/Cancel',      'Cancel stocktake',                     'epl_lptki_010 / eplLptki010Cnc'),
    ('inventory.serial', 'read',            'epl.inventory.v1.SerialService/Search',         'Serial inquiry',                       'epl_lpsri_010'),
    ('inventory.serial', 'register',        'epl.inventory.v1.SerialService/Register',       'Serial registration',                  'epl_lpsre_010 / eplLpsre010Reg'),
    ('inventory.kitting', 'create',         'epl.inventory.v1.KittingService/CreateOrder',   'Kitting order entry',                  'epl_lpkte_010'),
    ('inventory.kitting', 'receive',        'epl.inventory.v1.KittingService/Receive',       'Kitting arrival',                      'epl_lpkta_020'),
    ('inventory.kitting', 'confirm',        'epl.inventory.v1.KittingService/SendConfirmation', 'Send kitting confirmation',         'epl_lpkts_010'),
    ('billing.charge', 'read',              'epl.billing.v1.ChargeService/Get',              'Charge statement inquiry',             'epl_lpwpe_*'),
    ('billing.charge', 'edit',              'epl.billing.v1.ChargeService/Update',           'Edit shipping parameter file',         'epl_lpwpe_* / *Reg, *Mod, *Del'),
    ('billing.charge', 'close_month',       'epl.billing.v1.MonthEndService/Run',            'Month-end balance creation',           'epl_lpwmc_*, epl_lpwbf_* / *Run'),
    ('integration.upload', 'upload',        'epl.integration.v1.UploadService/Upload',       'CSV upload',                           'epl_lpupl_*, epl_lpflb_*'),
    ('integration.edi', 'read',             'epl.integration.v1.EdiService/Search',          'EDI history',                          'epl_lpedi_010'),
    ('integration.edi', 'resend',           'epl.integration.v1.EdiService/Resend',          'EDI resend / re-receive',              'epl_lpedi_010 / *Snd, *Rec, *Rer, epl_lpwes_010');

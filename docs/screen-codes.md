# Screen codes

Every UI screen of EPL has a code:

```
EPL-IV-120W
│   │  │  └─ channel: W = web (PC browser), H = handheld terminal
│   │  └──── number: hundreds = function group, tens = screen in the group (step 10)
│   └─────── domain (one per service)
└─────────── system prefix
```

* The code identifies a screen, not a function: the web and handheld versions of the same
  function share the number and differ in the channel (`EPL-IV-110W`, `EPL-IV-110H`).
* Numbers go up in steps of 10 so a new screen can be put between two existing ones
  (`EPL-IV-115W`). A retired code is never reused.
* The catalogue is the table `iam.screen`; `iam.screen_action` lists the ABAC actions each
  screen calls, so the client can `AuthorizationService/BatchCheck` them and hide denied buttons.
* In `proto/`, each RPC has a `// screens:` comment; an unimplemented RPC answers
  `UNIMPLEMENTED` with the same list. RPCs without screens (`Refresh`, `Logout`, `GetJwks`, `Check`,
  `ResolveBarcode`, `ResolveTenant` …) are shared by every screen or called by services.
* A new screen also needs a row in `iam.screen` through a new auth migration (e.g. `V5__crud_screens.sql`).

## Domains

| Domain | Name | Service |
|---|---|---|
| `SY` | System / IAM | `auth` |
| `MD` | Master data | `masterdata` |
| `IB` | Inbound | `inbound` |
| `IV` | Inventory | `inventory` |
| `OB` | Outbound | `outbound` |
| `BL` | Billing | `billing` |
| `IT` | Integration | `integration` |
| `RP` | Report | `report` |

## SY — System / IAM

| Code | Screen | RPCs |
|---|---|---|
| `EPL-SY-010W` | Login | `AuthService/Login` |
| `EPL-SY-010H` | Login | `AuthService/Login` |
| `EPL-SY-020W` | Work place selection (company / warehouse / customer) | `TenantService/ListTenants` |
| `EPL-SY-020H` | Work place selection (company / warehouse / customer) | `TenantService/ListTenants` |
| `EPL-SY-110W` | User maintenance | `UserService/CreateUser`, `UserService/UpdateUser`, `UserService/GetUser`, `UserService/ListUsers`, `UserService/DeleteUser` |
| `EPL-SY-120W` | User work place grants | `UserService/GrantTenant`, `UserService/ListGrants`, `UserService/RevokeGrant`, `RoleService/ListRoles` |
| `EPL-SY-130W` | Change password | `UserService/ChangePassword` |
| `EPL-SY-140W` | Roles | `RoleService/ListRoles`, `RoleService/UpsertRole`, `RoleService/DeleteRole` |
| `EPL-SY-210W` | Tenant registry | `TenantService/` List / Get / Create / Update / Delete of `Companies`, `Warehouses`, `Customers`; `ListTenants`, `GetTenant`, `UpdateTenant`, `DeleteTenant` |
| `EPL-SY-220W` | Tenant onboarding | `TenantService/CreateTenant` |
| `EPL-SY-310W` | Access policies | `AuthorizationService/ListPolicies`, `AuthorizationService/UpsertPolicy`, `AuthorizationService/DeletePolicy` |

## MD — Master data

| Code | Screen | RPCs |
|---|---|---|
| `EPL-MD-110W` | Areas | `LocationService/ListAreas`, `LocationService/GetArea`, `LocationService/UpsertArea`, `LocationService/DeleteArea` |
| `EPL-MD-120W` | Locations | `LocationService/ListLocations`, `LocationService/GetLocation`, `LocationService/UpsertLocation`, `LocationService/DeleteLocation` |
| `EPL-MD-210W` | Products | `ProductService/GetProduct`, `ProductService/ListProducts`, `ProductService/UpsertProduct`, `ProductService/DeleteProduct` |
| `EPL-MD-210H` | Product inquiry | `ProductService/GetProduct` |
| `EPL-MD-310W` | Delivery destinations | `PartyService/ListDeliveryDestinations` |
| `EPL-MD-320W` | Suppliers | `PartyService/ListSuppliers` |
| `EPL-MD-410W` | Sub-inventories | `ConfigService/ListSubInventories`, `ConfigService/UpsertSubInventory`, `ConfigService/DeleteSubInventory` |
| `EPL-MD-420W` | Picking key definitions | `ConfigService/ListPickingKeyDefinitions` |
| `EPL-MD-430W` | Transaction kinds | `ConfigService/ListTransactionKinds` |
| `EPL-MD-440W` | Statuses and process control | `ConfigService/ListStatuses` |
| `EPL-MD-450W` | Codes and reasons | `ConfigService/ListCodes` |
| `EPL-MD-460W` | Tenant settings | `ConfigService/GetEffectiveSettings` |

## IB — Inbound

| Code | Screen | RPCs |
|---|---|---|
| `EPL-IB-110W` | Arrival entry | `ArrivalService/CreateArrival`, `ArrivalService/UpdateArrival`, `ArrivalService/CancelArrival`, `ArrivalService/DeleteArrival` |
| `EPL-IB-110H` | Unplanned arrival | `ArrivalService/CreateArrival` |
| `EPL-IB-120W` | Arrival inquiry | `ArrivalService/GetArrival`, `ArrivalService/SearchArrivals` |
| `EPL-IB-120H` | Arrival inquiry | `ArrivalService/GetArrival`, `ArrivalService/SearchArrivals` |
| `EPL-IB-210W` | Arrival inspection | `ArrivalService/RecordInspection` |
| `EPL-IB-210H` | Arrival inspection | `ArrivalService/RecordInspection` |
| `EPL-IB-220H` | Put-away | `ArrivalService/RecordPutaway` |
| `EPL-IB-230W` | Arrival serial numbers | `ArrivalService/RegisterSerials` |
| `EPL-IB-310W` | Arrival confirmation | `ArrivalService/ConfirmArrival` |
| `EPL-IB-320W` | Arrival result sending | `ArrivalService/SendArrivalResult` |

## IV — Inventory

| Code | Screen | RPCs |
|---|---|---|
| `EPL-IV-110W` | Stock inquiry | `StockService/Search`, `StockService/Get`, `StockService/Export` |
| `EPL-IV-110H` | Stock inquiry | `StockService/Search`, `StockService/Get` |
| `EPL-IV-120W` | Stock adjustment | `StockService/Adjust` |
| `EPL-IV-130W` | Relocation | `StockService/Relocate` |
| `EPL-IV-130H` | Relocation | `StockService/Relocate` |
| `EPL-IV-140W` | Cargo ID / picking key change | `StockService/ChangePickKey` |
| `EPL-IV-140H` | Cargo ID / picking key change | `StockService/ChangePickKey` |
| `EPL-IV-150W` | Sub-inventory change | `StockService/ChangeSubInventory` |
| `EPL-IV-160W` | Divide cargo ID | `StockService/Divide` |
| `EPL-IV-210W` | In / out history | `MovementService/Search`, `MovementService/Export` |
| `EPL-IV-310W` | Stocktake inquiry | `StocktakeService/List`, `StocktakeService/Get` |
| `EPL-IV-320W` | Stocktake instruction | `StocktakeService/Create`, `StocktakeService/Update` |
| `EPL-IV-330W` | Stocktake count entry | `StocktakeService/RecordCount` |
| `EPL-IV-330H` | Stocktake count entry | `StocktakeService/RecordCount` |
| `EPL-IV-340W` | Stocktake confirmation | `StocktakeService/Confirm`, `StocktakeService/Cancel` |
| `EPL-IV-410W` | Serial inquiry | `SerialService/Search` |
| `EPL-IV-420W` | Serial registration | `SerialService/Register` |
| `EPL-IV-420H` | Serial registration | `SerialService/Register` |
| `EPL-IV-510W` | Kitting order | `KittingService/CreateOrder` |
| `EPL-IV-520W` | Kitting receipt | `KittingService/Receive` |
| `EPL-IV-530W` | Kitting confirmation sending | `KittingService/SendConfirmation` |

## OB — Outbound

| Code | Screen | RPCs |
|---|---|---|
| `EPL-OB-110W` | Shipping order entry | `ShipmentService/CreateShipment`, `ShipmentService/UpdateShipment`, `ShipmentService/CancelShipment`, `ShipmentService/DeleteShipment` |
| `EPL-OB-120W` | Shipping order inquiry | `ShipmentService/GetShipment`, `ShipmentService/SearchShipments` |
| `EPL-OB-120H` | Shipping order inquiry | `ShipmentService/GetShipment`, `ShipmentService/SearchShipments` |
| `EPL-OB-210W` | Allocation | `AllocationService/Allocate`, `AllocationService/CancelAllocation` |
| `EPL-OB-310W` | Picking batch | `PickingService/CreatePickingBatch` |
| `EPL-OB-320H` | Picking | `PickingService/RecordPick` |
| `EPL-OB-410W` | Packing | `PackingService/RecordPacking` |
| `EPL-OB-410H` | Packing | `PackingService/RecordPacking` |
| `EPL-OB-420W` | Packing inquiry by carton | `PackingService/ListCartons` |
| `EPL-OB-420H` | Packing inquiry by carton | `PackingService/ListCartons` |
| `EPL-OB-510W` | Shipping confirmation | `ShipmentService/ConfirmShipment` |
| `EPL-OB-520W` | Shipping result sending | `ShipmentService/SendShipmentResult` |

## BL — Billing

| Code | Screen | RPCs |
|---|---|---|
| `EPL-BL-110W` | Charge rates | `RateService/ListRates`, `RateService/GetRate`, `RateService/UpsertRate`, `RateService/DeleteRate` |
| `EPL-BL-210W` | Charge statement | `ChargeService/Get`, `ChargeService/Update` |
| `EPL-BL-310W` | Month-end close | `MonthEndService/Run`, `MonthEndService/GetStatus` |

## IT — Integration

| Code | Screen | RPCs |
|---|---|---|
| `EPL-IT-110W` | File upload | `UploadService/Upload`, `UploadService/GetUploadErrors` |
| `EPL-IT-210W` | EDI history and resend | `EdiService/Search`, `EdiService/Resend` |
| `EPL-IT-310W` | Batch job monitor | `JobService/ListJobDefinitions`, `JobService/ListJobs`, `JobService/GetJob`, `JobService/CreateJob`, `JobService/DeleteJob` |
| `EPL-IT-320W` | Data purge | `JobService/RunPurge`, `JobService/ListPurgeHistory` |

## RP — Report

| Code | Screen | RPCs |
|---|---|---|
| `EPL-RP-110W` | Report printing | `ReportService/ListReports`, `ReportService/Render` |
| `EPL-RP-210W` | Label printing | `LabelService/PrintLabels` |
| `EPL-RP-210H` | Label printing | `LabelService/PrintLabels` |
| `EPL-RP-310W` | Inventory turnover KPI | `KpiService/GetInventoryTurnover` |
| `EPL-RP-410W` | Printers | `PrinterService/ListPrinters`, `PrinterService/GetPrinter`, `PrinterService/UpsertPrinter`, `PrinterService/DeletePrinter` |

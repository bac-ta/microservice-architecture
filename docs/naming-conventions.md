# Naming conventions (EPL)

## 1. Screen codes

```
EPL-IV-120W
│   │  │  └─ channel: W = web (PC), H = handheld
│   │  └──── number:  hundreds = function group, tens = screen within the group (step 10)
│   └─────── domain:  2 letters, one domain per service
└─────────── system prefix
```

| Domain | Meaning | Service |
|---|---|---|
| `SY` | System / IAM | `auth` |
| `MD` | Master data | `masterdata` |
| `IB` | Inbound | `inbound` |
| `IV` | Inventory | `inventory` |
| `OB` | Outbound | `outbound` |
| `BL` | Billing | `billing` |
| `IT` | Integration | `integration` |
| `RP` | Reporting | `report` |

Rules:

* Regex: `^EPL-[A-Z]{2}-[0-9]{3}[WH]$` (enforced by a CHECK constraint on `iam.screen`).
* A code identifies **one screen**, not one function: the web and handheld versions of the same
  function share the number and differ only in the channel (`EPL-IV-110W` / `EPL-IV-110H`).
* Number in steps of 10 so new screens can be inserted in between (`EPL-IV-115W`). Retired codes
  are **never reused**.
* `0xx` is reserved for screens shared across the domain (e.g. `EPL-SY-010W` login).

Adding a new screen:

1. Add a row to `docs/screen-codes.md`.
2. Add `// screens: …` above every `rpc` the screen calls in `proto/`.
3. Add the code to `NotPorted.error(...)` of the corresponding RPC (while the RPC is not implemented).
4. Seed `iam.screen` and `iam.screen_action` with a **new** migration in `services/auth`
   (never edit a migration that has already run).

## 2. Services, modules, packages

| Item | Rule | Example |
|---|---|---|
| Gradle module | `services:<service>`, `libs:<lib>`; service name is a lowercase English noun | `services:inventory` |
| Spring `application.name` | = service name | `inventory` |
| Root Java package | `com.example.epl.<service>` | `com.example.epl.inventory` |
| Main class | `<Service>ServiceApplication` | `InventoryServiceApplication` |
| Configuration | package `configurations`, class `<Service><Thing>Configuration` | `InventoryGrpcServerConfiguration` |
| gRPC server | package `grpc`, class `<ProtoServiceName>GrpcServer`; only maps calls to services (`GrpcCalls.unary`) | `StockGrpcServer` |
| Business logic | package `services`, class `<Aggregate>Service`, `@Transactional` methods | `ArrivalService`, `StocktakeService` |
| JPA entity | package `entities`, class `<Table>Entity` (`AuditedEntity` for the audit columns) | `ArrivalLineEntity` |
| Repository | package `repositories`, `<Table>Repository extends JpaRepository` | `ArrivalRepository` |
| Entity <-> proto | package `mappers`, final class `<Area>Mappers` with static methods | `ArrivalMappers` |
| Ports | HTTP `81NN`, gRPC `91NN`, `NN` = service order | inventory `8104` / `9104` |

## 3. Proto / gRPC

| Item | Rule | Example |
|---|---|---|
| File | `proto/src/main/proto/epl/<service>/v1/<service>.proto` | `epl/inventory/v1/inventory.proto` |
| Package | `epl.<service>.v1` | `epl.inventory.v1` |
| `java_package` | `com.example.epl.<service>.v1` | |
| Service | `<Noun>Service` | `StockService`, `StocktakeService` |
| RPC | Verb + noun, PascalCase | `SearchArrivals`, `ConfirmShipment` |
| CRUD RPCs | `Create<X>` / `Get<X>` / `List<X>s` (or `Search<X>s` with filters) / `Update<X>` / `Delete<X>`; `Upsert<X>` for masters keyed by a code | `GetArea`, `UpsertProduct`, `DeleteArrival` |
| Updates / deletes | send the version read (`audit.version` or a `version` field): a stale one answers `ABORTED`; `Upsert` of an existing key without it answers `ALREADY_EXISTS` | |
| Errors | `NOT_FOUND`, `ALREADY_EXISTS`, `INVALID_ARGUMENT`, `FAILED_PRECONDITION` (state or references), `ABORTED` (version), `PERMISSION_DENIED` | |
| Message | `<Rpc>Request` / `<Rpc>Response`, entities are nouns | `SearchArrivalsRequest`, `Arrival` |
| Field | `snake_case`; document numbers `*_no`, codes `*_code`, dates `*_date`, quantities `*_qty` | `arrival_no`, `product_code` |
| Enum | Values `<ENUM>_<VALUE>`, value 0 = `<ENUM>_UNSPECIFIED` | `USER_TYPE_CUSTOMER` |
| RPC comment | `// screens: EPL-…` (omit if the RPC is shared by all screens) | |
| Full method name | `epl.<service>.v1.<Service>/<Rpc>` (used in `iam.action.grpc_method`) | `epl.inventory.v1.StockService/Adjust` |

Backward-incompatible contract changes go into a new `v2`; never modify `v1`.

## 4. Database

| Item | Rule | Example |
|---|---|---|
| Database | `epl` (one DB, one schema per service) | |
| Schema | short abbreviation of the service (list in `database/bootstrap/bootstrap.sql`) | `iam`, `md`, `inv`, `inb`, `outb`, `bill`, `intg`, `rpt` |
| Role | `<schema>_owner` (migrate job), `<schema>_app` (running service) | `inv_owner`, `inv_app` |
| Table | `snake_case`, singular | `stock`, `stock_movement`, `stocktake_line` |
| Primary key | `id uuid` (large ledger tables: `bigint identity`) | |
| Foreign key | `<table>_id` | `tenant_id`, `warehouse_id` |
| Business code | `code` in its own table, `<thing>_code` in other tables | `product_code` |
| Flag | `boolean`, adjective / `is_` name, never `*_flg` | `on_hold`, `locked` |
| Audit | `created_at/by`, `updated_at/by`, `version` | |
| Index | `ix_<table>_<column/purpose>` | `ix_stock_available` |
| Flyway migration | `V<n>__<description>.sql`, sequential per service, snake_case; **never edit a file that has run**, always add a new one | `V5__screen_seed.sql` |
| Repeatable migration | `R__<description>.sql`, only for `CREATE OR REPLACE` views / functions | `R__effective_views.sql` |
| Constraint | `ck_<table>_<rule>`, unique index `ux_<table>_<columns>` | `ck_stock_qty_balance`, `ux_app_user_email` |
| RLS policy | `p_tenant`, `p_warehouse`, `p_scope_read` | |

## 5. Authorization (ABAC)

| Item | Rule | Example |
|---|---|---|
| Resource type | `<service>.<resource>` | `inventory.stock`, `billing.charge` |
| Action | `snake_case`, verb | `read`, `adjust`, `change_pick_key`, `close_month` |
| Role | `UPPER_SNAKE`, grouped by business function | `WMS_OPERATOR`, `CUSTOMER_VIEWER` |
| Policy code | `kebab-case` describing the rule | `adjust-within-limit` |

## 6. Events (Kafka / outbox)

`<business domain>.<entity>.<past-tense verb>`, lowercase, e.g. `stock.adjusted`,
`stocktake.confirmed`, `inbound.arrival.confirmed`, `user.grants.changed`.

## 7. Configuration and sample data

* Environment variables: `UPPER_SNAKE`, prefixed by group: `DB_*` (runtime role),
  `FLYWAY_*` (schema owner, migrate job only), `GRPC_SERVER_*`, `GRPC_<BACKEND>_TARGET`, `HTTP_SERVER_PORT`.
* Spring profile `migrate` = migrate job (see `database/README.md`).
* Tenant headers: `x-company`, `x-warehouse`, `x-customer`.
* Upload file type codes: `UPPER_SNAKE` by business entity (`ARRIVAL`, `SHIPMENT`, `PRODUCT`), never
  numeric codes.
* Sample data in tests / docs: company `C001`, warehouse `WH01`, customer `CUST01`,
  product `P001`.

# Microservice architecture (gRPC, K8s, AWS...)
![Image architecture](assets/eks-architecture.drawio.png)

## Repository layout

One Gradle multi-project build (monorepo). Each service is still built and deployed on its own.

```
.
├── settings.gradle            all modules
├── gradle/libs.versions.toml  every dependency version, in one place
├── build-logic/               convention plugins: epl.java-library, epl.proto-library, epl.spring-boot-service, epl.database-service
├── proto/                     .proto contracts -> generated gRPC stubs (com.example.epl.<service>.v1)
├── libs/
│   ├── grpc-server/           @GrpcServer, server lifecycle, interceptor registry
│   ├── grpc-client/           @GrpcClient, channel factory, discovery name resolver
│   ├── security/              gRPC auth/header/error interceptors, password hashing, error handling
│   └── database/              PostgreSQL + Flyway: validate at runtime, migrate in the migrate job
├── services/                  deployable services (below)
└── database/                  PostgreSQL design, bootstrap script, migration tests (see database/README.md)
```

## Services

| Service | HTTP / gRPC | Schema | Screens | gRPC contract |
|---|---|---|---|---|
| `gateway` | 5555 / — | — | — | REST/JSON edge for every contract (see below) |
| `auth` | 8101 / 9101 | `iam` | `EPL-SY-*` | `epl/auth/v1` |
| `masterdata` | 8102 / 9102 | `md` | `EPL-MD-*` | `epl/masterdata/v1` |
| `inbound` | 8103 / 9103 | `inb` | `EPL-IB-*` | `epl/inbound/v1` |
| `inventory` | 8104 / 9104 | `inv` | `EPL-IV-*` | `epl/inventory/v1` |
| `outbound` | 8105 / 9105 | `outb` | `EPL-OB-*` | `epl/outbound/v1` |
| `billing` | 8106 / 9106 | `bill` | `EPL-BL-*` | `epl/billing/v1` |
| `integration` | 8107 / 9107 | `intg` | `EPL-IT-*` | `epl/integration/v1` |
| `report` | 8108 / 9108 | `rpt` | `EPL-RP-*` | `epl/report/v1` |

Contracts live in `proto/src/main/proto/epl/<service>/v1/`; each RPC is commented with the
screens that call it. Screen codes (`EPL-<domain>-<nnn><channel>`) are listed in
[`docs/screen-codes.md`](docs/screen-codes.md); naming rules for code, proto, DB and events are in
[`docs/naming-conventions.md`](docs/naming-conventions.md). The services expose every RPC already; one that
is not implemented yet answers `UNIMPLEMENTED` naming its screens.

Services with a schema need PostgreSQL prepared by `database/bootstrap/bootstrap.sql`
(`docker compose up -d postgres` locally). Their migrations are applied by a separate migrate job,
not when the service starts: `./gradlew :services:<service>:bootMigrate` locally, a Kubernetes Job
with `--spring.profiles.active=migrate` in an environment. Run `auth` first on an empty database (the
other schemas reference `iam`). A running service only validates its schema and refuses to start
when a migration is pending. Connection settings: `DB_HOST`, `DB_PORT`, `DB_NAME`,
`DB_USERNAME` / `DB_PASSWORD` (runtime role), `FLYWAY_USERNAME` / `FLYWAY_PASSWORD` (schema owner,
migrate job only). Details in [`database/README.md`](database/README.md#migrations-flyway).

Modules depend on each other with `project(':libs:security')` etc., so the build no longer
needs Nexus.

## Gateway (REST/JSON edge)

`services/gateway` exposes every RPC of the generated `com.example.epl.*` stubs over HTTP, so a new
service or RPC in `proto/` needs no gateway code. The backend of an RPC comes from its proto package
(`epl.inventory.v1.StockService` -> `inventory`) and its gRPC target from `epl.gateway.backends`
(`GRPC_<BACKEND>_TARGET`, e.g. `GRPC_INVENTORY_TARGET=dns:///inventory:9104`).

| Request | Meaning |
|---|---|
| `POST /api/rpc/{package.Service}/{Method}` | body = request message in [proto3 JSON](https://protobuf.dev/programming-guides/json/); unary -> JSON, server-streaming -> NDJSON (one message per line) |
| `GET /api/rpc` | every exposed RPC with its backend, type and message names |
| `POST /api/files/upload` | multipart `file`, `fileKind`, `validateOnly` -> `UploadService/Upload` (client streaming) |
| `POST /api/reports/render` | `RenderRequest` JSON -> file download from `ReportService/Render` |
| `GET /api/actuator/health` | liveness / readiness |

```bash
curl -X POST localhost:5555/api/rpc/epl.masterdata.v1.ProductService/GetProduct \
  -H "Authorization: Bearer $TOKEN" -H "X-Company: C001" -H "X-Warehouse: WH01" -H "X-Customer: CUST01" \
  -H "Content-Type: application/json" -d '{"code":"P001"}'
```

- **Auth**: every path except `epl.gateway.security.public-paths` (Login, Refresh, GetJwks, health) needs a JWT
  signed by the auth service; keys come from `AuthService/GetJwks` (cached, `JWT_ISSUER` checked when set).
  Backends verify the token again and enforce tenant grants. `GATEWAY_SECURITY_ENABLED=false` turns the check
  off for local development while `GetJwks` is not ported.
- **Forwarded as gRPC metadata**: `Authorization`, `X-Company`, `X-Warehouse`, `X-Customer`, `Accept-Language`,
  `X-Request-Id` (generated when missing, echoed in the response).
- **Errors**: gRPC status -> HTTP status (`NOT_FOUND` 404, `INVALID_ARGUMENT` 400, `PERMISSION_DENIED` 403,
  `UNIMPLEMENTED` 501, `UNAVAILABLE` 503 …), body `{"code","message","requestId"}`.

## Build

Requires JDK 21 (the Gradle wrapper downloads Gradle 8.14.5, which cannot run on JDK 25+). The migration tests need Docker.

```bash
./gradlew build                          # everything: compile, test, jars
./gradlew :services:gateway:bootJar      # one service -> services/gateway/build/libs/gateway-*.jar
./gradlew :services:gateway:bootRun      # run one service locally
./gradlew :services:inventory:bootMigrate   # apply one service's migrations, then exit
./gradlew :database:test                 # bootstrap + every migration on PostgreSQL 16 (Testcontainers)
```

### Adding a module

1. Create the folder with a `build.gradle` that applies one convention plugin:
   ```groovy
   plugins { id 'epl.spring-boot-service' }   // or epl.database-service / epl.java-library / epl.proto-library
   dependencies {
       implementation project(':proto')
       implementation project(':libs:security')
       implementation libs.spring.boot.starter.web
   }
   ```
2. Add it to `settings.gradle` (`include 'services:inventory'`).
3. New third-party versions go into `gradle/libs.versions.toml`, never into a module's `build.gradle`.

## Nexus (optional, Docker images / external artifacts only)

```bash
docker compose up -d
sudo docker exec -it microservice-architecture-starter-kit-nexus-1 cat /nexus-data/admin.password
```

Nexus URL: http://localhost:8081. Keep its credentials out of the repository
(use `~/.gradle/gradle.properties` or environment variables).

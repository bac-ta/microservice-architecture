# Microservice architecture (gRPC, K8s, AWS...)
![Image architecture](assets/eks-architecture.drawio.png)

## Repository layout

One Gradle multi-project build (monorepo). Each service is still built and deployed on its own.

```
.
├── settings.gradle            all modules
├── gradle/libs.versions.toml  every dependency version, in one place
├── build-logic/               convention plugins: epl.java-library, epl.proto-library, epl.spring-boot-service
├── proto/                     .proto contracts -> generated gRPC stubs (com.example.epl.<service>.v1)
├── libs/
│   ├── grpc-server/           @GrpcServer, server lifecycle, interceptor registry
│   ├── grpc-client/           @GrpcClient, channel factory, discovery name resolver
│   └── security/              gRPC auth/header/error interceptors, password hashing, error handling
├── services/                  deployable services (below)
└── database/                  PostgreSQL design, bootstrap script, ER diagram (see database/README.md)
```

## Services

| Service | HTTP / gRPC | Schema | Rebuilt from legacy GWH | gRPC contract |
|---|---|---|---|---|
| `gateway` | 5555 / — | — | — | REST/JSON edge for every contract (see below) |
| `auth` | 8101 / 9101 | `iam` | NCU users / roles, AGS_PR_TOKEN_010, GWH_PR_PRTTN_010, mpuah | `epl/auth/v1` |
| `masterdata` | 8102 / 9102 | `md` | GwhSrvcMpWeb, MbWeb, MhWeb, CpWeb | `epl/masterdata/v1` |
| `inbound` | 8103 / 9103 | — | GwhSrvcApWeb, AbWeb, AhWeb | `epl/inbound/v1` |
| `inventory` | 8104 / 9104 | `inv` | GwhSrvcLpWeb, LbWeb, LhWeb | `epl/inventory/v1` |
| `outbound` | 8105 / 9105 | — | GwhSrvcSpWeb, SbWeb, ShWeb | `epl/outbound/v1` |
| `billing` | 8106 / 9106 | — | lpw*, mpw*, rpwcs | `epl/billing/v1` |
| `integration` | 8107 / 9107 | — | lpupl / lpedi / lpwes, GwhSrvcUpWeb, GwhSrvcDpWeb | `epl/integration/v1` |
| `report` | 8108 / 9108 | — | GwhSrvcRpWeb, RbWeb, KpWeb, GwhEasWeb | `epl/report/v1` |

Contracts live in `proto/src/main/proto/epl/<service>/v1/`. The EPL services expose every RPC
already; each one answers `UNIMPLEMENTED` with the legacy screen / procedure to port
(see the Javadoc in `services/<service>/src/main/java/.../grpc/*GrpcServer.java`).
The legacy AGS low-code screen engine (`GwhSrvcStWeb`) is not rebuilt as a service.

Services with a schema need PostgreSQL prepared by `database/bootstrap/bootstrap.sql`; start
`auth` before `masterdata` / `inventory` on an empty database (their tables reference `iam`).
Connection settings: `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USERNAME` / `DB_PASSWORD` (runtime role),
`FLYWAY_USERNAME` / `FLYWAY_PASSWORD` (schema owner).

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
  -H "Authorization: Bearer $TOKEN" -H "X-Company: 530014" -H "X-Warehouse: BOND" -H "X-Customer: BOND01" \
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

Requires JDK 21 (the Gradle wrapper downloads Gradle 8.8).

```bash
./gradlew build                          # everything: compile, test, jars
./gradlew :services:gateway:bootJar      # one service -> services/gateway/build/libs/gateway-*.jar
./gradlew :services:gateway:bootRun      # run one service locally
```

### Adding a module

1. Create the folder with a `build.gradle` that applies one convention plugin:
   ```groovy
   plugins { id 'epl.spring-boot-service' }   // or epl.java-library / epl.proto-library
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

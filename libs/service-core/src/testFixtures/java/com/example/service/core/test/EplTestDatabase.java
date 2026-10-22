package com.example.service.core.test;

import org.flywaydb.core.Flyway;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.testcontainers.containers.PostgreSQLContainer;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * One PostgreSQL 16 per test JVM, prepared like an environment: bootstrap.sql, dev passwords
 * (password = role name) and, for services other than auth, the iam migrations plus the tenants
 * C001 / WH01 / CUST01 and CUST02. The service's own migrations run in its Spring context
 * (migrate mode), which then connects as its app role, like in production.
 * <pre>{@code
 * @DynamicPropertySource
 * static void database(DynamicPropertyRegistry registry) {
 *     EplTestDatabase.register(registry, "md");
 * }
 * }</pre>
 */
public final class EplTestDatabase {

    public static final String COMPANY = "C001";
    public static final String WAREHOUSE = "WH01";
    public static final String CUSTOMER = "CUST01";
    public static final String OTHER_CUSTOMER = "CUST02";

    private static final Path ROOT = Path.of(System.getProperty("epl.repository.root", "../.."));

    private static PostgreSQLContainer<?> postgres;
    private static boolean registryReady;

    private EplTestDatabase() {
    }

    public static synchronized PostgreSQLContainer<?> postgres() {
        if (postgres == null) {
            postgres = new PostgreSQLContainer<>("postgres:16-alpine").withDatabaseName("epl");
            postgres.start();
            execute(read("database/bootstrap/bootstrap.sql"));
            execute(read("database/local/dev-passwords.sql"));
        }
        return postgres;
    }

    /** Datasource (app role), Flyway (owner, migrate mode) and no gRPC port for a service owning {@code schema}. */
    public static void register(DynamicPropertyRegistry registry, String schema) {
        var container = postgres();
        if (!"iam".equals(schema)) {
            prepareTenantRegistry();
        }
        registry.add("spring.datasource.url", () -> container.getJdbcUrl() + "&currentSchema=" + schema);
        registry.add("spring.datasource.username", () -> schema + "_app");
        registry.add("spring.datasource.password", () -> schema + "_app");
        registry.add("spring.flyway.user", () -> schema + "_owner");
        registry.add("spring.flyway.password", () -> schema + "_owner");
        registry.add("epl.database.migration.mode", () -> "migrate");
        registry.add("app.grpc.server.enabled", () -> "false");
    }

    private static synchronized void prepareTenantRegistry() {
        if (registryReady) {
            return;
        }
        Flyway.configure()
                .dataSource(postgres.getJdbcUrl(), "iam_owner", "iam_owner")
                .schemas("iam").defaultSchema("iam").createSchemas(false)
                .locations("filesystem:" + ROOT.resolve("services/auth/src/main/resources/db/migration"))
                .load()
                .migrate();
        execute("""
                INSERT INTO iam.company (code, name, created_by, updated_by) VALUES ('C001', 'Company', 'test', 'test');
                INSERT INTO iam.warehouse (company_id, code, name, time_zone, created_by, updated_by)
                    SELECT id, 'WH01', 'Warehouse', 'UTC', 'test', 'test' FROM iam.company WHERE code = 'C001';
                INSERT INTO iam.customer (company_id, code, name, created_by, updated_by)
                    SELECT id, c, c, 'test', 'test' FROM iam.company, unnest(ARRAY['CUST01', 'CUST02']) c WHERE code = 'C001';
                INSERT INTO iam.tenant (company_id, warehouse_id, customer_id, company_code, warehouse_code,
                                        customer_code, status, created_by, updated_by)
                    SELECT w.company_id, w.id, u.id, 'C001', 'WH01', u.code, 'ACTIVE', 'test', 'test'
                    FROM iam.warehouse w JOIN iam.customer u ON u.company_id = w.company_id;
                """);
        registryReady = true;
    }

    static void execute(String sql) {
        try (var connection = DriverManager.getConnection(postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword());
             var statement = connection.createStatement()) {
            statement.execute(sql);
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }

    private static String read(String path) {
        try {
            return Files.readString(ROOT.resolve(path));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}

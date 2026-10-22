package com.example.database;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Runs the deployment order of database/README.md on an empty PostgreSQL 16:
 * bootstrap.sql (twice, it must be idempotent), then each service's migrations as its schema owner,
 * then the runtime check each service does at start-up (validate as its app role).
 */
@Testcontainers(disabledWithoutDocker = true)
class MigrationTests {

    // schema -> service, in the order the migrate jobs must run (iam first: the others reference it)
    private static final Map<String, String> SCHEMAS = new LinkedHashMap<>();

    static {
        SCHEMAS.put("iam", "auth");
        SCHEMAS.put("md", "masterdata");
        SCHEMAS.put("inv", "inventory");
        SCHEMAS.put("inb", "inbound");
        SCHEMAS.put("outb", "outbound");
        SCHEMAS.put("bill", "billing");
        SCHEMAS.put("intg", "integration");
        SCHEMAS.put("rpt", "report");
    }

    private static final Path ROOT = Path.of(System.getProperty("epl.repository.root", ".."));

    @Container
    private static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("epl");

    private static UUID tenantA;
    private static UUID tenantB;
    private static UUID warehouseId;

    @BeforeAll
    static void migrate() throws Exception {
        try (var connection = superuser()) {
            execute(connection, Files.readString(ROOT.resolve("database/bootstrap/bootstrap.sql")));
            execute(connection, Files.readString(ROOT.resolve("database/bootstrap/bootstrap.sql")));
            execute(connection, Files.readString(ROOT.resolve("database/local/dev-passwords.sql")));
        }
        SCHEMAS.forEach((schema, service) -> flyway(schema, service, schema + "_owner").migrate());
        seedTenants();
    }

    @Test
    void runningServicesValidateWithTheirAppRole() {
        SCHEMAS.forEach((schema, service) -> flyway(schema, service, schema + "_app").validate());
    }

    @Test
    void migrationHistoryIsReadOnlyForAppRoles() throws SQLException {
        try (var connection = login("inv_app")) {
            assertThatThrownBy(() -> execute(connection, "DELETE FROM inv.flyway_schema_history"))
                    .hasMessageContaining("permission denied");
        }
    }

    @Test
    void appRolesCannotReadOtherSchemas() throws SQLException {
        try (var connection = login("outb_app")) {
            assertThatThrownBy(() -> execute(connection, "SELECT 1 FROM inv.stock"))
                    .hasMessageContaining("permission denied");
            assertThatThrownBy(() -> execute(connection, "SELECT 1 FROM iam.tenant"))
                    .hasMessageContaining("permission denied");
        }
    }

    @Test
    void rowLevelSecurityKeepsTenantsApart() throws SQLException {
        try (var connection = login("inb_app")) {
            connection.setAutoCommit(false);
            setTenant(connection, tenantA);
            execute(connection, """
                    INSERT INTO inb.arrival (tenant_id, warehouse_id, arrival_no, scheduled_date, transaction_kind,
                                             created_by, updated_by)
                    VALUES ('%s', '%s', 'A0001', current_date, '010', 'test', 'test')
                    """.formatted(tenantA, warehouseId));
            assertThat(count(connection, "inb.arrival")).isEqualTo(1);

            // another tenant sees nothing and cannot write into tenant A
            setTenant(connection, tenantB);
            assertThat(count(connection, "inb.arrival")).isZero();
            assertThatThrownBy(() -> execute(connection, """
                    INSERT INTO inb.arrival (tenant_id, warehouse_id, arrival_no, scheduled_date, transaction_kind,
                                             created_by, updated_by)
                    VALUES ('%s', '%s', 'A0002', current_date, '010', 'test', 'test')
                    """.formatted(tenantA, warehouseId)))
                    .hasMessageContaining("row-level security");
            connection.rollback();
        }
    }

    @Test
    void noTenantContextMeansNoRows() throws SQLException {
        try (var connection = login("outb_app")) {
            assertThat(count(connection, "outb.shipment")).isZero();
        }
    }

    // -------------------------------------------------------------------------

    private static Flyway flyway(String schema, String service, String role) {
        return Flyway.configure()
                .dataSource(POSTGRES.getJdbcUrl(), role, role)
                .schemas(schema)
                .defaultSchema(schema)
                .createSchemas(false)
                .validateMigrationNaming(true)
                .locations("filesystem:" + ROOT.resolve("services/" + service + "/src/main/resources/db/migration"))
                .load();
    }

    private static void seedTenants() throws SQLException {
        try (var connection = superuser()) {
            execute(connection, """
                    INSERT INTO iam.company (code, name, created_by, updated_by) VALUES ('C001', 'Company', 't', 't');
                    INSERT INTO iam.warehouse (company_id, code, name, time_zone, created_by, updated_by)
                        SELECT id, 'WH01', 'Warehouse', 'UTC', 't', 't' FROM iam.company;
                    INSERT INTO iam.customer (company_id, code, name, created_by, updated_by)
                        SELECT id, c, c, 't', 't' FROM iam.company, unnest(ARRAY['CUST01', 'CUST02']) c;
                    INSERT INTO iam.tenant (company_id, warehouse_id, customer_id, company_code, warehouse_code,
                                            customer_code, created_by, updated_by)
                        SELECT w.company_id, w.id, u.id, 'C001', 'WH01', u.code, 't', 't'
                        FROM iam.warehouse w JOIN iam.customer u ON u.company_id = w.company_id;
                    """);
            try (var rs = connection.createStatement().executeQuery(
                    "SELECT id, warehouse_id FROM iam.tenant ORDER BY customer_code")) {
                rs.next();
                tenantA = rs.getObject(1, UUID.class);
                warehouseId = rs.getObject(2, UUID.class);
                rs.next();
                tenantB = rs.getObject(1, UUID.class);
            }
        }
    }

    private static void setTenant(Connection connection, UUID tenantId) throws SQLException {
        execute(connection, "SET LOCAL app.tenant_id = '" + tenantId + "'");
    }

    private static long count(Connection connection, String table) throws SQLException {
        try (var rs = connection.createStatement().executeQuery("SELECT count(*) FROM " + table)) {
            rs.next();
            return rs.getLong(1);
        }
    }

    private static void execute(Connection connection, String sql) throws SQLException {
        try (var statement = connection.createStatement()) {
            statement.execute(sql);
        }
    }

    private static Connection superuser() throws SQLException {
        return DriverManager.getConnection(POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword());
    }

    private static Connection login(String role) throws SQLException {
        return DriverManager.getConnection(POSTGRES.getJdbcUrl(), role, role);
    }
}

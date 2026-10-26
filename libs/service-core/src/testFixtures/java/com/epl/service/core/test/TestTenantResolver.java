package com.epl.service.core.test;

import com.epl.auth.v1.ResolvedTenant;
import com.epl.service.core.Errors;
import com.epl.service.core.TenantResolver;

import java.sql.DriverManager;
import java.sql.SQLException;

/** Resolves the tenant metadata from the test database's iam tables instead of calling the auth service. */
public class TestTenantResolver implements TenantResolver {

    @Override
    public ResolvedTenant resolve(String companyCode, String warehouseCode, String customerCode) {
        var postgres = EplTestDatabase.postgres();
        try (var connection = DriverManager.getConnection(postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword());
             var statement = connection.prepareStatement("""
                     SELECT c.id, w.id, u.id, t.id
                     FROM iam.company c
                     LEFT JOIN iam.warehouse w ON w.company_id = c.id AND w.code = ?
                     LEFT JOIN iam.customer u ON u.company_id = c.id AND u.code = ?
                     LEFT JOIN iam.tenant t ON t.warehouse_id = w.id AND t.customer_id = u.id
                     WHERE c.code = ?
                     """)) {
            statement.setString(1, warehouseCode);
            statement.setString(2, customerCode);
            statement.setString(3, companyCode);
            try (var rs = statement.executeQuery()) {
                if (!rs.next()) {
                    throw Errors.notFound("Company", companyCode);
                }
                var builder = ResolvedTenant.newBuilder().setCompanyId(rs.getString(1));
                if (warehouseCode != null) {
                    builder.setWarehouseId(require(rs.getString(2), "Warehouse", warehouseCode));
                }
                if (customerCode != null) {
                    builder.setCustomerId(require(rs.getString(3), "Customer", customerCode));
                    builder.setTenantId(require(rs.getString(4), "Tenant", customerCode));
                }
                return builder.build();
            }
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }

    private static String require(String id, String what, String code) {
        if (id == null) {
            throw Errors.notFound(what, code);
        }
        return id;
    }
}

package com.epl.inventory;

import com.epl.common.v1.Date;
import com.epl.common.v1.PickKeys;
import com.epl.inventory.services.LotService;
import com.epl.inventory.v1.CancelStocktakeRequest;
import com.epl.inventory.v1.CreateStocktakeRequest;
import com.epl.inventory.v1.GetStockRequest;
import com.epl.inventory.v1.GetStocktakeRequest;
import com.epl.inventory.v1.ListStocktakesRequest;
import com.epl.inventory.v1.SearchStockRequest;
import com.epl.inventory.v1.StockServiceGrpc;
import com.epl.inventory.v1.Stocktake;
import com.epl.inventory.v1.StocktakeServiceGrpc;
import com.epl.inventory.v1.StocktakeStatus;
import com.epl.inventory.v1.UpdateStocktakeRequest;
import com.epl.service.core.RequestContext;
import com.epl.service.core.test.EplServiceTestConfiguration;
import com.epl.service.core.test.EplTestDatabase;
import com.epl.service.core.test.GrpcTestServer;
import com.epl.service.core.test.TestTenantResolver;
import io.grpc.Context;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Testcontainers(disabledWithoutDocker = true)
@Import(EplServiceTestConfiguration.class)
class InventoryCrudTests {

    @DynamicPropertySource
    static void database(DynamicPropertyRegistry registry) {
        EplTestDatabase.register(registry, "inv");
    }

    @Autowired
    private GrpcTestServer grpc;

    /**
     * Stock is created by receipts (inbound events), not by CRUD: seed two lots of CUST01 directly.
     * The parameter makes Spring start the context, which runs the inv migrations, first.
     */
    @BeforeAll
    static void seedStock(@Autowired GrpcTestServer ignored) {
        EplTestDatabase.execute("""
                INSERT INTO inv.lot (tenant_id, product_code, pick_key1, lot_no, expiry_date, created_by)
                SELECT t.id, 'P001', 'LOT' || l, 'LOT' || l, DATE '2027-01-01' + l, 'test'
                FROM iam.tenant t, generate_series(1, 2) l
                WHERE t.customer_code = 'CUST01'
                ON CONFLICT DO NOTHING;
                INSERT INTO inv.stock (id, tenant_id, warehouse_id, receipt_no, receipt_line, receipt_seq, stock_kind,
                                       received_on, product_code, lot_id, location_code, area_code, sub_inventory_code,
                                       physical_qty, available_qty, allocated_qty, created_by, updated_by)
                SELECT gen_random_uuid(), t.id, t.warehouse_id, 'AR00000001', l, 1, '1', current_date, 'P001',
                       lot.id, 'A01-R1-0' || l, 'A01', 'GOOD', 10 * l, 10 * l, 0, 'test', 'test'
                FROM iam.tenant t, generate_series(1, 2) l
                JOIN inv.lot lot ON lot.pick_key1 = 'LOT' || l
                WHERE t.customer_code = 'CUST01' AND lot.tenant_id = t.id
                ON CONFLICT DO NOTHING;
                """);
    }

    @Test
    void searchAndGetStock() {
        var stock = StockServiceGrpc.newBlockingStub(grpc.tenantChannel());
        var all = stock.search(SearchStockRequest.newBuilder().setProductCode("P001").build());
        assertThat(all.getLotsList()).hasSize(2);
        assertThat(all.getPage().getTotalSize()).isEqualTo(2);

        var lot2 = stock.search(SearchStockRequest.newBuilder()
                .setPickKeys(PickKeys.newBuilder().setKey1("LOT2")).build()).getLots(0);
        assertThat(lot2.getPhysicalQty().getValue()).isEqualTo("20.000");
        assertThat(lot2.getLotNo()).isEqualTo("LOT2");
        assertThat(lot2.getExpiryDate()).isEqualTo(Date.newBuilder().setYear(2027).setMonth(1).setDay(3).build());
        assertThat(lot2.getLotId()).isNotEmpty();
        assertThat(stock.get(GetStockRequest.newBuilder().setId(lot2.getId()).build()).getLocationCode()).isEqualTo("A01-R1-02");

        // other customer of the warehouse: no rows (RLS), and the lot id is not found
        var other = StockServiceGrpc.newBlockingStub(grpc.channel("C001", "WH01", "CUST02"));
        assertThat(other.search(SearchStockRequest.getDefaultInstance()).getLotsList()).isEmpty();
        assertThatThrownBy(() -> other.get(GetStockRequest.newBuilder().setId(lot2.getId()).build()))
                .satisfies(e -> assertThat(code(e)).isEqualTo(Status.Code.NOT_FOUND));
    }

    @Test
    void stocktakeLifecycle() {
        var stocktakes = StocktakeServiceGrpc.newBlockingStub(grpc.tenantChannel());
        var created = stocktakes.create(CreateStocktakeRequest.newBuilder().setStocktake(Stocktake.newBuilder()
                .setKind("01").setTargetKind("01").setStocktakeDate(Date.newBuilder().setYear(2026).setMonth(10).setDay(31))).build());
        assertThat(created.getStocktakeNo()).isEqualTo("ST00000001");
        assertThat(created.getStatus()).isEqualTo(StocktakeStatus.STOCKTAKE_STATUS_INSTRUCTED);

        var updated = stocktakes.update(UpdateStocktakeRequest.newBuilder()
                .setStocktake(created.toBuilder().setIncludeEmptyLocations(true)).build());
        assertThat(updated.getIncludeEmptyLocations()).isTrue();

        var second = stocktakes.create(CreateStocktakeRequest.newBuilder().setStocktake(Stocktake.newBuilder()
                .setKind("02").setTargetKind("01")).build());
        assertThat(second.getStocktakeNo()).isEqualTo("ST00000002");

        assertThat(stocktakes.list(ListStocktakesRequest.newBuilder()
                .addStatuses(StocktakeStatus.STOCKTAKE_STATUS_INSTRUCTED).build()).getStocktakesList()).hasSize(2);

        var cancelled = stocktakes.cancel(CancelStocktakeRequest.newBuilder()
                .setStocktakeNo(created.getStocktakeNo()).setVersion(updated.getAudit().getVersion()).build());
        assertThat(cancelled.getStatus()).isEqualTo(StocktakeStatus.STOCKTAKE_STATUS_CANCELLED);
        assertThat(stocktakes.get(GetStocktakeRequest.newBuilder().setStocktakeNo(created.getStocktakeNo()).build()).getLinesList()).isEmpty();

        assertThatThrownBy(() -> stocktakes.update(UpdateStocktakeRequest.newBuilder().setStocktake(cancelled).build()))
                .satisfies(e -> assertThat(code(e)).isEqualTo(Status.Code.FAILED_PRECONDITION));
    }

    @Test
    void sameKeysAreTheSameLot(@Autowired LotService lots, @Autowired PlatformTransactionManager transactionManager) throws Exception {
        var tenant = new TestTenantResolver().resolve("C001", "WH01", "CUST01");
        var context = new RequestContext("tester", "C001", "WH01", "CUST01", UUID.fromString(tenant.getCompanyId()),
                UUID.fromString(tenant.getWarehouseId()), UUID.fromString(tenant.getCustomerId()), UUID.fromString(tenant.getTenantId()));
        var transaction = new TransactionTemplate(transactionManager);
        var keys = PickKeys.newBuilder().setKey1("L-9").setKey3("BLUE").build();

        var ids = context.attach(Context.current()).call(() -> transaction.execute(status -> List.of(
                lots.findOrCreate("P009", keys, "L-9", null),
                lots.findOrCreate("P009", keys, "L-9", null),
                lots.findOrCreate("P009", keys.toBuilder().setKey3("").build(), "L-9", null),   // key3 NULL: another lot
                lots.findOrCreate("P009", PickKeys.getDefaultInstance(), null, null),
                lots.findOrCreate("P009", PickKeys.getDefaultInstance(), null, null))));     // no keys at all: one lot
        assertThat(ids.get(0)).isEqualTo(ids.get(1));
        assertThat(ids.get(2)).isNotEqualTo(ids.get(0));
        assertThat(ids.get(3)).isEqualTo(ids.get(4)).isNotIn(ids.get(0), ids.get(2));
    }

    private static Status.Code code(Throwable e) {
        return ((StatusRuntimeException) e).getStatus().getCode();
    }
}

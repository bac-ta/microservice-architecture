package com.example.epl.inventory;

import com.example.epl.common.v1.Date;
import com.example.epl.common.v1.PickKeys;
import com.example.epl.inventory.v1.CancelStocktakeRequest;
import com.example.epl.inventory.v1.CreateStocktakeRequest;
import com.example.epl.inventory.v1.GetStockRequest;
import com.example.epl.inventory.v1.GetStocktakeRequest;
import com.example.epl.inventory.v1.ListStocktakesRequest;
import com.example.epl.inventory.v1.SearchStockRequest;
import com.example.epl.inventory.v1.StockServiceGrpc;
import com.example.epl.inventory.v1.Stocktake;
import com.example.epl.inventory.v1.StocktakeServiceGrpc;
import com.example.epl.inventory.v1.StocktakeStatus;
import com.example.epl.inventory.v1.UpdateStocktakeRequest;
import com.example.service.core.test.EplServiceTestConfiguration;
import com.example.service.core.test.EplTestDatabase;
import com.example.service.core.test.GrpcTestServer;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Testcontainers;

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
                INSERT INTO inv.stock (id, tenant_id, warehouse_id, receipt_no, receipt_line, receipt_seq, stock_kind,
                                       received_on, product_code, pick_key1, location_code, area_code, sub_inventory_code,
                                       physical_qty, available_qty, allocated_qty, created_by, updated_by)
                SELECT gen_random_uuid(), t.id, t.warehouse_id, 'AR00000001', l, 1, '1', current_date, 'P001',
                       'LOT' || l, 'A01-R1-0' || l, 'A01', 'GOOD', 10 * l, 10 * l, 0, 'test', 'test'
                FROM iam.tenant t, generate_series(1, 2) l
                WHERE t.customer_code = 'CUST01'
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

    private static Status.Code code(Throwable e) {
        return ((StatusRuntimeException) e).getStatus().getCode();
    }
}

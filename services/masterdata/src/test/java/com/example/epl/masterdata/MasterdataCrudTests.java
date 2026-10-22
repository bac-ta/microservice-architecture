package com.example.epl.masterdata;

import com.example.epl.common.v1.Decimal;
import com.example.epl.masterdata.v1.Area;
import com.example.epl.masterdata.v1.ConfigServiceGrpc;
import com.example.epl.masterdata.v1.DeleteAreaRequest;
import com.example.epl.masterdata.v1.DeleteLocationRequest;
import com.example.epl.masterdata.v1.DeleteProductRequest;
import com.example.epl.masterdata.v1.GetAreaRequest;
import com.example.epl.masterdata.v1.GetProductRequest;
import com.example.epl.masterdata.v1.ListLocationsRequest;
import com.example.epl.masterdata.v1.ListProductsRequest;
import com.example.epl.masterdata.v1.Location;
import com.example.epl.masterdata.v1.LocationServiceGrpc;
import com.example.epl.masterdata.v1.Packing;
import com.example.epl.masterdata.v1.Product;
import com.example.epl.masterdata.v1.ProductServiceGrpc;
import com.example.epl.masterdata.v1.ResolveBarcodeRequest;
import com.example.epl.masterdata.v1.SubInventory;
import com.example.service.core.test.EplServiceTestConfiguration;
import com.example.service.core.test.EplTestDatabase;
import com.example.service.core.test.GrpcTestServer;
import com.google.protobuf.Empty;
import com.google.protobuf.Struct;
import com.google.protobuf.Value;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Testcontainers(disabledWithoutDocker = true)
@Import(EplServiceTestConfiguration.class)
class MasterdataCrudTests {

    @DynamicPropertySource
    static void database(DynamicPropertyRegistry registry) {
        EplTestDatabase.register(registry, "md");
    }

    @Autowired
    private GrpcTestServer grpc;

    @Test
    void areasAndLocations() {
        var locations = LocationServiceGrpc.newBlockingStub(grpc.channel("C001", "WH01", null));
        var area = locations.upsertArea(Area.newBuilder().setCode("A01").setName("Ambient").build());
        assertThat(area.getAudit().getCreatedBy()).isEqualTo("tester");

        // second create without the version is refused, with it it updates
        assertThatThrownBy(() -> locations.upsertArea(Area.newBuilder().setCode("A01").setName("Again").build()))
                .satisfies(e -> assertThat(code(e)).isEqualTo(Status.Code.ALREADY_EXISTS));
        var renamed = locations.upsertArea(area.toBuilder().setName("Ambient zone").build());
        assertThat(renamed.getName()).isEqualTo("Ambient zone");

        var location = locations.upsertLocation(Location.newBuilder().setCode("A01-R1-01-1").setAreaCode("A01")
                .setRackCode("R1").setPositionCode("01").setLevelCode("1").setPickingSequence(10)
                .setExt(Struct.newBuilder().putFields("cit1", Value.newBuilder().setStringValue("x").build())).build());
        assertThat(location.getExt().getFieldsMap()).containsKey("cit1");
        assertThat(locations.listLocations(ListLocationsRequest.newBuilder().setAreaCode("A01").build()).getLocationsList())
                .extracting("code").containsExactly("A01-R1-01-1");

        assertThatThrownBy(() -> locations.deleteArea(DeleteAreaRequest.newBuilder().setCode("A01").setVersion(renamed.getAudit().getVersion()).build()))
                .satisfies(e -> assertThat(code(e)).isEqualTo(Status.Code.FAILED_PRECONDITION));
        locations.deleteLocation(DeleteLocationRequest.newBuilder().setCode("A01-R1-01-1").setVersion(location.getAudit().getVersion()).build());
        locations.deleteArea(DeleteAreaRequest.newBuilder().setCode("A01").setVersion(renamed.getAudit().getVersion()).build());
        assertThatThrownBy(() -> locations.getArea(GetAreaRequest.newBuilder().setCode("A01").build()))
                .satisfies(e -> assertThat(code(e)).isEqualTo(Status.Code.NOT_FOUND));

        // a deleted code can be created again
        assertThat(locations.upsertArea(Area.newBuilder().setCode("A01").setName("Reborn").build()).getName()).isEqualTo("Reborn");
    }

    @Test
    void warehouseDataNeedsTheWarehouseHeader() {
        var noWarehouse = LocationServiceGrpc.newBlockingStub(grpc.channel("C001", null, null));
        assertThatThrownBy(() -> noWarehouse.getArea(GetAreaRequest.newBuilder().setCode("A01").build()))
                .satisfies(e -> assertThat(code(e)).isEqualTo(Status.Code.FAILED_PRECONDITION));
    }

    @Test
    void productsAreIsolatedPerTenant() {
        var products = ProductServiceGrpc.newBlockingStub(grpc.tenantChannel());
        var product = products.upsertProduct(Product.newBuilder().setCode("P001").addNames("Green tea").addNames("500ml")
                .setPacking(Packing.newBuilder().setUnitsPerCase(24).setCaseWeight(Decimal.newBuilder().setValue("12.5")))
                .addBarcodes("4900000000017").build());
        assertThat(new BigDecimal(product.getPacking().getCaseWeight().getValue())).isEqualByComparingTo("12.5");
        assertThat(product.getBarcodesList()).containsExactly("4900000000017");

        assertThat(products.listProducts(ListProductsRequest.newBuilder().setQuery("TEA").build()).getProductsList())
                .extracting("code").containsExactly("P001");
        assertThat(products.resolveBarcode(ResolveBarcodeRequest.newBuilder().setBarcode("4900000000017").build())
                .getProduct().getCode()).isEqualTo("P001");

        // RLS: another customer of the same warehouse does not see it
        var other = ProductServiceGrpc.newBlockingStub(grpc.channel("C001", "WH01", "CUST02"));
        assertThatThrownBy(() -> other.getProduct(GetProductRequest.newBuilder().setCode("P001").build()))
                .satisfies(e -> assertThat(code(e)).isEqualTo(Status.Code.NOT_FOUND));
        assertThat(other.listProducts(ListProductsRequest.getDefaultInstance()).getProductsList()).isEmpty();

        products.deleteProduct(DeleteProductRequest.newBuilder().setCode("P001").setVersion(product.getAudit().getVersion()).build());
        assertThatThrownBy(() -> products.resolveBarcode(ResolveBarcodeRequest.newBuilder().setBarcode("4900000000017").build()))
                .satisfies(e -> assertThat(code(e)).isEqualTo(Status.Code.NOT_FOUND));
    }

    @Test
    void subInventoriesKeepOneDefault() {
        var config = ConfigServiceGrpc.newBlockingStub(grpc.tenantChannel());
        config.upsertSubInventory(SubInventory.newBuilder().setCode("GOOD").setName("Good").setIsDefault(true).build());
        config.upsertSubInventory(SubInventory.newBuilder().setCode("DMG").setName("Damaged").setIsDefault(true).build());
        assertThat(config.listSubInventories(Empty.getDefaultInstance()).getSubInventoriesList())
                .filteredOn(SubInventory::getIsDefault).extracting("code").containsExactly("DMG");
    }

    @Test
    void unknownTenantIsNotFound() {
        var unknown = ProductServiceGrpc.newBlockingStub(grpc.channel("C001", "WH01", "NOPE"));
        assertThatThrownBy(() -> unknown.getProduct(GetProductRequest.newBuilder().setCode("P001").build()))
                .satisfies(e -> assertThat(code(e)).isEqualTo(Status.Code.NOT_FOUND));
    }

    private static Status.Code code(Throwable e) {
        return ((StatusRuntimeException) e).getStatus().getCode();
    }
}

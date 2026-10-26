package com.epl.billing;

import com.epl.billing.v1.DeleteRateRequest;
import com.epl.billing.v1.GetRateRequest;
import com.epl.billing.v1.ListRatesRequest;
import com.epl.billing.v1.Rate;
import com.epl.billing.v1.RateServiceGrpc;
import com.epl.common.v1.Date;
import com.epl.common.v1.Decimal;
import com.epl.service.core.test.EplServiceTestConfiguration;
import com.epl.service.core.test.EplTestDatabase;
import com.epl.service.core.test.GrpcTestServer;
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
class RateCrudTests {

    @DynamicPropertySource
    static void database(DynamicPropertyRegistry registry) {
        EplTestDatabase.register(registry, "bill");
    }

    @Autowired
    private GrpcTestServer grpc;

    @Test
    void pricePeriods() {
        var rates = RateServiceGrpc.newBlockingStub(grpc.tenantChannel());
        var january = rates.upsertRate(rate("STORE-CS", "100", date(2026, 1, 1)));
        // a later period closes the open-ended one the day before
        rates.upsertRate(rate("STORE-CS", "120", date(2026, 7, 1)));

        assertThat(price(rates.getRate(GetRateRequest.newBuilder().setCode("STORE-CS").setValidOn(date(2026, 3, 15)).build())))
                .isEqualByComparingTo("100");
        assertThat(price(rates.getRate(GetRateRequest.newBuilder().setCode("STORE-CS").setValidOn(date(2026, 8, 1)).build())))
                .isEqualByComparingTo("120");
        var periods = rates.listRates(ListRatesRequest.newBuilder().setChargeType("STORAGE").build()).getRatesList();
        assertThat(periods).hasSize(2);
        assertThat(periods.get(0).getValidTo()).isEqualTo(date(2026, 6, 30));

        // updating a period needs its version
        assertThatThrownBy(() -> rates.upsertRate(rate("STORE-CS", "90", date(2026, 1, 1))))
                .satisfies(e -> assertThat(code(e)).isEqualTo(Status.Code.ALREADY_EXISTS));

        // an overlapping closed period is refused
        assertThatThrownBy(() -> rates.upsertRate(rate("STORE-CS", "90", date(2026, 3, 1)).toBuilder()
                .setValidTo(date(2026, 3, 31)).build()))
                .satisfies(e -> assertThat(code(e)).isEqualTo(Status.Code.FAILED_PRECONDITION));

        rates.deleteRate(DeleteRateRequest.newBuilder().setCode("STORE-CS").build());
        assertThat(rates.listRates(ListRatesRequest.getDefaultInstance()).getRatesList()).isEmpty();
        assertThat(january.getAudit().getCreatedBy()).isEqualTo("tester");
    }

    @Test
    void invalidChargeType() {
        var rates = RateServiceGrpc.newBlockingStub(grpc.tenantChannel());
        assertThatThrownBy(() -> rates.upsertRate(rate("X", "1", date(2026, 1, 1)).toBuilder().setChargeType("FREE").build()))
                .satisfies(e -> assertThat(code(e)).isEqualTo(Status.Code.INVALID_ARGUMENT));
    }

    private static Rate rate(String code, String price, Date validFrom) {
        return Rate.newBuilder().setCode(code).setChargeType("STORAGE").setName("Storage per case").setUnit("CS")
                .setUnitPrice(Decimal.newBuilder().setValue(price)).setCurrency("JPY").setValidFrom(validFrom).build();
    }

    private static BigDecimal price(Rate rate) {
        return new BigDecimal(rate.getUnitPrice().getValue());
    }

    private static Date date(int year, int month, int day) {
        return Date.newBuilder().setYear(year).setMonth(month).setDay(day).build();
    }

    private static Status.Code code(Throwable e) {
        return ((StatusRuntimeException) e).getStatus().getCode();
    }
}

package com.epl.billing.services;

import com.epl.billing.entities.RateEntity;
import com.epl.billing.mappers.RateMappers;
import com.epl.billing.repositories.RateRepository;
import com.epl.billing.v1.Rate;
import com.epl.common.v1.Date;
import com.epl.service.core.Errors;
import com.epl.service.core.RequestContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static com.epl.service.core.Protos.bigDecimal;
import static com.epl.service.core.Protos.localDate;

/**
 * Charge rates of the tenant. A rate code has price periods that never overlap: adding a period
 * after an open-ended one closes it the day before.
 */
@RequiredArgsConstructor
@Service
public class RateService {

    private static final Set<String> CHARGE_TYPES = Set.of("IN", "OUT", "STORAGE", "PROCESSING", "OTHER");

    private final RateRepository rateRepository;

    @Transactional(readOnly = true)
    /** validOn unset = every price period */
    public List<Rate> list(String chargeType, Date validOn) {
        var tenantId = RequestContext.current().requireTenant();
        var day = localDate(validOn);
        return rateRepository.search(tenantId, chargeType, day == null, day == null ? LocalDate.now() : day)
                .stream().map(RateMappers::rate).toList();
    }

    @Transactional(readOnly = true)
    public Rate get(String code, Date validOn) {
        var tenantId = RequestContext.current().requireTenant();
        var day = Optional.ofNullable(localDate(validOn)).orElse(LocalDate.now());
        return rateRepository.findAllByTenantIdAndCodeAndDeletedAtIsNullOrderByValidFrom(tenantId, Errors.required(code, "code"))
                .stream().filter(rate -> rate.overlaps(day, day)).findFirst()
                .map(RateMappers::rate)
                .orElseThrow(() -> Errors.notFound("Rate", code + " on " + day));
    }

    @Transactional
    public Rate upsert(Rate request) {
        var tenantId = RequestContext.current().requireTenant();
        var code = Errors.required(request.getCode(), "code");
        var validFrom = localDate(request.getValidFrom());
        if (validFrom == null) {
            throw Errors.invalid("valid_from is required");
        }
        var validTo = localDate(request.getValidTo());
        if (validTo != null && validTo.isBefore(validFrom)) {
            throw Errors.invalid("valid_to is before valid_from");
        }
        var chargeType = Errors.required(request.getChargeType(), "charge_type");
        if (!CHARGE_TYPES.contains(chargeType)) {
            throw Errors.invalid("charge_type must be one of " + CHARGE_TYPES);
        }
        var unitPrice = bigDecimal(request.getUnitPrice());
        if (unitPrice == null || unitPrice.signum() < 0) {
            throw Errors.invalid("unit_price must be >= 0");
        }

        var entity = rateRepository.findByTenantIdAndCodeAndValidFrom(tenantId, code, validFrom).orElse(null);
        var live = entity != null && entity.getDeletedAt() == null;
        Errors.checkUpsert("Rate", code + " from " + validFrom, live, request.hasAudit(),
                live ? entity.getVersion() : 0, request.getAudit().getVersion());
        if (entity == null) {
            entity = new RateEntity();
            entity.setTenantId(tenantId);
            entity.setCode(code);
            entity.setValidFrom(validFrom);
        }
        entity.setDeletedAt(null);
        entity.setChargeType(chargeType);
        entity.setName(Errors.required(request.getName(), "name"));
        entity.setUnit(Errors.required(request.getUnit(), "unit"));
        entity.setUnitPrice(unitPrice);
        entity.setCurrency(Errors.required(request.getCurrency(), "currency"));
        entity.setValidTo(validTo);

        for (var other : rateRepository.findAllByTenantIdAndCodeAndDeletedAtIsNullOrderByValidFrom(tenantId, code)) {
            if (other == entity) {
                continue;
            }
            if (other.getValidTo() == null && other.getValidFrom().isBefore(validFrom)) {
                other.setValidTo(validFrom.minusDays(1));
            }
            if (other.overlaps(validFrom, validTo)) {
                throw Errors.failedPrecondition("Rate " + code + " already has a price from " + other.getValidFrom()
                        + (other.getValidTo() == null ? "" : " to " + other.getValidTo()));
            }
        }
        return RateMappers.rate(rateRepository.saveAndFlush(entity));
    }

    @Transactional
    public void delete(String code) {
        var tenantId = RequestContext.current().requireTenant();
        var periods = rateRepository.findAllByTenantIdAndCodeAndDeletedAtIsNullOrderByValidFrom(tenantId, Errors.required(code, "code"));
        if (periods.isEmpty()) {
            throw Errors.notFound("Rate", code);
        }
        var now = Instant.now();
        periods.forEach(rate -> rate.setDeletedAt(now));
    }
}

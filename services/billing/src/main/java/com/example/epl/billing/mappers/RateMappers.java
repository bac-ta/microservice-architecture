package com.example.epl.billing.mappers;

import com.example.epl.billing.entities.RateEntity;
import com.example.epl.billing.v1.Rate;

import static com.example.service.core.Protos.audit;
import static com.example.service.core.Protos.date;
import static com.example.service.core.Protos.decimal;

public final class RateMappers {

    private RateMappers() {
    }

    public static Rate rate(RateEntity entity) {
        return Rate.newBuilder()
                .setCode(entity.getCode())
                .setChargeType(entity.getChargeType())
                .setName(entity.getName())
                .setUnit(entity.getUnit())
                .setUnitPrice(decimal(entity.getUnitPrice()))
                .setCurrency(entity.getCurrency())
                .setValidFrom(date(entity.getValidFrom()))
                .setValidTo(date(entity.getValidTo()))
                .setAudit(audit(entity))
                .build();
    }
}

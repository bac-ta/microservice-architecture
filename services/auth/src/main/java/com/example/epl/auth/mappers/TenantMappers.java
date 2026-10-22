package com.example.epl.auth.mappers;

import com.example.epl.auth.entities.CompanyEntity;
import com.example.epl.auth.entities.CustomerEntity;
import com.example.epl.auth.entities.TenantEntity;
import com.example.epl.auth.entities.WarehouseEntity;
import com.example.epl.auth.v1.Company;
import com.example.epl.auth.v1.Customer;
import com.example.epl.auth.v1.Tenant;
import com.example.epl.auth.v1.Warehouse;

import static com.example.service.core.Protos.audit;
import static com.example.service.core.Protos.date;
import static com.example.service.core.Protos.emptyIfNull;
import static com.example.service.core.Protos.text;

/** Tenant registry entities -> proto messages. */
public final class TenantMappers {

    private TenantMappers() {
    }

    public static Company company(CompanyEntity entity) {
        return Company.newBuilder()
                .setId(text(entity.getId()))
                .setCode(entity.getCode())
                .setName(entity.getName())
                .setCountryCode(emptyIfNull(entity.getCountryCode()))
                .setTimeZone(entity.getTimeZone())
                .setStatus(entity.getStatus())
                .setAudit(audit(entity))
                .build();
    }

    public static Warehouse warehouse(WarehouseEntity entity) {
        return Warehouse.newBuilder()
                .setId(text(entity.getId()))
                .setCompanyCode(entity.getCompany().getCode())
                .setCode(entity.getCode())
                .setName(entity.getName())
                .setTimeZone(entity.getTimeZone())
                .setStatus(entity.getStatus())
                .setStartDate(date(entity.getStartDate()))
                .setEndDate(date(entity.getEndDate()))
                .setAudit(audit(entity))
                .build();
    }

    public static Customer customer(CustomerEntity entity) {
        return Customer.newBuilder()
                .setId(text(entity.getId()))
                .setCompanyCode(entity.getCompany().getCode())
                .setCode(entity.getCode())
                .setName(entity.getName())
                .setStatus(entity.getStatus())
                .setAudit(audit(entity))
                .build();
    }

    public static Tenant tenant(TenantEntity entity) {
        return Tenant.newBuilder()
                .setId(text(entity.getId()))
                .setCompanyCode(entity.getCompanyCode())
                .setWarehouseCode(entity.getWarehouseCode())
                .setCustomerCode(entity.getCustomerCode())
                .setCustomerName(entity.getCustomer().getName())
                .setStatus(entity.getStatus())
                .setStartDate(date(entity.getStartDate()))
                .setEndDate(date(entity.getEndDate()))
                .setAudit(audit(entity))
                .build();
    }
}

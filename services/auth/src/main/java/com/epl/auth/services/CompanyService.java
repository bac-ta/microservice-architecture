package com.epl.auth.services;

import com.epl.auth.entities.CompanyEntity;
import com.epl.auth.mappers.TenantMappers;
import com.epl.auth.repositories.CompanyRepository;
import com.epl.auth.v1.Company;
import com.epl.service.core.Errors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.epl.service.core.Protos.nullIfEmpty;

@Service
public class CompanyService {

    private final CompanyRepository companyRepository;

    public CompanyService(CompanyRepository companyRepository) {
        this.companyRepository = companyRepository;
    }

    @Transactional(readOnly = true)
    public List<Company> list() {
        return companyRepository.findAllByOrderByCode().stream().map(TenantMappers::company).toList();
    }

    @Transactional(readOnly = true)
    public Company get(String code) {
        return TenantMappers.company(find(code));
    }

    @Transactional
    public Company create(Company request) {
        var entity = new CompanyEntity();
        entity.setCode(Errors.required(request.getCode(), "code"));
        apply(entity, request);
        return TenantMappers.company(companyRepository.saveAndFlush(entity));
    }

    @Transactional
    public Company update(Company request) {
        var entity = find(request.getCode());
        Errors.checkVersion(entity.getVersion(), request.getAudit().getVersion());
        apply(entity, request);
        return TenantMappers.company(companyRepository.saveAndFlush(entity));
    }

    @Transactional
    public void delete(String code, int version) {
        var entity = find(code);
        Errors.checkVersion(entity.getVersion(), version);
        companyRepository.delete(entity);
        companyRepository.flush();
    }

    CompanyEntity find(String code) {
        return companyRepository.findByCode(Errors.required(code, "company_code"))
                .orElseThrow(() -> Errors.notFound("Company", code));
    }

    private static void apply(CompanyEntity entity, Company request) {
        entity.setName(Errors.required(request.getName(), "name"));
        entity.setCountryCode(nullIfEmpty(request.getCountryCode()));
        entity.setTimeZone(request.getTimeZone().isBlank() ? "UTC" : request.getTimeZone());
        entity.setStatus(request.getStatus().isBlank() ? "ACTIVE" : request.getStatus());
    }
}

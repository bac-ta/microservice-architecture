package com.example.epl.auth.services;

import com.example.epl.auth.entities.CustomerEntity;
import com.example.epl.auth.mappers.TenantMappers;
import com.example.epl.auth.repositories.CustomerRepository;
import com.example.epl.auth.v1.Customer;
import com.example.service.core.Errors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final CompanyService companyService;

    public CustomerService(CustomerRepository customerRepository, CompanyService companyService) {
        this.customerRepository = customerRepository;
        this.companyService = companyService;
    }

    @Transactional(readOnly = true)
    public List<Customer> list(String companyCode) {
        return customerRepository.findAllByCompanyCodeOrderByCode(Errors.required(companyCode, "company_code"))
                .stream().map(TenantMappers::customer).toList();
    }

    @Transactional(readOnly = true)
    public Customer get(String companyCode, String code) {
        return TenantMappers.customer(find(companyCode, code));
    }

    @Transactional
    public Customer create(Customer request) {
        var entity = new CustomerEntity();
        entity.setCompany(companyService.find(request.getCompanyCode()));
        entity.setCode(Errors.required(request.getCode(), "code"));
        apply(entity, request);
        return TenantMappers.customer(customerRepository.saveAndFlush(entity));
    }

    @Transactional
    public Customer update(Customer request) {
        var entity = find(request.getCompanyCode(), request.getCode());
        Errors.checkVersion(entity.getVersion(), request.getAudit().getVersion());
        apply(entity, request);
        return TenantMappers.customer(customerRepository.saveAndFlush(entity));
    }

    @Transactional
    public void delete(String companyCode, String code, int version) {
        var entity = find(companyCode, code);
        Errors.checkVersion(entity.getVersion(), version);
        customerRepository.delete(entity);
        customerRepository.flush();
    }

    CustomerEntity find(String companyCode, String code) {
        return customerRepository.findByCompanyCodeAndCode(Errors.required(companyCode, "company_code"),
                        Errors.required(code, "customer_code"))
                .orElseThrow(() -> Errors.notFound("Customer", companyCode + "/" + code));
    }

    private static void apply(CustomerEntity entity, Customer request) {
        entity.setName(Errors.required(request.getName(), "name"));
        entity.setStatus(request.getStatus().isBlank() ? "ACTIVE" : request.getStatus());
    }
}

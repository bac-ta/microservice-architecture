package com.entropy.gateway.services;

import com.entropy.gateway.models.requests.DepartmentRequest;
import com.entropy.gateway.models.responses.DepartmentResponse;

import java.util.List;

public interface DepartmentService {
    DepartmentResponse findById(int id);

    List<DepartmentResponse> findAll();

    void removeById(int id);

    void saveDepartment(DepartmentRequest request);
}

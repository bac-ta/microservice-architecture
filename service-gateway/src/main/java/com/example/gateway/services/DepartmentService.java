package com.example.gateway.services;

import com.example.gateway.models.requests.DepartmentRequest;
import com.example.gateway.models.responses.DepartmentResponse;

import java.util.List;

public interface DepartmentService {
    DepartmentResponse findById(int id);

    List<DepartmentResponse> findAll();

    void removeById(int id);

    void saveDepartment(DepartmentRequest request);
}

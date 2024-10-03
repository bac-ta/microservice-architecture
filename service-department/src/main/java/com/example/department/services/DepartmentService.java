package com.example.department.services;


import com.example.core.autogen.grpc.department.Department;
import com.example.core.autogen.grpc.department.DepartmentIdRequest;
import com.example.core.autogen.grpc.department.DepartmentResponses;
import com.example.core.autogen.grpc.department.DepartmentSaving;

public interface DepartmentService {

    Department findById(DepartmentIdRequest request);

    DepartmentResponses findAllDepartments();

    void saveDepartment(DepartmentSaving department);

    void removeById(DepartmentIdRequest request);

}

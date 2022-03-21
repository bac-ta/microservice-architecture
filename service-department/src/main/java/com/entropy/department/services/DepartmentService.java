package com.entropy.department.services;

import com.entropy.core.autogen.grpc.department.Department;
import com.entropy.core.autogen.grpc.department.DepartmentIdRequest;
import com.entropy.core.autogen.grpc.department.DepartmentResponses;
import com.entropy.core.autogen.grpc.department.DepartmentSaving;


public interface DepartmentService {

    Department findById(DepartmentIdRequest request);

    DepartmentResponses findAllDepartments();

    void saveDepartment(DepartmentSaving department);

    void removeById(DepartmentIdRequest request);

}

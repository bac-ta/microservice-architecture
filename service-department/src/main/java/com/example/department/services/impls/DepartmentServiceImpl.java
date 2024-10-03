package com.example.department.services.impls;

import com.example.core.autogen.grpc.department.DepartmentIdRequest;
import com.example.core.autogen.grpc.department.DepartmentResponses;
import com.example.core.autogen.grpc.department.DepartmentSaving;
import com.example.department.models.entities.Department;
import com.example.department.repositories.DepartmentRepository;
import com.example.department.services.DepartmentService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DepartmentServiceImpl implements DepartmentService {
    private final DepartmentRepository departmentRepository;
    private static final Logger logger = LoggerFactory.getLogger(DepartmentService.class);

    public com.example.core.autogen.grpc.department.Department findById(DepartmentIdRequest request) {
        int id = request.getDpId();
        logger.info("DepartmentEntity Id: " + id);
        var entity = departmentRepository.findById(id);
        return entity.initDepartment(entity);
    }

    public DepartmentResponses findAllDepartments() {
        var entities = departmentRepository.findAll();
        if (entities.isEmpty())
            return DepartmentResponses.getDefaultInstance();

        var departments = entities.stream().map(entity -> entity.initDepartment(entity)).collect(Collectors.toList());

        return DepartmentResponses.newBuilder().addAllDepartment(departments).build();
    }

    public void saveDepartment(DepartmentSaving department) {
        var entity = new Department(department);
        try {
            departmentRepository.save(entity);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void removeById(DepartmentIdRequest request) {
        int id = request.getDpId();
        logger.info("Department Id: " + id);
        departmentRepository.removeById(id);
    }
}

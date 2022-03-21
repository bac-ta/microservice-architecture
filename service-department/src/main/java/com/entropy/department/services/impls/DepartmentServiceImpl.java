package com.entropy.department.services.impls;

import com.entropy.core.autogen.grpc.department.Department;
import com.entropy.core.autogen.grpc.department.DepartmentIdRequest;
import com.entropy.core.autogen.grpc.department.DepartmentResponses;
import com.entropy.core.autogen.grpc.department.DepartmentSaving;
import com.entropy.department.models.entities.DepartmentEntity;
import com.entropy.department.repositories.DepartmentRepository;
import com.entropy.department.services.DepartmentService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DepartmentServiceImpl implements DepartmentService {
    private final DepartmentRepository departmentRepository;
    private static final Logger logger = LoggerFactory.getLogger(DepartmentService.class);

    public Department findById(DepartmentIdRequest request) {
        int id = request.getDpId();
        logger.info("DepartmentEntity Id: " + id);
        DepartmentEntity entity = departmentRepository.findById(id);
        return entity.initDepartment(entity);
    }

    public DepartmentResponses findAllDepartments() {
        List<DepartmentEntity> entities = departmentRepository.findAll();
        System.out.println(entities);
        if (entities.isEmpty())
            return DepartmentResponses.getDefaultInstance();

        List<Department> departments = entities.stream().map(entity -> entity.initDepartment(entity)).collect(Collectors.toList());

        return DepartmentResponses.newBuilder().addAllDepartment(departments).build();
    }

    public void saveDepartment(DepartmentSaving department) {
        DepartmentEntity entity = new DepartmentEntity(department);
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

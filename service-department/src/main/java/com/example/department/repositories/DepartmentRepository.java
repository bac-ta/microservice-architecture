package com.example.department.repositories;

import com.example.department.models.entities.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, Integer> {
    Department findById(int id);

    Department save(Department department);

    void removeById(int departmentId);

    @Query(value = "SELECT * FROM department", nativeQuery = true)
    List<Department> findAll();
}

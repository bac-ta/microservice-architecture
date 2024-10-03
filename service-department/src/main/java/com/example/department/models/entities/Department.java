package com.example.department.models.entities;

import com.example.core.autogen.grpc.department.DepartmentSaving;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Data
@Table(name = "department")
public class Department {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private Integer id;
    @Column
    private String name;
    @Column
    private String description;

    public Department() {
        super();
    }

    public com.example.core.autogen.grpc.department.Department initDepartment(Department entity) {
        return com.example.core.autogen.grpc.department.Department.newBuilder().
                setDpId(entity.getId()).
                setDpName(entity.getName()).
                setDpDescription(entity.getDescription()).
                build();

    }

    public Department(DepartmentSaving department) {
        this.name = department.getDpName();
        this.description = department.getDpDescription();
    }
}

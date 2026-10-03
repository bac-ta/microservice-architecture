package com.example.product.models.entities;

import com.example.core.autogen.grpc.product.Gender;
import com.example.core.autogen.grpc.product.ProductSaving;
import com.example.product.models.enumerations.GenderEnum;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Data;

@Entity
@Data
@Table(name = "product",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {
                        "email"
                })
        }
)
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private Integer id;
    @Column
    private String name;
    @Column(name = "gender", length = 15)
    @Enumerated(EnumType.STRING)
    private GenderEnum genderEnum;
    @Column
    private String email;
    @Column
    private Integer age;
    @Column(name = "phone_number")
    private String phoneNumber;
    @Column(name = "department_id")
    private Integer departmentId;

    public Product() {
        super();
    }

    /**
     * Map ProductEntity to Product
     */
    public com.example.core.autogen.grpc.product.Product initProduct(Product entity) {
        Gender gender = Gender.forNumber(entity.getGenderEnum().getCode());
        return com.example.core.autogen.grpc.product.Product.newBuilder()
                .setId(entity.getId())
                .setName(entity.getName())
                .setGender(gender)
                .setEmail(entity.getEmail())
                .setAge(entity.getAge())
                .setPhoneNumber(entity.getPhoneNumber())
                .setDepartmentId(entity.getDepartmentId())
                .build();
    }

    public Product(ProductSaving product) {
        this.name = product.getName();
        Gender gender = product.getGender();
        this.genderEnum = GenderEnum.findByCode(gender.getNumber());
        this.email = product.getEmail();
        this.age = product.getAge();
        this.phoneNumber = product.getPhoneNumber();
        this.departmentId = product.getDepartmentId();
    }
}

package com.example.user.models.entities;

import com.example.core.autogen.grpc.user.Gender;
import com.example.core.autogen.grpc.user.UserSaving;
import com.example.user.models.enumerations.GenderEnum;
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
@Table(name = "user",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {
                        "email"
                })
        }
)
public class User {
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

    public User() {
        super();
    }

    /**
     * Map UserEntity to User
     */
    public com.example.core.autogen.grpc.user.User initUser(User entity) {
        Gender gender = Gender.forNumber(entity.getGenderEnum().getCode());
        return com.example.core.autogen.grpc.user.User.newBuilder()
                .setId(entity.getId())
                .setName(entity.getName())
                .setGender(gender)
                .setEmail(entity.getEmail())
                .setAge(entity.getAge())
                .setPhoneNumber(entity.getPhoneNumber())
                .setDepartmentId(entity.getDepartmentId())
                .build();
    }

    public User(UserSaving user) {
        this.name = user.getName();
        Gender gender = user.getGender();
        this.genderEnum = GenderEnum.findByCode(gender.getNumber());
        this.email = user.getEmail();
        this.age = user.getAge();
        this.phoneNumber = user.getPhoneNumber();
        this.departmentId = user.getDepartmentId();
    }
}

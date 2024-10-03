package com.example.gateway.models.requests;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;

@Data
public class UserRequest {
    @NotBlank
    private String name;
    private Integer gender;
    @NotBlank
    private String email;
    @Valid
    private Integer age;
    @NotBlank
    @JsonProperty("phone_number")
    private String phoneNumber;
    @NotBlank
    @JsonProperty("department_id")
    private Integer departmentId;
}

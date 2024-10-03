package com.example.gateway.models.requests;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;

@Data
public class DepartmentRequest {
    @NotBlank
    private String name;
    @NotBlank
    private String description;
}

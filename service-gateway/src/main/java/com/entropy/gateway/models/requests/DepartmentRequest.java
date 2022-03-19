package com.entropy.gateway.models.requests;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class DepartmentRequest {
    @NotBlank
    private String name;
    @NotBlank
    private String description;
}

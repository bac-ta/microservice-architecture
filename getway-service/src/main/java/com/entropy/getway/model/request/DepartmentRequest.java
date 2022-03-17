package com.entropy.getway.model.request;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class DepartmentRequest {
    @NotBlank
    private String name;
    @NotBlank
    private String description;
}

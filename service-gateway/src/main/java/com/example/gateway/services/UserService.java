package com.example.gateway.services;

import com.example.gateway.models.requests.UserRequest;
import com.example.gateway.models.responses.UserResponse;

import java.util.List;

public interface UserService {
    UserResponse findById(int id);

    List<UserResponse> findAll();

    List<UserResponse> findByDepartmentId(int departmentId);

    void removeUserById(int id);

    void saveUser(UserRequest request);
}

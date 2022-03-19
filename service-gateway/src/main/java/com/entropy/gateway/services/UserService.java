package com.entropy.gateway.services;

import com.entropy.gateway.models.requests.UserRequest;
import com.entropy.gateway.models.responses.UserResponse;

import java.util.List;

public interface UserService {
    UserResponse findById(int id);

    List<UserResponse> findAll();

    List<UserResponse> findByDepartmentId(int departmentId);

    void removeUserById(int id);

    void saveUser(UserRequest request);
}

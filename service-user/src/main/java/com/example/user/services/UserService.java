package com.example.user.services;


import com.example.core.autogen.grpc.user.DepartmentIdRequest;
import com.example.core.autogen.grpc.user.User;
import com.example.core.autogen.grpc.user.UserIdRequest;
import com.example.core.autogen.grpc.user.UserResponses;
import com.example.core.autogen.grpc.user.UserSaving;

public interface UserService {

    User findById(UserIdRequest request);

    UserResponses findByDepartmentId(DepartmentIdRequest request);

    UserResponses findUsers();

    void saveUser(UserSaving user);

    void removeUserById(UserIdRequest request);
}


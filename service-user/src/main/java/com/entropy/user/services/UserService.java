package com.entropy.user.services;

import com.entropy.core.autogen.grpc.user.DepartmentIdRequest;
import com.entropy.core.autogen.grpc.user.User;
import com.entropy.core.autogen.grpc.user.UserIdRequest;
import com.entropy.core.autogen.grpc.user.UserResponses;
import com.entropy.core.autogen.grpc.user.UserSaving;


public interface UserService {

    User findById(UserIdRequest request);

    UserResponses findByDepartmentId(DepartmentIdRequest request);

    UserResponses findUsers();

    void saveUser(UserSaving user);

    void removeUserById(UserIdRequest request);
}


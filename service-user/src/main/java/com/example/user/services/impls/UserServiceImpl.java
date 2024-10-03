package com.example.user.services.impls;


import com.example.core.autogen.grpc.user.DepartmentIdRequest;
import com.example.core.autogen.grpc.user.UserIdRequest;
import com.example.core.autogen.grpc.user.UserResponses;
import com.example.core.autogen.grpc.user.UserSaving;
import com.example.user.models.entities.User;
import com.example.user.repositories.UserRepository;
import com.example.user.services.UserService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;

    public com.example.core.autogen.grpc.user.User findById(UserIdRequest request) {
        int userId = request.getId();
        logger.info("User Id: " + userId);
        var entity = userRepository.findById(userId);

        return entity.initUser(entity);
    }

    public UserResponses findByDepartmentId(DepartmentIdRequest request) {
        int departmentId = request.getDepartmentId();
        logger.info("Department Id: " + departmentId);
        var entities = userRepository.findAllByDepartmentId(departmentId);
        var users = entities.stream().map(entity -> entity.initUser(entity)).collect(Collectors.toList());

        return UserResponses.newBuilder().addAllUser(users).build();
    }

    public UserResponses findUsers() {
        var entities = userRepository.findAll();
        var users = entities.stream().map(entity -> entity.initUser(entity)).collect(Collectors.toList());

        return UserResponses.newBuilder().addAllUser(users).build();
    }

    public void saveUser(UserSaving user) {
        var entity = new User(user);
        try {
            userRepository.save(entity);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void removeUserById(UserIdRequest request) {
        int id = request.getId();
        logger.info("User Id: " + id);
        userRepository.removeById(id);
    }
}

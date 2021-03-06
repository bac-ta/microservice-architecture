package com.entropy.user.services.impls;

import com.entropy.core.autogen.grpc.user.DepartmentIdRequest;
import com.entropy.core.autogen.grpc.user.User;
import com.entropy.core.autogen.grpc.user.UserIdRequest;
import com.entropy.core.autogen.grpc.user.UserResponses;
import com.entropy.core.autogen.grpc.user.UserSaving;
import com.entropy.user.models.entities.UserEntity;
import com.entropy.user.repositories.UserRepository;
import com.entropy.user.services.UserService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;

    public User findById(UserIdRequest request) {
        int userId = request.getId();
        logger.info("User Id: " + userId);
        UserEntity entity = userRepository.findById(userId);

        return entity.initUser(entity);
    }

    public UserResponses findByDepartmentId(DepartmentIdRequest request) {
        int departmentId = request.getDepartmentId();
        logger.info("Department Id: " + departmentId);
        List<UserEntity> entities = userRepository.findAllByDepartmentId(departmentId);
        List<User> users = entities.stream().map(entity -> entity.initUser(entity)).collect(Collectors.toList());

        return UserResponses.newBuilder().addAllUser(users).build();
    }

    public UserResponses findUsers() {
        List<UserEntity> entities = userRepository.findAll();
        List<User> users = entities.stream().map(entity -> entity.initUser(entity)).collect(Collectors.toList());

        return UserResponses.newBuilder().addAllUser(users).build();
    }

    public void saveUser(UserSaving user) {
        UserEntity entity = new UserEntity(user);
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

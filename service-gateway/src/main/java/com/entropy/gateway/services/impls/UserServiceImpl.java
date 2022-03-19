package com.entropy.gateway.services.impls;

import com.entropy.core.autogen.grpc.user.DepartmentIdRequest;
import com.entropy.core.autogen.grpc.user.Gender;
import com.entropy.core.autogen.grpc.user.User;
import com.entropy.core.autogen.grpc.user.UserIdRequest;
import com.entropy.core.autogen.grpc.user.UserResponses;
import com.entropy.core.autogen.grpc.user.UserSaving;
import com.entropy.core.autogen.grpc.user.UserServiceGrpc;
import com.entropy.gateway.models.requests.UserRequest;
import com.entropy.gateway.models.responses.UserResponse;
import com.entropy.gateway.services.UserService;
import com.entropy.grpc.client.models.annotations.GrpcClient;
import com.google.common.annotations.VisibleForTesting;
import com.google.protobuf.Empty;
import io.grpc.Channel;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {
    @GrpcClient(value = "user")
    private Channel channel;
    private UserServiceGrpc.UserServiceBlockingStub stub;

    @VisibleForTesting
    protected UserServiceImpl(final Channel channel) {
        this.channel = channel;
    }

    public UserServiceImpl() {
        super();
    }

    private UserServiceGrpc.UserServiceBlockingStub getStub() {
        return UserServiceGrpc.newBlockingStub(channel);
    }

    @Override
    public UserResponse findById(int id) {
        UserIdRequest request = UserIdRequest.newBuilder().setId(id).build();
        stub = getStub();
        User user = stub.findUserById(request);
        String name = user.getName();
        String gender = user.getGender().name();
        String email = user.getEmail();
        Integer age = user.getAge();
        String phoneNumber = user.getPhoneNumber();
        int departmentId = user.getDepartmentId();

        return new UserResponse(id, name, gender, email, age, phoneNumber, departmentId);
    }

    @Override
    public List<UserResponse> findAll() {
        stub = getStub();
        UserResponses responses = stub.findUsers(Empty.getDefaultInstance());

        return responses.getUserList().stream().map(resp -> {
            UserResponse response = new UserResponse();
            response.setId(resp.getId());
            response.setName(resp.getName());
            response.setEmail(resp.getEmail());
            response.setGender(resp.getGender().name());
            response.setAge(resp.getAge());
            response.setPhoneNumber(resp.getPhoneNumber());
            response.setDepartmentId(resp.getDepartmentId());

            return response;
        }).collect(Collectors.toList());
    }

    @Override
    public List<UserResponse> findByDepartmentId(int departmentId) {
        stub = getStub();
        DepartmentIdRequest request = DepartmentIdRequest.newBuilder().setDepartmentId(departmentId).build();
        UserResponses responses = stub.findUserByDepartmentId(request);

        return responses.getUserList().stream().map(resp -> {
            UserResponse response = new UserResponse();
            response.setId(resp.getId());
            response.setName(resp.getName());
            response.setEmail(resp.getEmail());
            response.setAge(resp.getAge());
            response.setPhoneNumber(resp.getPhoneNumber());
            response.setGender(resp.getGender().name());
            response.setDepartmentId(resp.getDepartmentId());

            return response;
        }).collect(Collectors.toList());
    }

    @Override
    public void removeUserById(int id) {
        stub = getStub();
        UserIdRequest request = UserIdRequest.newBuilder().setId(id).build();
        stub.removeUserById(request);
    }

    @Override
    public void saveUser(UserRequest request) {
        stub = getStub();
        String name = request.getName();
        String email = request.getEmail();
        Integer age = request.getAge();

        if (age == null) age = 0;

        else if (age < 0) throw new IllegalArgumentException("parameter 'age' invalid");

        Integer genderNum = request.getGender();
        Gender gender = Gender.forNumber(genderNum);
        String phoneNumber = request.getPhoneNumber();
        int departmentId = request.getDepartmentId();

        UserSaving user = UserSaving.newBuilder().setName(name).setEmail(email).setAge(age).setGender(gender).setPhoneNumber(phoneNumber).setDepartmentId(departmentId).build();

        stub.saveUser(user);

    }
}

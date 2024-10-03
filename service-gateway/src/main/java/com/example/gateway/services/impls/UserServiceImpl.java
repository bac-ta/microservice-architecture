package com.example.gateway.services.impls;

import com.example.core.autogen.grpc.user.DepartmentIdRequest;
import com.example.core.autogen.grpc.user.Gender;
import com.example.core.autogen.grpc.user.UserIdRequest;
import com.example.core.autogen.grpc.user.UserSaving;
import com.example.core.autogen.grpc.user.UserServiceGrpc;
import com.example.gateway.models.requests.UserRequest;
import com.example.gateway.models.responses.UserResponse;
import com.example.gateway.services.UserService;
import com.example.grpc.client.models.annotations.GrpcClient;
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
        var request = UserIdRequest.newBuilder().setId(id).build();
        stub = getStub();
        var user = stub.findUserById(request);
        var name = user.getName();
        var gender = user.getGender().name();
        var email = user.getEmail();
        var age = user.getAge();
        var phoneNumber = user.getPhoneNumber();
        int departmentId = user.getDepartmentId();

        return new UserResponse(id, name, gender, email, age, phoneNumber, departmentId);
    }

    @Override
    public List<UserResponse> findAll() {
        stub = getStub();
        var responses = stub.findUsers(Empty.getDefaultInstance());

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
        var request = DepartmentIdRequest.newBuilder().setDepartmentId(departmentId).build();
        var responses = stub.findUserByDepartmentId(request);

        return responses.getUserList().stream().map(resp -> {
            var response = new UserResponse();
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
        var request = UserIdRequest.newBuilder().setId(id).build();
        stub.removeUserById(request);
    }

    @Override
    public void saveUser(UserRequest request) {
        stub = getStub();
        var name = request.getName();
        var email = request.getEmail();
        var age = request.getAge();

        if (age == null) age = 0;

        else if (age < 0) throw new IllegalArgumentException("parameter 'age' invalid");

        var genderNum = request.getGender();
        var gender = Gender.forNumber(genderNum);
        var phoneNumber = request.getPhoneNumber();
        int departmentId = request.getDepartmentId();

        var user = UserSaving.newBuilder().setName(name).setEmail(email).setAge(age).setGender(gender).setPhoneNumber(phoneNumber).setDepartmentId(departmentId).build();

        stub.saveUser(user);

    }
}

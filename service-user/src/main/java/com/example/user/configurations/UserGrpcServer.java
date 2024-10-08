package com.example.user.configurations;
import com.example.core.autogen.grpc.user.DepartmentIdRequest;
import com.example.core.autogen.grpc.user.User;
import com.example.core.autogen.grpc.user.UserIdRequest;
import com.example.core.autogen.grpc.user.UserResponses;
import com.example.core.autogen.grpc.user.UserSaving;
import com.example.core.autogen.grpc.user.UserServiceGrpc;
import com.example.grpc.server.models.annotations.GrpcServer;
import com.example.user.services.UserService;
import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;
import org.springframework.beans.factory.annotation.Autowired;

@GrpcServer(value = UserServiceGrpc.class)
public class UserGrpcServer extends UserServiceGrpc.UserServiceImplBase {

    private final UserService service;

    @Autowired
    public UserGrpcServer(final UserService service) {
        this.service = service;
    }

    @Override
    public void findUserById(UserIdRequest request, StreamObserver<User> responseObserver) {
        responseObserver.onNext(service.findById(request));
        responseObserver.onCompleted();
    }

    @Override
    public void findUserByDepartmentId(DepartmentIdRequest request, StreamObserver<UserResponses> responseObserver) {
        UserResponses responses = service.findByDepartmentId(request);
        responseObserver.onNext(responses);
        responseObserver.onCompleted();
    }

    @Override
    public void findUsers(Empty request, StreamObserver<UserResponses> responseObserver) {
        UserResponses responses = service.findUsers();
        responseObserver.onNext(responses);
        responseObserver.onCompleted();
    }

    @Override
    public void removeUserById(UserIdRequest request, StreamObserver<Empty> responseObserver) {
        service.removeUserById(request);
        responseObserver.onNext(Empty.getDefaultInstance());
        responseObserver.onCompleted();
    }

    @Override
    public void saveUser(UserSaving request, StreamObserver<Empty> responseObserver) {
        service.saveUser(request);
        responseObserver.onNext(Empty.getDefaultInstance());
        responseObserver.onCompleted();
    }

}

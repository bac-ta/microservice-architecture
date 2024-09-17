package com.entropy.core.autogen.grpc.user;

import static io.grpc.MethodDescriptor.generateFullMethodName;

/**
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 1.64.0)",
    comments = "Source: user.proto")
@io.grpc.stub.annotations.GrpcGenerated
public final class UserServiceGrpc {

  private UserServiceGrpc() {}

  public static final java.lang.String SERVICE_NAME = "user.UserService";

  // Static method descriptors that strictly reflect the proto.
  private static volatile io.grpc.MethodDescriptor<com.google.protobuf.Empty,
      com.entropy.core.autogen.grpc.user.UserResponses> getFindUsersMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "findUsers",
      requestType = com.google.protobuf.Empty.class,
      responseType = com.entropy.core.autogen.grpc.user.UserResponses.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<com.google.protobuf.Empty,
      com.entropy.core.autogen.grpc.user.UserResponses> getFindUsersMethod() {
    io.grpc.MethodDescriptor<com.google.protobuf.Empty, com.entropy.core.autogen.grpc.user.UserResponses> getFindUsersMethod;
    if ((getFindUsersMethod = UserServiceGrpc.getFindUsersMethod) == null) {
      synchronized (UserServiceGrpc.class) {
        if ((getFindUsersMethod = UserServiceGrpc.getFindUsersMethod) == null) {
          UserServiceGrpc.getFindUsersMethod = getFindUsersMethod =
              io.grpc.MethodDescriptor.<com.google.protobuf.Empty, com.entropy.core.autogen.grpc.user.UserResponses>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "findUsers"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.google.protobuf.Empty.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.entropy.core.autogen.grpc.user.UserResponses.getDefaultInstance()))
              .setSchemaDescriptor(new UserServiceMethodDescriptorSupplier("findUsers"))
              .build();
        }
      }
    }
    return getFindUsersMethod;
  }

  private static volatile io.grpc.MethodDescriptor<com.entropy.core.autogen.grpc.user.UserIdRequest,
      com.entropy.core.autogen.grpc.user.User> getFindUserByIdMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "findUserById",
      requestType = com.entropy.core.autogen.grpc.user.UserIdRequest.class,
      responseType = com.entropy.core.autogen.grpc.user.User.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<com.entropy.core.autogen.grpc.user.UserIdRequest,
      com.entropy.core.autogen.grpc.user.User> getFindUserByIdMethod() {
    io.grpc.MethodDescriptor<com.entropy.core.autogen.grpc.user.UserIdRequest, com.entropy.core.autogen.grpc.user.User> getFindUserByIdMethod;
    if ((getFindUserByIdMethod = UserServiceGrpc.getFindUserByIdMethod) == null) {
      synchronized (UserServiceGrpc.class) {
        if ((getFindUserByIdMethod = UserServiceGrpc.getFindUserByIdMethod) == null) {
          UserServiceGrpc.getFindUserByIdMethod = getFindUserByIdMethod =
              io.grpc.MethodDescriptor.<com.entropy.core.autogen.grpc.user.UserIdRequest, com.entropy.core.autogen.grpc.user.User>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "findUserById"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.entropy.core.autogen.grpc.user.UserIdRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.entropy.core.autogen.grpc.user.User.getDefaultInstance()))
              .setSchemaDescriptor(new UserServiceMethodDescriptorSupplier("findUserById"))
              .build();
        }
      }
    }
    return getFindUserByIdMethod;
  }

  private static volatile io.grpc.MethodDescriptor<com.entropy.core.autogen.grpc.user.DepartmentIdRequest,
      com.entropy.core.autogen.grpc.user.UserResponses> getFindUserByDepartmentIdMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "findUserByDepartmentId",
      requestType = com.entropy.core.autogen.grpc.user.DepartmentIdRequest.class,
      responseType = com.entropy.core.autogen.grpc.user.UserResponses.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<com.entropy.core.autogen.grpc.user.DepartmentIdRequest,
      com.entropy.core.autogen.grpc.user.UserResponses> getFindUserByDepartmentIdMethod() {
    io.grpc.MethodDescriptor<com.entropy.core.autogen.grpc.user.DepartmentIdRequest, com.entropy.core.autogen.grpc.user.UserResponses> getFindUserByDepartmentIdMethod;
    if ((getFindUserByDepartmentIdMethod = UserServiceGrpc.getFindUserByDepartmentIdMethod) == null) {
      synchronized (UserServiceGrpc.class) {
        if ((getFindUserByDepartmentIdMethod = UserServiceGrpc.getFindUserByDepartmentIdMethod) == null) {
          UserServiceGrpc.getFindUserByDepartmentIdMethod = getFindUserByDepartmentIdMethod =
              io.grpc.MethodDescriptor.<com.entropy.core.autogen.grpc.user.DepartmentIdRequest, com.entropy.core.autogen.grpc.user.UserResponses>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "findUserByDepartmentId"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.entropy.core.autogen.grpc.user.DepartmentIdRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.entropy.core.autogen.grpc.user.UserResponses.getDefaultInstance()))
              .setSchemaDescriptor(new UserServiceMethodDescriptorSupplier("findUserByDepartmentId"))
              .build();
        }
      }
    }
    return getFindUserByDepartmentIdMethod;
  }

  private static volatile io.grpc.MethodDescriptor<com.entropy.core.autogen.grpc.user.UserSaving,
      com.google.protobuf.Empty> getSaveUserMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "saveUser",
      requestType = com.entropy.core.autogen.grpc.user.UserSaving.class,
      responseType = com.google.protobuf.Empty.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<com.entropy.core.autogen.grpc.user.UserSaving,
      com.google.protobuf.Empty> getSaveUserMethod() {
    io.grpc.MethodDescriptor<com.entropy.core.autogen.grpc.user.UserSaving, com.google.protobuf.Empty> getSaveUserMethod;
    if ((getSaveUserMethod = UserServiceGrpc.getSaveUserMethod) == null) {
      synchronized (UserServiceGrpc.class) {
        if ((getSaveUserMethod = UserServiceGrpc.getSaveUserMethod) == null) {
          UserServiceGrpc.getSaveUserMethod = getSaveUserMethod =
              io.grpc.MethodDescriptor.<com.entropy.core.autogen.grpc.user.UserSaving, com.google.protobuf.Empty>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "saveUser"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.entropy.core.autogen.grpc.user.UserSaving.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.google.protobuf.Empty.getDefaultInstance()))
              .setSchemaDescriptor(new UserServiceMethodDescriptorSupplier("saveUser"))
              .build();
        }
      }
    }
    return getSaveUserMethod;
  }

  private static volatile io.grpc.MethodDescriptor<com.entropy.core.autogen.grpc.user.UserIdRequest,
      com.google.protobuf.Empty> getRemoveUserByIdMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "removeUserById",
      requestType = com.entropy.core.autogen.grpc.user.UserIdRequest.class,
      responseType = com.google.protobuf.Empty.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<com.entropy.core.autogen.grpc.user.UserIdRequest,
      com.google.protobuf.Empty> getRemoveUserByIdMethod() {
    io.grpc.MethodDescriptor<com.entropy.core.autogen.grpc.user.UserIdRequest, com.google.protobuf.Empty> getRemoveUserByIdMethod;
    if ((getRemoveUserByIdMethod = UserServiceGrpc.getRemoveUserByIdMethod) == null) {
      synchronized (UserServiceGrpc.class) {
        if ((getRemoveUserByIdMethod = UserServiceGrpc.getRemoveUserByIdMethod) == null) {
          UserServiceGrpc.getRemoveUserByIdMethod = getRemoveUserByIdMethod =
              io.grpc.MethodDescriptor.<com.entropy.core.autogen.grpc.user.UserIdRequest, com.google.protobuf.Empty>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "removeUserById"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.entropy.core.autogen.grpc.user.UserIdRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.google.protobuf.Empty.getDefaultInstance()))
              .setSchemaDescriptor(new UserServiceMethodDescriptorSupplier("removeUserById"))
              .build();
        }
      }
    }
    return getRemoveUserByIdMethod;
  }

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static UserServiceStub newStub(io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<UserServiceStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<UserServiceStub>() {
        @java.lang.Override
        public UserServiceStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new UserServiceStub(channel, callOptions);
        }
      };
    return UserServiceStub.newStub(factory, channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static UserServiceBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<UserServiceBlockingStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<UserServiceBlockingStub>() {
        @java.lang.Override
        public UserServiceBlockingStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new UserServiceBlockingStub(channel, callOptions);
        }
      };
    return UserServiceBlockingStub.newStub(factory, channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static UserServiceFutureStub newFutureStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<UserServiceFutureStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<UserServiceFutureStub>() {
        @java.lang.Override
        public UserServiceFutureStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new UserServiceFutureStub(channel, callOptions);
        }
      };
    return UserServiceFutureStub.newStub(factory, channel);
  }

  /**
   */
  public interface AsyncService {

    /**
     */
    default void findUsers(com.google.protobuf.Empty request,
        io.grpc.stub.StreamObserver<com.entropy.core.autogen.grpc.user.UserResponses> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getFindUsersMethod(), responseObserver);
    }

    /**
     */
    default void findUserById(com.entropy.core.autogen.grpc.user.UserIdRequest request,
        io.grpc.stub.StreamObserver<com.entropy.core.autogen.grpc.user.User> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getFindUserByIdMethod(), responseObserver);
    }

    /**
     */
    default void findUserByDepartmentId(com.entropy.core.autogen.grpc.user.DepartmentIdRequest request,
        io.grpc.stub.StreamObserver<com.entropy.core.autogen.grpc.user.UserResponses> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getFindUserByDepartmentIdMethod(), responseObserver);
    }

    /**
     */
    default void saveUser(com.entropy.core.autogen.grpc.user.UserSaving request,
        io.grpc.stub.StreamObserver<com.google.protobuf.Empty> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getSaveUserMethod(), responseObserver);
    }

    /**
     */
    default void removeUserById(com.entropy.core.autogen.grpc.user.UserIdRequest request,
        io.grpc.stub.StreamObserver<com.google.protobuf.Empty> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getRemoveUserByIdMethod(), responseObserver);
    }
  }

  /**
   * Base class for the server implementation of the service UserService.
   */
  public static abstract class UserServiceImplBase
      implements io.grpc.BindableService, AsyncService {

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return UserServiceGrpc.bindService(this);
    }
  }

  /**
   * A stub to allow clients to do asynchronous rpc calls to service UserService.
   */
  public static final class UserServiceStub
      extends io.grpc.stub.AbstractAsyncStub<UserServiceStub> {
    private UserServiceStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected UserServiceStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new UserServiceStub(channel, callOptions);
    }

    /**
     */
    public void findUsers(com.google.protobuf.Empty request,
        io.grpc.stub.StreamObserver<com.entropy.core.autogen.grpc.user.UserResponses> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getFindUsersMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void findUserById(com.entropy.core.autogen.grpc.user.UserIdRequest request,
        io.grpc.stub.StreamObserver<com.entropy.core.autogen.grpc.user.User> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getFindUserByIdMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void findUserByDepartmentId(com.entropy.core.autogen.grpc.user.DepartmentIdRequest request,
        io.grpc.stub.StreamObserver<com.entropy.core.autogen.grpc.user.UserResponses> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getFindUserByDepartmentIdMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void saveUser(com.entropy.core.autogen.grpc.user.UserSaving request,
        io.grpc.stub.StreamObserver<com.google.protobuf.Empty> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getSaveUserMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void removeUserById(com.entropy.core.autogen.grpc.user.UserIdRequest request,
        io.grpc.stub.StreamObserver<com.google.protobuf.Empty> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getRemoveUserByIdMethod(), getCallOptions()), request, responseObserver);
    }
  }

  /**
   * A stub to allow clients to do synchronous rpc calls to service UserService.
   */
  public static final class UserServiceBlockingStub
      extends io.grpc.stub.AbstractBlockingStub<UserServiceBlockingStub> {
    private UserServiceBlockingStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected UserServiceBlockingStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new UserServiceBlockingStub(channel, callOptions);
    }

    /**
     */
    public com.entropy.core.autogen.grpc.user.UserResponses findUsers(com.google.protobuf.Empty request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getFindUsersMethod(), getCallOptions(), request);
    }

    /**
     */
    public com.entropy.core.autogen.grpc.user.User findUserById(com.entropy.core.autogen.grpc.user.UserIdRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getFindUserByIdMethod(), getCallOptions(), request);
    }

    /**
     */
    public com.entropy.core.autogen.grpc.user.UserResponses findUserByDepartmentId(com.entropy.core.autogen.grpc.user.DepartmentIdRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getFindUserByDepartmentIdMethod(), getCallOptions(), request);
    }

    /**
     */
    public com.google.protobuf.Empty saveUser(com.entropy.core.autogen.grpc.user.UserSaving request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getSaveUserMethod(), getCallOptions(), request);
    }

    /**
     */
    public com.google.protobuf.Empty removeUserById(com.entropy.core.autogen.grpc.user.UserIdRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getRemoveUserByIdMethod(), getCallOptions(), request);
    }
  }

  /**
   * A stub to allow clients to do ListenableFuture-style rpc calls to service UserService.
   */
  public static final class UserServiceFutureStub
      extends io.grpc.stub.AbstractFutureStub<UserServiceFutureStub> {
    private UserServiceFutureStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected UserServiceFutureStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new UserServiceFutureStub(channel, callOptions);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<com.entropy.core.autogen.grpc.user.UserResponses> findUsers(
        com.google.protobuf.Empty request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getFindUsersMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<com.entropy.core.autogen.grpc.user.User> findUserById(
        com.entropy.core.autogen.grpc.user.UserIdRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getFindUserByIdMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<com.entropy.core.autogen.grpc.user.UserResponses> findUserByDepartmentId(
        com.entropy.core.autogen.grpc.user.DepartmentIdRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getFindUserByDepartmentIdMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<com.google.protobuf.Empty> saveUser(
        com.entropy.core.autogen.grpc.user.UserSaving request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getSaveUserMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<com.google.protobuf.Empty> removeUserById(
        com.entropy.core.autogen.grpc.user.UserIdRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getRemoveUserByIdMethod(), getCallOptions()), request);
    }
  }

  private static final int METHODID_FIND_USERS = 0;
  private static final int METHODID_FIND_USER_BY_ID = 1;
  private static final int METHODID_FIND_USER_BY_DEPARTMENT_ID = 2;
  private static final int METHODID_SAVE_USER = 3;
  private static final int METHODID_REMOVE_USER_BY_ID = 4;

  private static final class MethodHandlers<Req, Resp> implements
      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final AsyncService serviceImpl;
    private final int methodId;

    MethodHandlers(AsyncService serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_FIND_USERS:
          serviceImpl.findUsers((com.google.protobuf.Empty) request,
              (io.grpc.stub.StreamObserver<com.entropy.core.autogen.grpc.user.UserResponses>) responseObserver);
          break;
        case METHODID_FIND_USER_BY_ID:
          serviceImpl.findUserById((com.entropy.core.autogen.grpc.user.UserIdRequest) request,
              (io.grpc.stub.StreamObserver<com.entropy.core.autogen.grpc.user.User>) responseObserver);
          break;
        case METHODID_FIND_USER_BY_DEPARTMENT_ID:
          serviceImpl.findUserByDepartmentId((com.entropy.core.autogen.grpc.user.DepartmentIdRequest) request,
              (io.grpc.stub.StreamObserver<com.entropy.core.autogen.grpc.user.UserResponses>) responseObserver);
          break;
        case METHODID_SAVE_USER:
          serviceImpl.saveUser((com.entropy.core.autogen.grpc.user.UserSaving) request,
              (io.grpc.stub.StreamObserver<com.google.protobuf.Empty>) responseObserver);
          break;
        case METHODID_REMOVE_USER_BY_ID:
          serviceImpl.removeUserById((com.entropy.core.autogen.grpc.user.UserIdRequest) request,
              (io.grpc.stub.StreamObserver<com.google.protobuf.Empty>) responseObserver);
          break;
        default:
          throw new AssertionError();
      }
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public io.grpc.stub.StreamObserver<Req> invoke(
        io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        default:
          throw new AssertionError();
      }
    }
  }

  public static final io.grpc.ServerServiceDefinition bindService(AsyncService service) {
    return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
        .addMethod(
          getFindUsersMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              com.google.protobuf.Empty,
              com.entropy.core.autogen.grpc.user.UserResponses>(
                service, METHODID_FIND_USERS)))
        .addMethod(
          getFindUserByIdMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              com.entropy.core.autogen.grpc.user.UserIdRequest,
              com.entropy.core.autogen.grpc.user.User>(
                service, METHODID_FIND_USER_BY_ID)))
        .addMethod(
          getFindUserByDepartmentIdMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              com.entropy.core.autogen.grpc.user.DepartmentIdRequest,
              com.entropy.core.autogen.grpc.user.UserResponses>(
                service, METHODID_FIND_USER_BY_DEPARTMENT_ID)))
        .addMethod(
          getSaveUserMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              com.entropy.core.autogen.grpc.user.UserSaving,
              com.google.protobuf.Empty>(
                service, METHODID_SAVE_USER)))
        .addMethod(
          getRemoveUserByIdMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              com.entropy.core.autogen.grpc.user.UserIdRequest,
              com.google.protobuf.Empty>(
                service, METHODID_REMOVE_USER_BY_ID)))
        .build();
  }

  private static abstract class UserServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    UserServiceBaseDescriptorSupplier() {}

    @java.lang.Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return com.entropy.core.autogen.grpc.user.UserOuterClass.getDescriptor();
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
      return getFileDescriptor().findServiceByName("UserService");
    }
  }

  private static final class UserServiceFileDescriptorSupplier
      extends UserServiceBaseDescriptorSupplier {
    UserServiceFileDescriptorSupplier() {}
  }

  private static final class UserServiceMethodDescriptorSupplier
      extends UserServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
    private final java.lang.String methodName;

    UserServiceMethodDescriptorSupplier(java.lang.String methodName) {
      this.methodName = methodName;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.MethodDescriptor getMethodDescriptor() {
      return getServiceDescriptor().findMethodByName(methodName);
    }
  }

  private static volatile io.grpc.ServiceDescriptor serviceDescriptor;

  public static io.grpc.ServiceDescriptor getServiceDescriptor() {
    io.grpc.ServiceDescriptor result = serviceDescriptor;
    if (result == null) {
      synchronized (UserServiceGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new UserServiceFileDescriptorSupplier())
              .addMethod(getFindUsersMethod())
              .addMethod(getFindUserByIdMethod())
              .addMethod(getFindUserByDepartmentIdMethod())
              .addMethod(getSaveUserMethod())
              .addMethod(getRemoveUserByIdMethod())
              .build();
        }
      }
    }
    return result;
  }
}

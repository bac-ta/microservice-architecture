package com.entropy.core.autogen.grpc.department;

import static io.grpc.MethodDescriptor.generateFullMethodName;

/**
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 1.64.0)",
    comments = "Source: department.proto")
@io.grpc.stub.annotations.GrpcGenerated
public final class DepartmentServiceGrpc {

  private DepartmentServiceGrpc() {}

  public static final java.lang.String SERVICE_NAME = "department.DepartmentService";

  // Static method descriptors that strictly reflect the proto.
  private static volatile io.grpc.MethodDescriptor<com.google.protobuf.Empty,
      com.entropy.core.autogen.grpc.department.DepartmentResponses> getFindDepartmentsMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "findDepartments",
      requestType = com.google.protobuf.Empty.class,
      responseType = com.entropy.core.autogen.grpc.department.DepartmentResponses.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<com.google.protobuf.Empty,
      com.entropy.core.autogen.grpc.department.DepartmentResponses> getFindDepartmentsMethod() {
    io.grpc.MethodDescriptor<com.google.protobuf.Empty, com.entropy.core.autogen.grpc.department.DepartmentResponses> getFindDepartmentsMethod;
    if ((getFindDepartmentsMethod = DepartmentServiceGrpc.getFindDepartmentsMethod) == null) {
      synchronized (DepartmentServiceGrpc.class) {
        if ((getFindDepartmentsMethod = DepartmentServiceGrpc.getFindDepartmentsMethod) == null) {
          DepartmentServiceGrpc.getFindDepartmentsMethod = getFindDepartmentsMethod =
              io.grpc.MethodDescriptor.<com.google.protobuf.Empty, com.entropy.core.autogen.grpc.department.DepartmentResponses>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "findDepartments"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.google.protobuf.Empty.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.entropy.core.autogen.grpc.department.DepartmentResponses.getDefaultInstance()))
              .setSchemaDescriptor(new DepartmentServiceMethodDescriptorSupplier("findDepartments"))
              .build();
        }
      }
    }
    return getFindDepartmentsMethod;
  }

  private static volatile io.grpc.MethodDescriptor<com.entropy.core.autogen.grpc.department.DepartmentIdRequest,
      com.entropy.core.autogen.grpc.department.Department> getFindDepartmentByIdMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "findDepartmentById",
      requestType = com.entropy.core.autogen.grpc.department.DepartmentIdRequest.class,
      responseType = com.entropy.core.autogen.grpc.department.Department.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<com.entropy.core.autogen.grpc.department.DepartmentIdRequest,
      com.entropy.core.autogen.grpc.department.Department> getFindDepartmentByIdMethod() {
    io.grpc.MethodDescriptor<com.entropy.core.autogen.grpc.department.DepartmentIdRequest, com.entropy.core.autogen.grpc.department.Department> getFindDepartmentByIdMethod;
    if ((getFindDepartmentByIdMethod = DepartmentServiceGrpc.getFindDepartmentByIdMethod) == null) {
      synchronized (DepartmentServiceGrpc.class) {
        if ((getFindDepartmentByIdMethod = DepartmentServiceGrpc.getFindDepartmentByIdMethod) == null) {
          DepartmentServiceGrpc.getFindDepartmentByIdMethod = getFindDepartmentByIdMethod =
              io.grpc.MethodDescriptor.<com.entropy.core.autogen.grpc.department.DepartmentIdRequest, com.entropy.core.autogen.grpc.department.Department>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "findDepartmentById"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.entropy.core.autogen.grpc.department.DepartmentIdRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.entropy.core.autogen.grpc.department.Department.getDefaultInstance()))
              .setSchemaDescriptor(new DepartmentServiceMethodDescriptorSupplier("findDepartmentById"))
              .build();
        }
      }
    }
    return getFindDepartmentByIdMethod;
  }

  private static volatile io.grpc.MethodDescriptor<com.entropy.core.autogen.grpc.department.DepartmentSaving,
      com.google.protobuf.Empty> getSaveDepartmentMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "saveDepartment",
      requestType = com.entropy.core.autogen.grpc.department.DepartmentSaving.class,
      responseType = com.google.protobuf.Empty.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<com.entropy.core.autogen.grpc.department.DepartmentSaving,
      com.google.protobuf.Empty> getSaveDepartmentMethod() {
    io.grpc.MethodDescriptor<com.entropy.core.autogen.grpc.department.DepartmentSaving, com.google.protobuf.Empty> getSaveDepartmentMethod;
    if ((getSaveDepartmentMethod = DepartmentServiceGrpc.getSaveDepartmentMethod) == null) {
      synchronized (DepartmentServiceGrpc.class) {
        if ((getSaveDepartmentMethod = DepartmentServiceGrpc.getSaveDepartmentMethod) == null) {
          DepartmentServiceGrpc.getSaveDepartmentMethod = getSaveDepartmentMethod =
              io.grpc.MethodDescriptor.<com.entropy.core.autogen.grpc.department.DepartmentSaving, com.google.protobuf.Empty>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "saveDepartment"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.entropy.core.autogen.grpc.department.DepartmentSaving.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.google.protobuf.Empty.getDefaultInstance()))
              .setSchemaDescriptor(new DepartmentServiceMethodDescriptorSupplier("saveDepartment"))
              .build();
        }
      }
    }
    return getSaveDepartmentMethod;
  }

  private static volatile io.grpc.MethodDescriptor<com.entropy.core.autogen.grpc.department.DepartmentIdRequest,
      com.google.protobuf.Empty> getRemoveDepartmentByIdMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "removeDepartmentById",
      requestType = com.entropy.core.autogen.grpc.department.DepartmentIdRequest.class,
      responseType = com.google.protobuf.Empty.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<com.entropy.core.autogen.grpc.department.DepartmentIdRequest,
      com.google.protobuf.Empty> getRemoveDepartmentByIdMethod() {
    io.grpc.MethodDescriptor<com.entropy.core.autogen.grpc.department.DepartmentIdRequest, com.google.protobuf.Empty> getRemoveDepartmentByIdMethod;
    if ((getRemoveDepartmentByIdMethod = DepartmentServiceGrpc.getRemoveDepartmentByIdMethod) == null) {
      synchronized (DepartmentServiceGrpc.class) {
        if ((getRemoveDepartmentByIdMethod = DepartmentServiceGrpc.getRemoveDepartmentByIdMethod) == null) {
          DepartmentServiceGrpc.getRemoveDepartmentByIdMethod = getRemoveDepartmentByIdMethod =
              io.grpc.MethodDescriptor.<com.entropy.core.autogen.grpc.department.DepartmentIdRequest, com.google.protobuf.Empty>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "removeDepartmentById"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.entropy.core.autogen.grpc.department.DepartmentIdRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.google.protobuf.Empty.getDefaultInstance()))
              .setSchemaDescriptor(new DepartmentServiceMethodDescriptorSupplier("removeDepartmentById"))
              .build();
        }
      }
    }
    return getRemoveDepartmentByIdMethod;
  }

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static DepartmentServiceStub newStub(io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<DepartmentServiceStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<DepartmentServiceStub>() {
        @java.lang.Override
        public DepartmentServiceStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new DepartmentServiceStub(channel, callOptions);
        }
      };
    return DepartmentServiceStub.newStub(factory, channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static DepartmentServiceBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<DepartmentServiceBlockingStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<DepartmentServiceBlockingStub>() {
        @java.lang.Override
        public DepartmentServiceBlockingStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new DepartmentServiceBlockingStub(channel, callOptions);
        }
      };
    return DepartmentServiceBlockingStub.newStub(factory, channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static DepartmentServiceFutureStub newFutureStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<DepartmentServiceFutureStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<DepartmentServiceFutureStub>() {
        @java.lang.Override
        public DepartmentServiceFutureStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new DepartmentServiceFutureStub(channel, callOptions);
        }
      };
    return DepartmentServiceFutureStub.newStub(factory, channel);
  }

  /**
   */
  public interface AsyncService {

    /**
     */
    default void findDepartments(com.google.protobuf.Empty request,
        io.grpc.stub.StreamObserver<com.entropy.core.autogen.grpc.department.DepartmentResponses> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getFindDepartmentsMethod(), responseObserver);
    }

    /**
     */
    default void findDepartmentById(com.entropy.core.autogen.grpc.department.DepartmentIdRequest request,
        io.grpc.stub.StreamObserver<com.entropy.core.autogen.grpc.department.Department> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getFindDepartmentByIdMethod(), responseObserver);
    }

    /**
     */
    default void saveDepartment(com.entropy.core.autogen.grpc.department.DepartmentSaving request,
        io.grpc.stub.StreamObserver<com.google.protobuf.Empty> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getSaveDepartmentMethod(), responseObserver);
    }

    /**
     */
    default void removeDepartmentById(com.entropy.core.autogen.grpc.department.DepartmentIdRequest request,
        io.grpc.stub.StreamObserver<com.google.protobuf.Empty> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getRemoveDepartmentByIdMethod(), responseObserver);
    }
  }

  /**
   * Base class for the server implementation of the service DepartmentService.
   */
  public static abstract class DepartmentServiceImplBase
      implements io.grpc.BindableService, AsyncService {

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return DepartmentServiceGrpc.bindService(this);
    }
  }

  /**
   * A stub to allow clients to do asynchronous rpc calls to service DepartmentService.
   */
  public static final class DepartmentServiceStub
      extends io.grpc.stub.AbstractAsyncStub<DepartmentServiceStub> {
    private DepartmentServiceStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected DepartmentServiceStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new DepartmentServiceStub(channel, callOptions);
    }

    /**
     */
    public void findDepartments(com.google.protobuf.Empty request,
        io.grpc.stub.StreamObserver<com.entropy.core.autogen.grpc.department.DepartmentResponses> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getFindDepartmentsMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void findDepartmentById(com.entropy.core.autogen.grpc.department.DepartmentIdRequest request,
        io.grpc.stub.StreamObserver<com.entropy.core.autogen.grpc.department.Department> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getFindDepartmentByIdMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void saveDepartment(com.entropy.core.autogen.grpc.department.DepartmentSaving request,
        io.grpc.stub.StreamObserver<com.google.protobuf.Empty> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getSaveDepartmentMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void removeDepartmentById(com.entropy.core.autogen.grpc.department.DepartmentIdRequest request,
        io.grpc.stub.StreamObserver<com.google.protobuf.Empty> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getRemoveDepartmentByIdMethod(), getCallOptions()), request, responseObserver);
    }
  }

  /**
   * A stub to allow clients to do synchronous rpc calls to service DepartmentService.
   */
  public static final class DepartmentServiceBlockingStub
      extends io.grpc.stub.AbstractBlockingStub<DepartmentServiceBlockingStub> {
    private DepartmentServiceBlockingStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected DepartmentServiceBlockingStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new DepartmentServiceBlockingStub(channel, callOptions);
    }

    /**
     */
    public com.entropy.core.autogen.grpc.department.DepartmentResponses findDepartments(com.google.protobuf.Empty request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getFindDepartmentsMethod(), getCallOptions(), request);
    }

    /**
     */
    public com.entropy.core.autogen.grpc.department.Department findDepartmentById(com.entropy.core.autogen.grpc.department.DepartmentIdRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getFindDepartmentByIdMethod(), getCallOptions(), request);
    }

    /**
     */
    public com.google.protobuf.Empty saveDepartment(com.entropy.core.autogen.grpc.department.DepartmentSaving request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getSaveDepartmentMethod(), getCallOptions(), request);
    }

    /**
     */
    public com.google.protobuf.Empty removeDepartmentById(com.entropy.core.autogen.grpc.department.DepartmentIdRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getRemoveDepartmentByIdMethod(), getCallOptions(), request);
    }
  }

  /**
   * A stub to allow clients to do ListenableFuture-style rpc calls to service DepartmentService.
   */
  public static final class DepartmentServiceFutureStub
      extends io.grpc.stub.AbstractFutureStub<DepartmentServiceFutureStub> {
    private DepartmentServiceFutureStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected DepartmentServiceFutureStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new DepartmentServiceFutureStub(channel, callOptions);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<com.entropy.core.autogen.grpc.department.DepartmentResponses> findDepartments(
        com.google.protobuf.Empty request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getFindDepartmentsMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<com.entropy.core.autogen.grpc.department.Department> findDepartmentById(
        com.entropy.core.autogen.grpc.department.DepartmentIdRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getFindDepartmentByIdMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<com.google.protobuf.Empty> saveDepartment(
        com.entropy.core.autogen.grpc.department.DepartmentSaving request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getSaveDepartmentMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<com.google.protobuf.Empty> removeDepartmentById(
        com.entropy.core.autogen.grpc.department.DepartmentIdRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getRemoveDepartmentByIdMethod(), getCallOptions()), request);
    }
  }

  private static final int METHODID_FIND_DEPARTMENTS = 0;
  private static final int METHODID_FIND_DEPARTMENT_BY_ID = 1;
  private static final int METHODID_SAVE_DEPARTMENT = 2;
  private static final int METHODID_REMOVE_DEPARTMENT_BY_ID = 3;

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
        case METHODID_FIND_DEPARTMENTS:
          serviceImpl.findDepartments((com.google.protobuf.Empty) request,
              (io.grpc.stub.StreamObserver<com.entropy.core.autogen.grpc.department.DepartmentResponses>) responseObserver);
          break;
        case METHODID_FIND_DEPARTMENT_BY_ID:
          serviceImpl.findDepartmentById((com.entropy.core.autogen.grpc.department.DepartmentIdRequest) request,
              (io.grpc.stub.StreamObserver<com.entropy.core.autogen.grpc.department.Department>) responseObserver);
          break;
        case METHODID_SAVE_DEPARTMENT:
          serviceImpl.saveDepartment((com.entropy.core.autogen.grpc.department.DepartmentSaving) request,
              (io.grpc.stub.StreamObserver<com.google.protobuf.Empty>) responseObserver);
          break;
        case METHODID_REMOVE_DEPARTMENT_BY_ID:
          serviceImpl.removeDepartmentById((com.entropy.core.autogen.grpc.department.DepartmentIdRequest) request,
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
          getFindDepartmentsMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              com.google.protobuf.Empty,
              com.entropy.core.autogen.grpc.department.DepartmentResponses>(
                service, METHODID_FIND_DEPARTMENTS)))
        .addMethod(
          getFindDepartmentByIdMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              com.entropy.core.autogen.grpc.department.DepartmentIdRequest,
              com.entropy.core.autogen.grpc.department.Department>(
                service, METHODID_FIND_DEPARTMENT_BY_ID)))
        .addMethod(
          getSaveDepartmentMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              com.entropy.core.autogen.grpc.department.DepartmentSaving,
              com.google.protobuf.Empty>(
                service, METHODID_SAVE_DEPARTMENT)))
        .addMethod(
          getRemoveDepartmentByIdMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              com.entropy.core.autogen.grpc.department.DepartmentIdRequest,
              com.google.protobuf.Empty>(
                service, METHODID_REMOVE_DEPARTMENT_BY_ID)))
        .build();
  }

  private static abstract class DepartmentServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    DepartmentServiceBaseDescriptorSupplier() {}

    @java.lang.Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return com.entropy.core.autogen.grpc.department.DepartmentOuterClass.getDescriptor();
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
      return getFileDescriptor().findServiceByName("DepartmentService");
    }
  }

  private static final class DepartmentServiceFileDescriptorSupplier
      extends DepartmentServiceBaseDescriptorSupplier {
    DepartmentServiceFileDescriptorSupplier() {}
  }

  private static final class DepartmentServiceMethodDescriptorSupplier
      extends DepartmentServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
    private final java.lang.String methodName;

    DepartmentServiceMethodDescriptorSupplier(java.lang.String methodName) {
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
      synchronized (DepartmentServiceGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new DepartmentServiceFileDescriptorSupplier())
              .addMethod(getFindDepartmentsMethod())
              .addMethod(getFindDepartmentByIdMethod())
              .addMethod(getSaveDepartmentMethod())
              .addMethod(getRemoveDepartmentByIdMethod())
              .build();
        }
      }
    }
    return result;
  }
}

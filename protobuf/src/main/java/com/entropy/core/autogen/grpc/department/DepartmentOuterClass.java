// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: department.proto

package com.entropy.core.autogen.grpc.department;

public final class DepartmentOuterClass {
  private DepartmentOuterClass() {}
  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistryLite registry) {
  }

  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistry registry) {
    registerAllExtensions(
        (com.google.protobuf.ExtensionRegistryLite) registry);
  }
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_department_Department_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_department_Department_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_department_DepartmentSaving_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_department_DepartmentSaving_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_department_DepartmentIdRequest_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_department_DepartmentIdRequest_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_department_DepartmentResponses_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_department_DepartmentResponses_fieldAccessorTable;

  public static com.google.protobuf.Descriptors.FileDescriptor
      getDescriptor() {
    return descriptor;
  }
  private static  com.google.protobuf.Descriptors.FileDescriptor
      descriptor;
  static {
    java.lang.String[] descriptorData = {
      "\n\020department.proto\022\ndepartment\032\033google/p" +
      "rotobuf/empty.proto\032\037google/protobuf/tim" +
      "estamp.proto\"A\n\nDepartment\022\014\n\004dpId\030\001 \001(\005" +
      "\022\016\n\006dpName\030\002 \001(\t\022\025\n\rdpDescription\030\003 \001(\t\"" +
      "9\n\020DepartmentSaving\022\016\n\006dpName\030\001 \001(\t\022\025\n\rd" +
      "pDescription\030\002 \001(\t\"#\n\023DepartmentIdReques" +
      "t\022\014\n\004dpId\030\001 \001(\005\"A\n\023DepartmentResponses\022*" +
      "\n\ndepartment\030\001 \003(\0132\026.department.Departme" +
      "nt2\307\002\n\021DepartmentService\022J\n\017findDepartme" +
      "nts\022\026.google.protobuf.Empty\032\037.department" +
      ".DepartmentResponses\022M\n\022findDepartmentBy" +
      "Id\022\037.department.DepartmentIdRequest\032\026.de" +
      "partment.Department\022F\n\016saveDepartment\022\034." +
      "department.DepartmentSaving\032\026.google.pro" +
      "tobuf.Empty\022O\n\024removeDepartmentById\022\037.de" +
      "partment.DepartmentIdRequest\032\026.google.pr" +
      "otobuf.EmptyB,\n(com.entropy.core.autogen" +
      ".grpc.departmentP\001b\006proto3"
    };
    com.google.protobuf.Descriptors.FileDescriptor.InternalDescriptorAssigner assigner =
        new com.google.protobuf.Descriptors.FileDescriptor.    InternalDescriptorAssigner() {
          public com.google.protobuf.ExtensionRegistry assignDescriptors(
              com.google.protobuf.Descriptors.FileDescriptor root) {
            descriptor = root;
            return null;
          }
        };
    com.google.protobuf.Descriptors.FileDescriptor
      .internalBuildGeneratedFileFrom(descriptorData,
        new com.google.protobuf.Descriptors.FileDescriptor[] {
          com.google.protobuf.EmptyProto.getDescriptor(),
          com.google.protobuf.TimestampProto.getDescriptor(),
        }, assigner);
    internal_static_department_Department_descriptor =
      getDescriptor().getMessageTypes().get(0);
    internal_static_department_Department_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_department_Department_descriptor,
        new java.lang.String[] { "DpId", "DpName", "DpDescription", });
    internal_static_department_DepartmentSaving_descriptor =
      getDescriptor().getMessageTypes().get(1);
    internal_static_department_DepartmentSaving_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_department_DepartmentSaving_descriptor,
        new java.lang.String[] { "DpName", "DpDescription", });
    internal_static_department_DepartmentIdRequest_descriptor =
      getDescriptor().getMessageTypes().get(2);
    internal_static_department_DepartmentIdRequest_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_department_DepartmentIdRequest_descriptor,
        new java.lang.String[] { "DpId", });
    internal_static_department_DepartmentResponses_descriptor =
      getDescriptor().getMessageTypes().get(3);
    internal_static_department_DepartmentResponses_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_department_DepartmentResponses_descriptor,
        new java.lang.String[] { "Department", });
    com.google.protobuf.EmptyProto.getDescriptor();
    com.google.protobuf.TimestampProto.getDescriptor();
  }

  // @@protoc_insertion_point(outer_class_scope)
}

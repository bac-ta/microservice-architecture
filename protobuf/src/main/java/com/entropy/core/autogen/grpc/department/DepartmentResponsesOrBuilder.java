// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: department.proto

package com.entropy.core.autogen.grpc.department;

public interface DepartmentResponsesOrBuilder extends
    // @@protoc_insertion_point(interface_extends:department.DepartmentResponses)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <code>repeated .department.Department department = 1;</code>
   */
  java.util.List<Department>
      getDepartmentList();
  /**
   * <code>repeated .department.Department department = 1;</code>
   */
  Department getDepartment(int index);
  /**
   * <code>repeated .department.Department department = 1;</code>
   */
  int getDepartmentCount();
  /**
   * <code>repeated .department.Department department = 1;</code>
   */
  java.util.List<? extends DepartmentOrBuilder>
      getDepartmentOrBuilderList();
  /**
   * <code>repeated .department.Department department = 1;</code>
   */
  DepartmentOrBuilder getDepartmentOrBuilder(
      int index);
}
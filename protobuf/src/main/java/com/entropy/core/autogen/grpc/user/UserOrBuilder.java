// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: user.proto

package com.entropy.core.autogen.grpc.user;

public interface UserOrBuilder extends
    // @@protoc_insertion_point(interface_extends:user.User)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <code>int32 id = 1;</code>
   */
  int getId();

  /**
   * <code>string name = 2;</code>
   */
  java.lang.String getName();
  /**
   * <code>string name = 2;</code>
   */
  com.google.protobuf.ByteString
      getNameBytes();

  /**
   * <code>.user.Gender gender = 3;</code>
   */
  int getGenderValue();
  /**
   * <code>.user.Gender gender = 3;</code>
   */
  com.entropy.core.autogen.grpc.user.Gender getGender();

  /**
   * <code>string email = 4;</code>
   */
  java.lang.String getEmail();
  /**
   * <code>string email = 4;</code>
   */
  com.google.protobuf.ByteString
      getEmailBytes();

  /**
   * <code>int32 age = 5;</code>
   */
  int getAge();

  /**
   * <code>string phoneNumber = 6;</code>
   */
  java.lang.String getPhoneNumber();
  /**
   * <code>string phoneNumber = 6;</code>
   */
  com.google.protobuf.ByteString
      getPhoneNumberBytes();

  /**
   * <code>int32 departmentId = 7;</code>
   */
  int getDepartmentId();
}

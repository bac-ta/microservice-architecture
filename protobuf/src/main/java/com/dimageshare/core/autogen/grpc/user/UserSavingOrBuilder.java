// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: user.proto

package com.dimageshare.core.autogen.grpc.user;

public interface UserSavingOrBuilder extends
    // @@protoc_insertion_point(interface_extends:user.UserSaving)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <code>string name = 1;</code>
   */
  java.lang.String getName();
  /**
   * <code>string name = 1;</code>
   */
  com.google.protobuf.ByteString
      getNameBytes();

  /**
   * <code>.user.Gender gender = 2;</code>
   */
  int getGenderValue();
  /**
   * <code>.user.Gender gender = 2;</code>
   */
  com.dimageshare.core.autogen.grpc.user.Gender getGender();

  /**
   * <code>string email = 3;</code>
   */
  java.lang.String getEmail();
  /**
   * <code>string email = 3;</code>
   */
  com.google.protobuf.ByteString
      getEmailBytes();

  /**
   * <code>int32 age = 4;</code>
   */
  int getAge();

  /**
   * <code>string phoneNumber = 5;</code>
   */
  java.lang.String getPhoneNumber();
  /**
   * <code>string phoneNumber = 5;</code>
   */
  com.google.protobuf.ByteString
      getPhoneNumberBytes();

  /**
   * <code>int32 departmentId = 6;</code>
   */
  int getDepartmentId();
}

// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: department.proto

package com.entropy.core.autogen.grpc.department;

/**
 * Protobuf type {@code department.Department}
 */
public  final class Department extends
    com.google.protobuf.GeneratedMessageV3 implements
    // @@protoc_insertion_point(message_implements:department.Department)
    DepartmentOrBuilder {
private static final long serialVersionUID = 0L;
  // Use Department.newBuilder() to construct.
  private Department(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
    super(builder);
  }
  private Department() {
    dpId_ = 0;
    dpName_ = "";
    dpDescription_ = "";
  }

  @java.lang.Override
  public final com.google.protobuf.UnknownFieldSet
  getUnknownFields() {
    return this.unknownFields;
  }
  private Department(
      com.google.protobuf.CodedInputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    this();
    if (extensionRegistry == null) {
      throw new java.lang.NullPointerException();
    }
    int mutable_bitField0_ = 0;
    com.google.protobuf.UnknownFieldSet.Builder unknownFields =
        com.google.protobuf.UnknownFieldSet.newBuilder();
    try {
      boolean done = false;
      while (!done) {
        int tag = input.readTag();
        switch (tag) {
          case 0:
            done = true;
            break;
          default: {
            if (!parseUnknownFieldProto3(
                input, unknownFields, extensionRegistry, tag)) {
              done = true;
            }
            break;
          }
          case 8: {

            dpId_ = input.readInt32();
            break;
          }
          case 18: {
            java.lang.String s = input.readStringRequireUtf8();

            dpName_ = s;
            break;
          }
          case 26: {
            java.lang.String s = input.readStringRequireUtf8();

            dpDescription_ = s;
            break;
          }
        }
      }
    } catch (com.google.protobuf.InvalidProtocolBufferException e) {
      throw e.setUnfinishedMessage(this);
    } catch (java.io.IOException e) {
      throw new com.google.protobuf.InvalidProtocolBufferException(
          e).setUnfinishedMessage(this);
    } finally {
      this.unknownFields = unknownFields.build();
      makeExtensionsImmutable();
    }
  }
  public static final com.google.protobuf.Descriptors.Descriptor
      getDescriptor() {
    return DepartmentOuterClass.internal_static_department_Department_descriptor;
  }

  protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internalGetFieldAccessorTable() {
    return DepartmentOuterClass.internal_static_department_Department_fieldAccessorTable
        .ensureFieldAccessorsInitialized(
            Department.class, Department.Builder.class);
  }

  public static final int DPID_FIELD_NUMBER = 1;
  private int dpId_;
  /**
   * <code>int32 dpId = 1;</code>
   */
  public int getDpId() {
    return dpId_;
  }

  public static final int DPNAME_FIELD_NUMBER = 2;
  private volatile java.lang.Object dpName_;
  /**
   * <code>string dpName = 2;</code>
   */
  public java.lang.String getDpName() {
    java.lang.Object ref = dpName_;
    if (ref instanceof java.lang.String) {
      return (java.lang.String) ref;
    } else {
      com.google.protobuf.ByteString bs = 
          (com.google.protobuf.ByteString) ref;
      java.lang.String s = bs.toStringUtf8();
      dpName_ = s;
      return s;
    }
  }
  /**
   * <code>string dpName = 2;</code>
   */
  public com.google.protobuf.ByteString
      getDpNameBytes() {
    java.lang.Object ref = dpName_;
    if (ref instanceof java.lang.String) {
      com.google.protobuf.ByteString b = 
          com.google.protobuf.ByteString.copyFromUtf8(
              (java.lang.String) ref);
      dpName_ = b;
      return b;
    } else {
      return (com.google.protobuf.ByteString) ref;
    }
  }

  public static final int DPDESCRIPTION_FIELD_NUMBER = 3;
  private volatile java.lang.Object dpDescription_;
  /**
   * <code>string dpDescription = 3;</code>
   */
  public java.lang.String getDpDescription() {
    java.lang.Object ref = dpDescription_;
    if (ref instanceof java.lang.String) {
      return (java.lang.String) ref;
    } else {
      com.google.protobuf.ByteString bs = 
          (com.google.protobuf.ByteString) ref;
      java.lang.String s = bs.toStringUtf8();
      dpDescription_ = s;
      return s;
    }
  }
  /**
   * <code>string dpDescription = 3;</code>
   */
  public com.google.protobuf.ByteString
      getDpDescriptionBytes() {
    java.lang.Object ref = dpDescription_;
    if (ref instanceof java.lang.String) {
      com.google.protobuf.ByteString b = 
          com.google.protobuf.ByteString.copyFromUtf8(
              (java.lang.String) ref);
      dpDescription_ = b;
      return b;
    } else {
      return (com.google.protobuf.ByteString) ref;
    }
  }

  private byte memoizedIsInitialized = -1;
  public final boolean isInitialized() {
    byte isInitialized = memoizedIsInitialized;
    if (isInitialized == 1) return true;
    if (isInitialized == 0) return false;

    memoizedIsInitialized = 1;
    return true;
  }

  public void writeTo(com.google.protobuf.CodedOutputStream output)
                      throws java.io.IOException {
    if (dpId_ != 0) {
      output.writeInt32(1, dpId_);
    }
    if (!getDpNameBytes().isEmpty()) {
      com.google.protobuf.GeneratedMessageV3.writeString(output, 2, dpName_);
    }
    if (!getDpDescriptionBytes().isEmpty()) {
      com.google.protobuf.GeneratedMessageV3.writeString(output, 3, dpDescription_);
    }
    unknownFields.writeTo(output);
  }

  public int getSerializedSize() {
    int size = memoizedSize;
    if (size != -1) return size;

    size = 0;
    if (dpId_ != 0) {
      size += com.google.protobuf.CodedOutputStream
        .computeInt32Size(1, dpId_);
    }
    if (!getDpNameBytes().isEmpty()) {
      size += com.google.protobuf.GeneratedMessageV3.computeStringSize(2, dpName_);
    }
    if (!getDpDescriptionBytes().isEmpty()) {
      size += com.google.protobuf.GeneratedMessageV3.computeStringSize(3, dpDescription_);
    }
    size += unknownFields.getSerializedSize();
    memoizedSize = size;
    return size;
  }

  @java.lang.Override
  public boolean equals(final java.lang.Object obj) {
    if (obj == this) {
     return true;
    }
    if (!(obj instanceof Department)) {
      return super.equals(obj);
    }
    Department other = (Department) obj;

    boolean result = true;
    result = result && (getDpId()
        == other.getDpId());
    result = result && getDpName()
        .equals(other.getDpName());
    result = result && getDpDescription()
        .equals(other.getDpDescription());
    result = result && unknownFields.equals(other.unknownFields);
    return result;
  }

  @java.lang.Override
  public int hashCode() {
    if (memoizedHashCode != 0) {
      return memoizedHashCode;
    }
    int hash = 41;
    hash = (19 * hash) + getDescriptor().hashCode();
    hash = (37 * hash) + DPID_FIELD_NUMBER;
    hash = (53 * hash) + getDpId();
    hash = (37 * hash) + DPNAME_FIELD_NUMBER;
    hash = (53 * hash) + getDpName().hashCode();
    hash = (37 * hash) + DPDESCRIPTION_FIELD_NUMBER;
    hash = (53 * hash) + getDpDescription().hashCode();
    hash = (29 * hash) + unknownFields.hashCode();
    memoizedHashCode = hash;
    return hash;
  }

  public static Department parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static Department parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static Department parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static Department parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static Department parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static Department parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static Department parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static Department parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input, extensionRegistry);
  }
  public static Department parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input);
  }
  public static Department parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
  }
  public static Department parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static Department parseFrom(
      com.google.protobuf.CodedInputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input, extensionRegistry);
  }

  public Builder newBuilderForType() { return newBuilder(); }
  public static Builder newBuilder() {
    return DEFAULT_INSTANCE.toBuilder();
  }
  public static Builder newBuilder(Department prototype) {
    return DEFAULT_INSTANCE.toBuilder().mergeFrom(prototype);
  }
  public Builder toBuilder() {
    return this == DEFAULT_INSTANCE
        ? new Builder() : new Builder().mergeFrom(this);
  }

  @java.lang.Override
  protected Builder newBuilderForType(
      com.google.protobuf.GeneratedMessageV3.BuilderParent parent) {
    Builder builder = new Builder(parent);
    return builder;
  }
  /**
   * Protobuf type {@code department.Department}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessageV3.Builder<Builder> implements
      // @@protoc_insertion_point(builder_implements:department.Department)
          DepartmentOrBuilder {
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return DepartmentOuterClass.internal_static_department_Department_descriptor;
    }

    protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return DepartmentOuterClass.internal_static_department_Department_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              Department.class, Department.Builder.class);
    }

    // Construct using com.entropy.core.autogen.grpc.department.Department.newBuilder()
    private Builder() {
      maybeForceBuilderInitialization();
    }

    private Builder(
        com.google.protobuf.GeneratedMessageV3.BuilderParent parent) {
      super(parent);
      maybeForceBuilderInitialization();
    }
    private void maybeForceBuilderInitialization() {
      if (com.google.protobuf.GeneratedMessageV3
              .alwaysUseFieldBuilders) {
      }
    }
    public Builder clear() {
      super.clear();
      dpId_ = 0;

      dpName_ = "";

      dpDescription_ = "";

      return this;
    }

    public com.google.protobuf.Descriptors.Descriptor
        getDescriptorForType() {
      return DepartmentOuterClass.internal_static_department_Department_descriptor;
    }

    public Department getDefaultInstanceForType() {
      return Department.getDefaultInstance();
    }

    public Department build() {
      Department result = buildPartial();
      if (!result.isInitialized()) {
        throw newUninitializedMessageException(result);
      }
      return result;
    }

    public Department buildPartial() {
      Department result = new Department(this);
      result.dpId_ = dpId_;
      result.dpName_ = dpName_;
      result.dpDescription_ = dpDescription_;
      onBuilt();
      return result;
    }

    public Builder clone() {
      return (Builder) super.clone();
    }
    public Builder setField(
        com.google.protobuf.Descriptors.FieldDescriptor field,
        java.lang.Object value) {
      return (Builder) super.setField(field, value);
    }
    public Builder clearField(
        com.google.protobuf.Descriptors.FieldDescriptor field) {
      return (Builder) super.clearField(field);
    }
    public Builder clearOneof(
        com.google.protobuf.Descriptors.OneofDescriptor oneof) {
      return (Builder) super.clearOneof(oneof);
    }
    public Builder setRepeatedField(
        com.google.protobuf.Descriptors.FieldDescriptor field,
        int index, java.lang.Object value) {
      return (Builder) super.setRepeatedField(field, index, value);
    }
    public Builder addRepeatedField(
        com.google.protobuf.Descriptors.FieldDescriptor field,
        java.lang.Object value) {
      return (Builder) super.addRepeatedField(field, value);
    }
    public Builder mergeFrom(com.google.protobuf.Message other) {
      if (other instanceof Department) {
        return mergeFrom((Department)other);
      } else {
        super.mergeFrom(other);
        return this;
      }
    }

    public Builder mergeFrom(Department other) {
      if (other == Department.getDefaultInstance()) return this;
      if (other.getDpId() != 0) {
        setDpId(other.getDpId());
      }
      if (!other.getDpName().isEmpty()) {
        dpName_ = other.dpName_;
        onChanged();
      }
      if (!other.getDpDescription().isEmpty()) {
        dpDescription_ = other.dpDescription_;
        onChanged();
      }
      this.mergeUnknownFields(other.unknownFields);
      onChanged();
      return this;
    }

    public final boolean isInitialized() {
      return true;
    }

    public Builder mergeFrom(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      Department parsedMessage = null;
      try {
        parsedMessage = PARSER.parsePartialFrom(input, extensionRegistry);
      } catch (com.google.protobuf.InvalidProtocolBufferException e) {
        parsedMessage = (Department) e.getUnfinishedMessage();
        throw e.unwrapIOException();
      } finally {
        if (parsedMessage != null) {
          mergeFrom(parsedMessage);
        }
      }
      return this;
    }

    private int dpId_ ;
    /**
     * <code>int32 dpId = 1;</code>
     */
    public int getDpId() {
      return dpId_;
    }
    /**
     * <code>int32 dpId = 1;</code>
     */
    public Builder setDpId(int value) {
      
      dpId_ = value;
      onChanged();
      return this;
    }
    /**
     * <code>int32 dpId = 1;</code>
     */
    public Builder clearDpId() {
      
      dpId_ = 0;
      onChanged();
      return this;
    }

    private java.lang.Object dpName_ = "";
    /**
     * <code>string dpName = 2;</code>
     */
    public java.lang.String getDpName() {
      java.lang.Object ref = dpName_;
      if (!(ref instanceof java.lang.String)) {
        com.google.protobuf.ByteString bs =
            (com.google.protobuf.ByteString) ref;
        java.lang.String s = bs.toStringUtf8();
        dpName_ = s;
        return s;
      } else {
        return (java.lang.String) ref;
      }
    }
    /**
     * <code>string dpName = 2;</code>
     */
    public com.google.protobuf.ByteString
        getDpNameBytes() {
      java.lang.Object ref = dpName_;
      if (ref instanceof String) {
        com.google.protobuf.ByteString b = 
            com.google.protobuf.ByteString.copyFromUtf8(
                (java.lang.String) ref);
        dpName_ = b;
        return b;
      } else {
        return (com.google.protobuf.ByteString) ref;
      }
    }
    /**
     * <code>string dpName = 2;</code>
     */
    public Builder setDpName(
        java.lang.String value) {
      if (value == null) {
    throw new NullPointerException();
  }
  
      dpName_ = value;
      onChanged();
      return this;
    }
    /**
     * <code>string dpName = 2;</code>
     */
    public Builder clearDpName() {
      
      dpName_ = getDefaultInstance().getDpName();
      onChanged();
      return this;
    }
    /**
     * <code>string dpName = 2;</code>
     */
    public Builder setDpNameBytes(
        com.google.protobuf.ByteString value) {
      if (value == null) {
    throw new NullPointerException();
  }
  checkByteStringIsUtf8(value);
      
      dpName_ = value;
      onChanged();
      return this;
    }

    private java.lang.Object dpDescription_ = "";
    /**
     * <code>string dpDescription = 3;</code>
     */
    public java.lang.String getDpDescription() {
      java.lang.Object ref = dpDescription_;
      if (!(ref instanceof java.lang.String)) {
        com.google.protobuf.ByteString bs =
            (com.google.protobuf.ByteString) ref;
        java.lang.String s = bs.toStringUtf8();
        dpDescription_ = s;
        return s;
      } else {
        return (java.lang.String) ref;
      }
    }
    /**
     * <code>string dpDescription = 3;</code>
     */
    public com.google.protobuf.ByteString
        getDpDescriptionBytes() {
      java.lang.Object ref = dpDescription_;
      if (ref instanceof String) {
        com.google.protobuf.ByteString b = 
            com.google.protobuf.ByteString.copyFromUtf8(
                (java.lang.String) ref);
        dpDescription_ = b;
        return b;
      } else {
        return (com.google.protobuf.ByteString) ref;
      }
    }
    /**
     * <code>string dpDescription = 3;</code>
     */
    public Builder setDpDescription(
        java.lang.String value) {
      if (value == null) {
    throw new NullPointerException();
  }
  
      dpDescription_ = value;
      onChanged();
      return this;
    }
    /**
     * <code>string dpDescription = 3;</code>
     */
    public Builder clearDpDescription() {
      
      dpDescription_ = getDefaultInstance().getDpDescription();
      onChanged();
      return this;
    }
    /**
     * <code>string dpDescription = 3;</code>
     */
    public Builder setDpDescriptionBytes(
        com.google.protobuf.ByteString value) {
      if (value == null) {
    throw new NullPointerException();
  }
  checkByteStringIsUtf8(value);
      
      dpDescription_ = value;
      onChanged();
      return this;
    }
    public final Builder setUnknownFields(
        final com.google.protobuf.UnknownFieldSet unknownFields) {
      return super.setUnknownFieldsProto3(unknownFields);
    }

    public final Builder mergeUnknownFields(
        final com.google.protobuf.UnknownFieldSet unknownFields) {
      return super.mergeUnknownFields(unknownFields);
    }


    // @@protoc_insertion_point(builder_scope:department.Department)
  }

  // @@protoc_insertion_point(class_scope:department.Department)
  private static final Department DEFAULT_INSTANCE;
  static {
    DEFAULT_INSTANCE = new Department();
  }

  public static Department getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static final com.google.protobuf.Parser<Department>
      PARSER = new com.google.protobuf.AbstractParser<Department>() {
    public Department parsePartialFrom(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return new Department(input, extensionRegistry);
    }
  };

  public static com.google.protobuf.Parser<Department> parser() {
    return PARSER;
  }

  @java.lang.Override
  public com.google.protobuf.Parser<Department> getParserForType() {
    return PARSER;
  }

  public Department getDefaultInstanceForType() {
    return DEFAULT_INSTANCE;
  }

}

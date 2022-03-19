package com.entropy.gateway.services.impls;

import com.entropy.core.autogen.grpc.department.Department;
import com.entropy.core.autogen.grpc.department.DepartmentIdRequest;
import com.entropy.core.autogen.grpc.department.DepartmentResponses;
import com.entropy.core.autogen.grpc.department.DepartmentSaving;
import com.entropy.core.autogen.grpc.department.DepartmentServiceGrpc;
import com.entropy.gateway.models.requests.DepartmentRequest;
import com.entropy.gateway.models.responses.DepartmentResponse;
import com.entropy.gateway.services.DepartmentService;
import com.entropy.grpc.client.models.annotations.GrpcClient;
import com.google.common.annotations.VisibleForTesting;
import com.google.protobuf.Empty;
import io.grpc.Channel;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DepartmentServiceImpl implements DepartmentService {
    @GrpcClient(value = "department")
    private Channel channel;
    private DepartmentServiceGrpc.DepartmentServiceBlockingStub stub;

    @VisibleForTesting
    protected DepartmentServiceImpl(final Channel channel) {
        this.channel = channel;
    }

    public DepartmentServiceImpl() {
        super();
    }

    private DepartmentServiceGrpc.DepartmentServiceBlockingStub getStub() {
        return DepartmentServiceGrpc.newBlockingStub(channel);
    }

    @Override
    public DepartmentResponse findById(int id) {
        DepartmentIdRequest request = DepartmentIdRequest.newBuilder().setDpId(id).build();
        stub = getStub();
        Department department = stub.findDepartmentById(request);
        String name = department.getDpName();
        String description = department.getDpDescription();
        return new DepartmentResponse(id, name, description);
    }

    @Override
    public List<DepartmentResponse> findAll() {
        stub = getStub();
        DepartmentResponses departments = stub.findDepartments(Empty.getDefaultInstance());
        return departments.getDepartmentList().stream().map(el -> new DepartmentResponse(el.getDpId(), el.getDpName(), el.getDpDescription())).collect(Collectors.toList());
    }

    @Override
    public void removeById(int id) {
        stub = getStub();
        DepartmentIdRequest request = DepartmentIdRequest.newBuilder().setDpId(id).build();
        stub.removeDepartmentById(request);
    }

    @Override
    public void saveDepartment(DepartmentRequest request) {
        stub = getStub();
        String name = request.getName();
        String description = request.getDescription();
        DepartmentSaving department = DepartmentSaving.newBuilder().setDpName(name).setDpDescription(description).build();
        stub.saveDepartment(department);
    }

}
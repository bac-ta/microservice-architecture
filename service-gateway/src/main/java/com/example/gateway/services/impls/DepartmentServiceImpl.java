package com.example.gateway.services.impls;

import com.example.core.autogen.grpc.department.DepartmentIdRequest;
import com.example.core.autogen.grpc.department.DepartmentSaving;
import com.example.core.autogen.grpc.department.DepartmentServiceGrpc;
import com.example.gateway.models.requests.DepartmentRequest;
import com.example.gateway.models.responses.DepartmentResponse;
import com.example.gateway.services.DepartmentService;
import com.example.grpc.client.models.annotations.GrpcClient;
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
        var request = DepartmentIdRequest.newBuilder().setDpId(id).build();
        stub = getStub();
        var department = stub.findDepartmentById(request);
        var name = department.getDpName();
        var description = department.getDpDescription();
        return new DepartmentResponse(id, name, description);
    }

    @Override
    public List<DepartmentResponse> findAll() {
        stub = getStub();
        var departments = stub.findDepartments(Empty.getDefaultInstance());
        return departments.getDepartmentList().stream().map(el -> new DepartmentResponse(el.getDpId(), el.getDpName(), el.getDpDescription())).collect(Collectors.toList());
    }

    @Override
    public void removeById(int id) {
        stub = getStub();
        var request = DepartmentIdRequest.newBuilder().setDpId(id).build();
        stub.removeDepartmentById(request);
    }

    @Override
    public void saveDepartment(DepartmentRequest request) {
        stub = getStub();
        var name = request.getName();
        var description = request.getDescription();
        var department = DepartmentSaving.newBuilder().setDpName(name).setDpDescription(description).build();
        stub.saveDepartment(department);
    }

}
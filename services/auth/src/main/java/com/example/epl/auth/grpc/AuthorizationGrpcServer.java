package com.example.epl.auth.grpc;

import com.example.epl.auth.v1.AuthorizationServiceGrpc;
import com.example.epl.auth.v1.BatchCheckRequest;
import com.example.epl.auth.v1.BatchCheckResponse;
import com.example.epl.auth.v1.CheckRequest;
import com.example.epl.auth.v1.CheckResponse;
import com.example.epl.auth.v1.DeletePolicyRequest;
import com.example.epl.auth.v1.ListPoliciesRequest;
import com.example.epl.auth.v1.ListPoliciesResponse;
import com.example.epl.auth.v1.Policy;
import com.example.grpc.server.models.annotations.GrpcServer;
import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;

/**
 * gRPC endpoint epl.auth.v1.AuthorizationService (contract: proto/src/main/proto/epl/auth/v1/auth.proto).
 * Every RPC answers UNIMPLEMENTED, naming the screens that call it, until it is implemented.
 */
@GrpcServer(AuthorizationServiceGrpc.class)
public class AuthorizationGrpcServer extends AuthorizationServiceGrpc.AuthorizationServiceImplBase {

    @Override
    public void check(CheckRequest request, StreamObserver<CheckResponse> responseObserver) {
        responseObserver.onError(NotPorted.error("epl.auth.v1.AuthorizationService/Check"));
    }

    @Override
    public void batchCheck(BatchCheckRequest request, StreamObserver<BatchCheckResponse> responseObserver) {
        responseObserver.onError(NotPorted.error("epl.auth.v1.AuthorizationService/BatchCheck"));
    }

    @Override
    public void listPolicies(ListPoliciesRequest request, StreamObserver<ListPoliciesResponse> responseObserver) {
        responseObserver.onError(NotPorted.error("epl.auth.v1.AuthorizationService/ListPolicies", "EPL-SY-310W"));
    }

    @Override
    public void upsertPolicy(Policy request, StreamObserver<Policy> responseObserver) {
        responseObserver.onError(NotPorted.error("epl.auth.v1.AuthorizationService/UpsertPolicy", "EPL-SY-310W"));
    }

    @Override
    public void deletePolicy(DeletePolicyRequest request, StreamObserver<Empty> responseObserver) {
        responseObserver.onError(NotPorted.error("epl.auth.v1.AuthorizationService/DeletePolicy", "EPL-SY-310W"));
    }
}

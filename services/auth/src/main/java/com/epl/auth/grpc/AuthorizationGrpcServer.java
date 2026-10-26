package com.epl.auth.grpc;

import com.epl.auth.v1.AuthorizationServiceGrpc;
import com.epl.auth.v1.BatchCheckRequest;
import com.epl.auth.v1.BatchCheckResponse;
import com.epl.auth.v1.CheckRequest;
import com.epl.auth.v1.CheckResponse;
import com.epl.auth.v1.DeletePolicyRequest;
import com.epl.auth.v1.ListPoliciesRequest;
import com.epl.auth.v1.ListPoliciesResponse;
import com.epl.auth.v1.Policy;
import com.epl.grpc.server.models.annotations.GrpcServer;
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

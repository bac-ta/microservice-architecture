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
 * Every RPC answers UNIMPLEMENTED until the legacy logic named in its Javadoc is ported.
 */
@GrpcServer(AuthorizationServiceGrpc.class)
public class AuthorizationGrpcServer extends AuthorizationServiceGrpc.AuthorizationServiceImplBase {

    /**
     * Legacy: GWH_PR_PRTTN_010 (GWH_TM_PEVT function-key permissions)
     */
    @Override
    public void check(CheckRequest request, StreamObserver<CheckResponse> responseObserver) {
        responseObserver.onError(NotPorted.error("epl.auth.v1.AuthorizationService/Check", "GWH_PR_PRTTN_010 (GWH_TM_PEVT function-key permissions)"));
    }

    /**
     * Legacy: BTNAUTHMST (which buttons a screen may show)
     */
    @Override
    public void batchCheck(BatchCheckRequest request, StreamObserver<BatchCheckResponse> responseObserver) {
        responseObserver.onError(NotPorted.error("epl.auth.v1.AuthorizationService/BatchCheck", "BTNAUTHMST (which buttons a screen may show)"));
    }

    /**
     * Legacy: new in EPL
     */
    @Override
    public void listPolicies(ListPoliciesRequest request, StreamObserver<ListPoliciesResponse> responseObserver) {
        responseObserver.onError(NotPorted.error("epl.auth.v1.AuthorizationService/ListPolicies", "new in EPL"));
    }

    /**
     * Legacy: new in EPL
     */
    @Override
    public void upsertPolicy(Policy request, StreamObserver<Policy> responseObserver) {
        responseObserver.onError(NotPorted.error("epl.auth.v1.AuthorizationService/UpsertPolicy", "new in EPL"));
    }

    /**
     * Legacy: new in EPL
     */
    @Override
    public void deletePolicy(DeletePolicyRequest request, StreamObserver<Empty> responseObserver) {
        responseObserver.onError(NotPorted.error("epl.auth.v1.AuthorizationService/DeletePolicy", "new in EPL"));
    }
}

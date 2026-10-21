package com.example.epl.integration.grpc;

import com.example.epl.integration.v1.Job;
import com.example.epl.integration.v1.JobServiceGrpc;
import com.example.epl.integration.v1.ListJobsRequest;
import com.example.epl.integration.v1.ListJobsResponse;
import com.example.epl.integration.v1.ListPurgeHistoryRequest;
import com.example.epl.integration.v1.ListPurgeHistoryResponse;
import com.example.epl.integration.v1.RunPurgeRequest;
import com.example.grpc.server.models.annotations.GrpcServer;
import io.grpc.stub.StreamObserver;

/**
 * gRPC endpoint epl.integration.v1.JobService (contract: proto/src/main/proto/epl/integration/v1/integration.proto).
 * Every RPC answers UNIMPLEMENTED, naming the screens that call it, until it is implemented.
 */
@GrpcServer(JobServiceGrpc.class)
public class JobGrpcServer extends JobServiceGrpc.JobServiceImplBase {

    @Override
    public void listJobs(ListJobsRequest request, StreamObserver<ListJobsResponse> responseObserver) {
        responseObserver.onError(NotPorted.error("epl.integration.v1.JobService/ListJobs", "EPL-IT-310W"));
    }

    @Override
    public void runPurge(RunPurgeRequest request, StreamObserver<Job> responseObserver) {
        responseObserver.onError(NotPorted.error("epl.integration.v1.JobService/RunPurge", "EPL-IT-320W"));
    }

    @Override
    public void listPurgeHistory(ListPurgeHistoryRequest request, StreamObserver<ListPurgeHistoryResponse> responseObserver) {
        responseObserver.onError(NotPorted.error("epl.integration.v1.JobService/ListPurgeHistory", "EPL-IT-320W"));
    }
}

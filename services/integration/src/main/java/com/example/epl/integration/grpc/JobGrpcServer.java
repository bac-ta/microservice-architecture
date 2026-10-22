package com.example.epl.integration.grpc;

import com.example.epl.integration.services.JobService;
import com.example.epl.integration.v1.CreateJobRequest;
import com.example.epl.integration.v1.DeleteJobRequest;
import com.example.epl.integration.v1.GetJobRequest;
import com.example.epl.integration.v1.Job;
import com.example.epl.integration.v1.JobServiceGrpc;
import com.example.epl.integration.v1.ListJobDefinitionsResponse;
import com.example.epl.integration.v1.ListJobsRequest;
import com.example.epl.integration.v1.ListJobsResponse;
import com.example.epl.integration.v1.ListPurgeHistoryRequest;
import com.example.epl.integration.v1.ListPurgeHistoryResponse;
import com.example.epl.integration.v1.RunPurgeRequest;
import com.example.grpc.server.models.annotations.GrpcServer;
import com.example.service.core.GrpcCalls;
import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;

/**
 * gRPC endpoint epl.integration.v1.JobService (contract: proto/src/main/proto/epl/integration/v1/integration.proto).
 * Job runs and purge history are implemented; RunPurge answers UNIMPLEMENTED, naming its screen, until it is.
 */
@GrpcServer(JobServiceGrpc.class)
public class JobGrpcServer extends JobServiceGrpc.JobServiceImplBase {

    private final JobService jobService;

    public JobGrpcServer(JobService jobService) {
        this.jobService = jobService;
    }

    @Override
    public void listJobDefinitions(Empty request, StreamObserver<ListJobDefinitionsResponse> responseObserver) {
        GrpcCalls.unary(responseObserver, () -> ListJobDefinitionsResponse.newBuilder().addAllDefinitions(jobService.definitions()).build());
    }

    @Override
    public void listJobs(ListJobsRequest request, StreamObserver<ListJobsResponse> responseObserver) {
        GrpcCalls.unary(responseObserver, () -> jobService.list(request));
    }

    @Override
    public void getJob(GetJobRequest request, StreamObserver<Job> responseObserver) {
        GrpcCalls.unary(responseObserver, () -> jobService.get(request.getId()));
    }

    @Override
    public void createJob(CreateJobRequest request, StreamObserver<Job> responseObserver) {
        GrpcCalls.unary(responseObserver, () -> jobService.create(request));
    }

    @Override
    public void deleteJob(DeleteJobRequest request, StreamObserver<Empty> responseObserver) {
        GrpcCalls.unaryEmpty(responseObserver, () -> jobService.delete(request.getId()));
    }

    @Override
    public void runPurge(RunPurgeRequest request, StreamObserver<Job> responseObserver) {
        responseObserver.onError(NotPorted.error("epl.integration.v1.JobService/RunPurge", "EPL-IT-320W"));
    }

    @Override
    public void listPurgeHistory(ListPurgeHistoryRequest request, StreamObserver<ListPurgeHistoryResponse> responseObserver) {
        GrpcCalls.unary(responseObserver, () -> jobService.purgeHistory(request));
    }
}

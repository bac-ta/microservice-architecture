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
 * Every RPC answers UNIMPLEMENTED until the legacy logic named in its Javadoc is ported.
 */
@GrpcServer(JobServiceGrpc.class)
public class JobGrpcServer extends JobServiceGrpc.JobServiceImplBase {

    /**
     * Legacy: gwh_lpbtj_010 (GWH_TJ_BJST batch job status)
     */
    @Override
    public void listJobs(ListJobsRequest request, StreamObserver<ListJobsResponse> responseObserver) {
        responseObserver.onError(NotPorted.error("epl.integration.v1.JobService/ListJobs", "gwh_lpbtj_010 (GWH_TJ_BJST batch job status)"));
    }

    /**
     * Legacy: gwh_dpmgr_010 / gwhDpmgr010Exe (data purge)
     */
    @Override
    public void runPurge(RunPurgeRequest request, StreamObserver<Job> responseObserver) {
        responseObserver.onError(NotPorted.error("epl.integration.v1.JobService/RunPurge", "gwh_dpmgr_010 / gwhDpmgr010Exe (data purge)"));
    }

    /**
     * Legacy: gwh_lpddh_010, gwh_dpmgr_020 (GWH_TJ_DDH purge history)
     */
    @Override
    public void listPurgeHistory(ListPurgeHistoryRequest request, StreamObserver<ListPurgeHistoryResponse> responseObserver) {
        responseObserver.onError(NotPorted.error("epl.integration.v1.JobService/ListPurgeHistory", "gwh_lpddh_010, gwh_dpmgr_020 (GWH_TJ_DDH purge history)"));
    }
}

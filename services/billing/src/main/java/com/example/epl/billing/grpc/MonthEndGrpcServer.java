package com.example.epl.billing.grpc;

import com.example.epl.billing.v1.GetMonthEndStatusRequest;
import com.example.epl.billing.v1.MonthEndServiceGrpc;
import com.example.epl.billing.v1.MonthEndStatus;
import com.example.epl.billing.v1.RunMonthEndRequest;
import com.example.grpc.server.models.annotations.GrpcServer;
import io.grpc.stub.StreamObserver;

/**
 * gRPC endpoint epl.billing.v1.MonthEndService (contract: proto/src/main/proto/epl/billing/v1/billing.proto).
 * Every RPC answers UNIMPLEMENTED until the legacy logic named in its Javadoc is ported.
 */
@GrpcServer(MonthEndServiceGrpc.class)
public class MonthEndGrpcServer extends MonthEndServiceGrpc.MonthEndServiceImplBase {

    /**
     * Legacy: gwh_lpwmc_010 / 110 / 130 *Run, gwh_lpwbf_010 / 110 *Run (WMBF / WEMB creation)
     */
    @Override
    public void run(RunMonthEndRequest request, StreamObserver<MonthEndStatus> responseObserver) {
        responseObserver.onError(NotPorted.error("epl.billing.v1.MonthEndService/Run", "gwh_lpwmc_010 / 110 / 130 *Run, gwh_lpwbf_010 / 110 *Run (WMBF / WEMB creation)"));
    }

    /**
     * Legacy: gwh_lpwbf_110 / gwhLpwbf110Get (WMBS / WEMS status)
     */
    @Override
    public void getStatus(GetMonthEndStatusRequest request, StreamObserver<MonthEndStatus> responseObserver) {
        responseObserver.onError(NotPorted.error("epl.billing.v1.MonthEndService/GetStatus", "gwh_lpwbf_110 / gwhLpwbf110Get (WMBS / WEMS status)"));
    }
}

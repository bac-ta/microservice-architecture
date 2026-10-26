package com.epl.billing.grpc;

import com.epl.billing.v1.GetMonthEndStatusRequest;
import com.epl.billing.v1.MonthEndServiceGrpc;
import com.epl.billing.v1.MonthEndStatus;
import com.epl.billing.v1.RunMonthEndRequest;
import com.epl.grpc.server.models.annotations.GrpcServer;
import io.grpc.stub.StreamObserver;

/**
 * gRPC endpoint epl.billing.v1.MonthEndService (contract: proto/src/main/proto/epl/billing/v1/billing.proto).
 * Every RPC answers UNIMPLEMENTED, naming the screens that call it, until it is implemented.
 */
@GrpcServer(MonthEndServiceGrpc.class)
public class MonthEndGrpcServer extends MonthEndServiceGrpc.MonthEndServiceImplBase {

    @Override
    public void run(RunMonthEndRequest request, StreamObserver<MonthEndStatus> responseObserver) {
        responseObserver.onError(NotPorted.error("epl.billing.v1.MonthEndService/Run", "EPL-BL-310W"));
    }

    @Override
    public void getStatus(GetMonthEndStatusRequest request, StreamObserver<MonthEndStatus> responseObserver) {
        responseObserver.onError(NotPorted.error("epl.billing.v1.MonthEndService/GetStatus", "EPL-BL-310W"));
    }
}

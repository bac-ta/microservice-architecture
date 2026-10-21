package com.example.epl.report.grpc;

import com.example.epl.report.v1.GetInventoryTurnoverRequest;
import com.example.epl.report.v1.GetInventoryTurnoverResponse;
import com.example.epl.report.v1.KpiServiceGrpc;
import com.example.grpc.server.models.annotations.GrpcServer;
import io.grpc.stub.StreamObserver;

/**
 * gRPC endpoint epl.report.v1.KpiService (contract: proto/src/main/proto/epl/report/v1/report.proto).
 * Every RPC answers UNIMPLEMENTED, naming the screens that call it, until it is implemented.
 */
@GrpcServer(KpiServiceGrpc.class)
public class KpiGrpcServer extends KpiServiceGrpc.KpiServiceImplBase {

    @Override
    public void getInventoryTurnover(GetInventoryTurnoverRequest request, StreamObserver<GetInventoryTurnoverResponse> responseObserver) {
        responseObserver.onError(NotPorted.error("epl.report.v1.KpiService/GetInventoryTurnover", "EPL-RP-310W"));
    }
}

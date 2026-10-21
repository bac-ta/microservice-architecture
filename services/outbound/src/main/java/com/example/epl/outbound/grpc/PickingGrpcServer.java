package com.example.epl.outbound.grpc;

import com.example.epl.outbound.v1.CreatePickingBatchRequest;
import com.example.epl.outbound.v1.PickingBatch;
import com.example.epl.outbound.v1.PickingServiceGrpc;
import com.example.epl.outbound.v1.RecordPickRequest;
import com.example.epl.outbound.v1.RecordPickResponse;
import com.example.grpc.server.models.annotations.GrpcServer;
import io.grpc.stub.StreamObserver;

/**
 * gRPC endpoint epl.outbound.v1.PickingService (contract: proto/src/main/proto/epl/outbound/v1/outbound.proto).
 * Every RPC answers UNIMPLEMENTED, naming the screens that call it, until it is implemented.
 */
@GrpcServer(PickingServiceGrpc.class)
public class PickingGrpcServer extends PickingServiceGrpc.PickingServiceImplBase {

    @Override
    public void createPickingBatch(CreatePickingBatchRequest request, StreamObserver<PickingBatch> responseObserver) {
        responseObserver.onError(NotPorted.error("epl.outbound.v1.PickingService/CreatePickingBatch", "EPL-OB-310W"));
    }

    @Override
    public void recordPick(RecordPickRequest request, StreamObserver<RecordPickResponse> responseObserver) {
        responseObserver.onError(NotPorted.error("epl.outbound.v1.PickingService/RecordPick", "EPL-OB-320H"));
    }
}

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
 * Every RPC answers UNIMPLEMENTED until the legacy logic named in its Javadoc is ported.
 */
@GrpcServer(PickingServiceGrpc.class)
public class PickingGrpcServer extends PickingServiceGrpc.PickingServiceImplBase {

    /**
     * Legacy: gwh_spbtg_010 / hpbtg (batch & total-picking grouping), gwh_spplt
     */
    @Override
    public void createPickingBatch(CreatePickingBatchRequest request, StreamObserver<PickingBatch> responseObserver) {
        responseObserver.onError(NotPorted.error("epl.outbound.v1.PickingService/CreatePickingBatch", "gwh_spbtg_010 / hpbtg (batch & total-picking grouping), gwh_spplt"));
    }

    /**
     * Legacy: gwh_sbpic / gwh_shpic (HHT picking by case / SKU), gwh_sbpip / gwh_shpip
     */
    @Override
    public void recordPick(RecordPickRequest request, StreamObserver<RecordPickResponse> responseObserver) {
        responseObserver.onError(NotPorted.error("epl.outbound.v1.PickingService/RecordPick", "gwh_sbpic / gwh_shpic (HHT picking by case / SKU), gwh_sbpip / gwh_shpip"));
    }
}

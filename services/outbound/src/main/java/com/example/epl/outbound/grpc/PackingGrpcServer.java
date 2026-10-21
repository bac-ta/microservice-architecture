package com.example.epl.outbound.grpc;

import com.example.epl.outbound.v1.ListCartonsRequest;
import com.example.epl.outbound.v1.ListCartonsResponse;
import com.example.epl.outbound.v1.PackingServiceGrpc;
import com.example.epl.outbound.v1.RecordPackingRequest;
import com.example.epl.outbound.v1.RecordPackingResponse;
import com.example.grpc.server.models.annotations.GrpcServer;
import io.grpc.stub.StreamObserver;

/**
 * gRPC endpoint epl.outbound.v1.PackingService (contract: proto/src/main/proto/epl/outbound/v1/outbound.proto).
 * Every RPC answers UNIMPLEMENTED, naming the screens that call it, until it is implemented.
 */
@GrpcServer(PackingServiceGrpc.class)
public class PackingGrpcServer extends PackingServiceGrpc.PackingServiceImplBase {

    @Override
    public void recordPacking(RecordPackingRequest request, StreamObserver<RecordPackingResponse> responseObserver) {
        responseObserver.onError(NotPorted.error("epl.outbound.v1.PackingService/RecordPacking", "EPL-OB-410W", "EPL-OB-410H"));
    }

    @Override
    public void listCartons(ListCartonsRequest request, StreamObserver<ListCartonsResponse> responseObserver) {
        responseObserver.onError(NotPorted.error("epl.outbound.v1.PackingService/ListCartons", "EPL-OB-420W", "EPL-OB-420H"));
    }
}

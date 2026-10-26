package com.epl.outbound.grpc;

import com.epl.outbound.v1.AllocateRequest;
import com.epl.outbound.v1.AllocateResponse;
import com.epl.outbound.v1.AllocationServiceGrpc;
import com.epl.outbound.v1.CancelAllocationRequest;
import com.epl.grpc.server.models.annotations.GrpcServer;
import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;

/**
 * gRPC endpoint epl.outbound.v1.AllocationService (contract: proto/src/main/proto/epl/outbound/v1/outbound.proto).
 * Every RPC answers UNIMPLEMENTED, naming the screens that call it, until it is implemented.
 */
@GrpcServer(AllocationServiceGrpc.class)
public class AllocationGrpcServer extends AllocationServiceGrpc.AllocationServiceImplBase {

    @Override
    public void allocate(AllocateRequest request, StreamObserver<AllocateResponse> responseObserver) {
        responseObserver.onError(NotPorted.error("epl.outbound.v1.AllocationService/Allocate", "EPL-OB-210W"));
    }

    @Override
    public void cancelAllocation(CancelAllocationRequest request, StreamObserver<Empty> responseObserver) {
        responseObserver.onError(NotPorted.error("epl.outbound.v1.AllocationService/CancelAllocation", "EPL-OB-210W"));
    }
}

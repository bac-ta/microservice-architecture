package com.example.epl.outbound.grpc;

import com.example.epl.outbound.v1.AllocateRequest;
import com.example.epl.outbound.v1.AllocateResponse;
import com.example.epl.outbound.v1.AllocationServiceGrpc;
import com.example.epl.outbound.v1.CancelAllocationRequest;
import com.example.grpc.server.models.annotations.GrpcServer;
import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;

/**
 * gRPC endpoint epl.outbound.v1.AllocationService (contract: proto/src/main/proto/epl/outbound/v1/outbound.proto).
 * Every RPC answers UNIMPLEMENTED until the legacy logic named in its Javadoc is ported.
 */
@GrpcServer(AllocationServiceGrpc.class)
public class AllocationGrpcServer extends AllocationServiceGrpc.AllocationServiceImplBase {

    /**
     * Legacy: gwh_spalc_010 search / register (allocate available stock to order lines)
     */
    @Override
    public void allocate(AllocateRequest request, StreamObserver<AllocateResponse> responseObserver) {
        responseObserver.onError(NotPorted.error("epl.outbound.v1.AllocationService/Allocate", "gwh_spalc_010 search / register (allocate available stock to order lines)"));
    }

    /**
     * Legacy: gwh_spalc_010 cancel / back
     */
    @Override
    public void cancelAllocation(CancelAllocationRequest request, StreamObserver<Empty> responseObserver) {
        responseObserver.onError(NotPorted.error("epl.outbound.v1.AllocationService/CancelAllocation", "gwh_spalc_010 cancel / back"));
    }
}

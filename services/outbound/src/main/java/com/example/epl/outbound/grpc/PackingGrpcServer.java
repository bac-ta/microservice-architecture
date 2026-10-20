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
 * Every RPC answers UNIMPLEMENTED until the legacy logic named in its Javadoc is ported.
 */
@GrpcServer(PackingServiceGrpc.class)
public class PackingGrpcServer extends PackingServiceGrpc.PackingServiceImplBase {

    /**
     * Legacy: gwh_sppcc_010 (PC), gwh_sbpcc / gwh_shpcc (HHT carton entry incl. weight / volume)
     */
    @Override
    public void recordPacking(RecordPackingRequest request, StreamObserver<RecordPackingResponse> responseObserver) {
        responseObserver.onError(NotPorted.error("epl.outbound.v1.PackingService/RecordPacking", "gwh_sppcc_010 (PC), gwh_sbpcc / gwh_shpcc (HHT carton entry incl. weight / volume)"));
    }

    /**
     * Legacy: gwh_sppcb / gwh_sbpcb / gwh_shpcb (packing inquiry by carton)
     */
    @Override
    public void listCartons(ListCartonsRequest request, StreamObserver<ListCartonsResponse> responseObserver) {
        responseObserver.onError(NotPorted.error("epl.outbound.v1.PackingService/ListCartons", "gwh_sppcb / gwh_sbpcb / gwh_shpcb (packing inquiry by carton)"));
    }
}

package com.example.epl.masterdata.grpc;

import com.example.epl.masterdata.v1.ListPartiesRequest;
import com.example.epl.masterdata.v1.ListPartiesResponse;
import com.example.epl.masterdata.v1.PartyServiceGrpc;
import com.example.grpc.server.models.annotations.GrpcServer;
import io.grpc.stub.StreamObserver;

/**
 * gRPC endpoint epl.masterdata.v1.PartyService (contract: proto/src/main/proto/epl/masterdata/v1/masterdata.proto).
 * Every RPC answers UNIMPLEMENTED until the legacy logic named in its Javadoc is ported.
 */
@GrpcServer(PartyServiceGrpc.class)
public class PartyGrpcServer extends PartyServiceGrpc.PartyServiceImplBase {

    /**
     * Legacy: gwh_mpdlv_010, gwh_cpdlv (GWH_TM_DLV)
     */
    @Override
    public void listDeliveryDestinations(ListPartiesRequest request, StreamObserver<ListPartiesResponse> responseObserver) {
        responseObserver.onError(NotPorted.error("epl.masterdata.v1.PartyService/ListDeliveryDestinations", "gwh_mpdlv_010, gwh_cpdlv (GWH_TM_DLV)"));
    }

    /**
     * Legacy: gwh_mpspl_010, gwh_cpspl (GWH_TM_SPL)
     */
    @Override
    public void listSuppliers(ListPartiesRequest request, StreamObserver<ListPartiesResponse> responseObserver) {
        responseObserver.onError(NotPorted.error("epl.masterdata.v1.PartyService/ListSuppliers", "gwh_mpspl_010, gwh_cpspl (GWH_TM_SPL)"));
    }
}

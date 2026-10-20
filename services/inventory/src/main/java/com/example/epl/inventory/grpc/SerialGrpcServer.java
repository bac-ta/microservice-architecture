package com.example.epl.inventory.grpc;

import com.example.epl.inventory.v1.RegisterSerialsRequest;
import com.example.epl.inventory.v1.RegisterSerialsResponse;
import com.example.epl.inventory.v1.SearchSerialsRequest;
import com.example.epl.inventory.v1.SearchSerialsResponse;
import com.example.epl.inventory.v1.SerialServiceGrpc;
import com.example.grpc.server.models.annotations.GrpcServer;
import io.grpc.stub.StreamObserver;

/**
 * gRPC endpoint epl.inventory.v1.SerialService (contract: proto/src/main/proto/epl/inventory/v1/inventory.proto).
 * Every RPC answers UNIMPLEMENTED until the legacy logic named in its Javadoc is ported.
 */
@GrpcServer(SerialServiceGrpc.class)
public class SerialGrpcServer extends SerialServiceGrpc.SerialServiceImplBase {

    /**
     * Legacy: gwh_lpsri_010
     */
    @Override
    public void search(SearchSerialsRequest request, StreamObserver<SearchSerialsResponse> responseObserver) {
        responseObserver.onError(NotPorted.error("epl.inventory.v1.SerialService/Search", "gwh_lpsri_010"));
    }

    /**
     * Legacy: gwh_lpsre_010 / gwhLpsre010Reg, HHT gwh_lbsrl
     */
    @Override
    public void register(RegisterSerialsRequest request, StreamObserver<RegisterSerialsResponse> responseObserver) {
        responseObserver.onError(NotPorted.error("epl.inventory.v1.SerialService/Register", "gwh_lpsre_010 / gwhLpsre010Reg, HHT gwh_lbsrl"));
    }
}

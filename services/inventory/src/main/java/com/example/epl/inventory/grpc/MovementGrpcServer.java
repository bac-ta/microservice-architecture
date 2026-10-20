package com.example.epl.inventory.grpc;

import com.example.epl.inventory.v1.Movement;
import com.example.epl.inventory.v1.MovementServiceGrpc;
import com.example.epl.inventory.v1.SearchMovementsRequest;
import com.example.epl.inventory.v1.SearchMovementsResponse;
import com.example.grpc.server.models.annotations.GrpcServer;
import io.grpc.stub.StreamObserver;

/**
 * gRPC endpoint epl.inventory.v1.MovementService (contract: proto/src/main/proto/epl/inventory/v1/inventory.proto).
 * Every RPC answers UNIMPLEMENTED until the legacy logic named in its Javadoc is ported.
 */
@GrpcServer(MovementServiceGrpc.class)
public class MovementGrpcServer extends MovementServiceGrpc.MovementServiceImplBase {

    /**
     * Legacy: gwh_lpioh (by invoice / IO no), gwh_lpioc / gwh_lpios (by product), gwh_lpiop, gwh_lpiol (by
     * location)
     */
    @Override
    public void search(SearchMovementsRequest request, StreamObserver<SearchMovementsResponse> responseObserver) {
        responseObserver.onError(NotPorted.error("epl.inventory.v1.MovementService/Search", "gwh_lpioh (by invoice / IO no), gwh_lpioc / gwh_lpios (by product), gwh_lpiop, gwh_lpiol (by location)"));
    }

    /**
     * Legacy: gwh_lpioh / lpioc / lpiop *DwlBigData
     */
    @Override
    public void export(SearchMovementsRequest request, StreamObserver<Movement> responseObserver) {
        responseObserver.onError(NotPorted.error("epl.inventory.v1.MovementService/Export", "gwh_lpioh / lpioc / lpiop *DwlBigData"));
    }
}

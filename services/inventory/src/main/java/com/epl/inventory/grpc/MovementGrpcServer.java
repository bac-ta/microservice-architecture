package com.epl.inventory.grpc;

import com.epl.inventory.v1.Movement;
import com.epl.inventory.v1.MovementServiceGrpc;
import com.epl.inventory.v1.SearchMovementsRequest;
import com.epl.inventory.v1.SearchMovementsResponse;
import com.epl.grpc.server.models.annotations.GrpcServer;
import io.grpc.stub.StreamObserver;

/**
 * gRPC endpoint epl.inventory.v1.MovementService (contract: proto/src/main/proto/epl/inventory/v1/inventory.proto).
 * Every RPC answers UNIMPLEMENTED, naming the screens that call it, until it is implemented.
 */
@GrpcServer(MovementServiceGrpc.class)
public class MovementGrpcServer extends MovementServiceGrpc.MovementServiceImplBase {

    @Override
    public void search(SearchMovementsRequest request, StreamObserver<SearchMovementsResponse> responseObserver) {
        responseObserver.onError(NotPorted.error("epl.inventory.v1.MovementService/Search", "EPL-IV-210W"));
    }

    @Override
    public void export(SearchMovementsRequest request, StreamObserver<Movement> responseObserver) {
        responseObserver.onError(NotPorted.error("epl.inventory.v1.MovementService/Export", "EPL-IV-210W"));
    }
}

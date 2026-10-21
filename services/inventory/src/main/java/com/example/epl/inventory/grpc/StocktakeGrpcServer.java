package com.example.epl.inventory.grpc;

import com.example.epl.inventory.v1.CancelStocktakeRequest;
import com.example.epl.inventory.v1.ConfirmStocktakeRequest;
import com.example.epl.inventory.v1.CreateStocktakeRequest;
import com.example.epl.inventory.v1.GetStocktakeRequest;
import com.example.epl.inventory.v1.ListStocktakesRequest;
import com.example.epl.inventory.v1.ListStocktakesResponse;
import com.example.epl.inventory.v1.RecordCountRequest;
import com.example.epl.inventory.v1.RecordCountResponse;
import com.example.epl.inventory.v1.Stocktake;
import com.example.epl.inventory.v1.StocktakeServiceGrpc;
import com.example.grpc.server.models.annotations.GrpcServer;
import io.grpc.stub.StreamObserver;

/**
 * gRPC endpoint epl.inventory.v1.StocktakeService (contract: proto/src/main/proto/epl/inventory/v1/inventory.proto).
 * Every RPC answers UNIMPLEMENTED, naming the screens that call it, until it is implemented.
 */
@GrpcServer(StocktakeServiceGrpc.class)
public class StocktakeGrpcServer extends StocktakeServiceGrpc.StocktakeServiceImplBase {

    @Override
    public void create(CreateStocktakeRequest request, StreamObserver<Stocktake> responseObserver) {
        responseObserver.onError(NotPorted.error("epl.inventory.v1.StocktakeService/Create", "EPL-IV-320W"));
    }

    @Override
    public void get(GetStocktakeRequest request, StreamObserver<Stocktake> responseObserver) {
        responseObserver.onError(NotPorted.error("epl.inventory.v1.StocktakeService/Get", "EPL-IV-310W"));
    }

    @Override
    public void list(ListStocktakesRequest request, StreamObserver<ListStocktakesResponse> responseObserver) {
        responseObserver.onError(NotPorted.error("epl.inventory.v1.StocktakeService/List", "EPL-IV-310W"));
    }

    @Override
    public void recordCount(RecordCountRequest request, StreamObserver<RecordCountResponse> responseObserver) {
        responseObserver.onError(NotPorted.error("epl.inventory.v1.StocktakeService/RecordCount", "EPL-IV-330W", "EPL-IV-330H"));
    }

    @Override
    public void confirm(ConfirmStocktakeRequest request, StreamObserver<Stocktake> responseObserver) {
        responseObserver.onError(NotPorted.error("epl.inventory.v1.StocktakeService/Confirm", "EPL-IV-340W"));
    }

    @Override
    public void cancel(CancelStocktakeRequest request, StreamObserver<Stocktake> responseObserver) {
        responseObserver.onError(NotPorted.error("epl.inventory.v1.StocktakeService/Cancel", "EPL-IV-340W"));
    }
}

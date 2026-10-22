package com.example.epl.inventory.grpc;

import com.example.epl.inventory.services.StocktakeService;
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
import com.example.epl.inventory.v1.UpdateStocktakeRequest;
import com.example.grpc.server.models.annotations.GrpcServer;
import com.example.service.core.GrpcCalls;
import io.grpc.stub.StreamObserver;

/**
 * gRPC endpoint epl.inventory.v1.StocktakeService (contract: proto/src/main/proto/epl/inventory/v1/inventory.proto).
 * Create, Get, List, Update and Cancel are implemented; RecordCount and Confirm (stock operations)
 * answer UNIMPLEMENTED, naming their screens, until they are.
 */
@GrpcServer(StocktakeServiceGrpc.class)
public class StocktakeGrpcServer extends StocktakeServiceGrpc.StocktakeServiceImplBase {

    private final StocktakeService stocktakeService;

    public StocktakeGrpcServer(StocktakeService stocktakeService) {
        this.stocktakeService = stocktakeService;
    }

    @Override
    public void create(CreateStocktakeRequest request, StreamObserver<Stocktake> responseObserver) {
        GrpcCalls.unary(responseObserver, () -> stocktakeService.create(request.getStocktake()));
    }

    @Override
    public void get(GetStocktakeRequest request, StreamObserver<Stocktake> responseObserver) {
        GrpcCalls.unary(responseObserver, () -> stocktakeService.get(request.getStocktakeNo(), request.getOnlyDifferences()));
    }

    @Override
    public void list(ListStocktakesRequest request, StreamObserver<ListStocktakesResponse> responseObserver) {
        GrpcCalls.unary(responseObserver, () -> stocktakeService.list(request));
    }

    @Override
    public void update(UpdateStocktakeRequest request, StreamObserver<Stocktake> responseObserver) {
        GrpcCalls.unary(responseObserver, () -> stocktakeService.update(request.getStocktake()));
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
        GrpcCalls.unary(responseObserver, () -> stocktakeService.cancel(request.getStocktakeNo(), request.getVersion()));
    }
}

package com.example.epl.inventory.grpc;

import com.example.epl.inventory.v1.AdjustStockRequest;
import com.example.epl.inventory.v1.ChangePickKeyRequest;
import com.example.epl.inventory.v1.ChangeSubInventoryRequest;
import com.example.epl.inventory.v1.DivideStockRequest;
import com.example.epl.inventory.v1.MovementResult;
import com.example.epl.inventory.v1.RelocateStockRequest;
import com.example.epl.inventory.v1.SearchStockRequest;
import com.example.epl.inventory.v1.SearchStockResponse;
import com.example.epl.inventory.v1.StockLot;
import com.example.epl.inventory.v1.StockServiceGrpc;
import com.example.grpc.server.models.annotations.GrpcServer;
import io.grpc.stub.StreamObserver;

/**
 * gRPC endpoint epl.inventory.v1.StockService (contract: proto/src/main/proto/epl/inventory/v1/inventory.proto).
 * Every RPC answers UNIMPLEMENTED, naming the screens that call it, until it is implemented.
 */
@GrpcServer(StockServiceGrpc.class)
public class StockGrpcServer extends StockServiceGrpc.StockServiceImplBase {

    @Override
    public void search(SearchStockRequest request, StreamObserver<SearchStockResponse> responseObserver) {
        responseObserver.onError(NotPorted.error("epl.inventory.v1.StockService/Search", "EPL-IV-110W", "EPL-IV-110H"));
    }

    @Override
    public void adjust(AdjustStockRequest request, StreamObserver<MovementResult> responseObserver) {
        responseObserver.onError(NotPorted.error("epl.inventory.v1.StockService/Adjust", "EPL-IV-120W"));
    }

    @Override
    public void relocate(RelocateStockRequest request, StreamObserver<MovementResult> responseObserver) {
        responseObserver.onError(NotPorted.error("epl.inventory.v1.StockService/Relocate", "EPL-IV-130W", "EPL-IV-130H"));
    }

    @Override
    public void changePickKey(ChangePickKeyRequest request, StreamObserver<MovementResult> responseObserver) {
        responseObserver.onError(NotPorted.error("epl.inventory.v1.StockService/ChangePickKey", "EPL-IV-140W", "EPL-IV-140H"));
    }

    @Override
    public void changeSubInventory(ChangeSubInventoryRequest request, StreamObserver<MovementResult> responseObserver) {
        responseObserver.onError(NotPorted.error("epl.inventory.v1.StockService/ChangeSubInventory", "EPL-IV-150W"));
    }

    @Override
    public void divide(DivideStockRequest request, StreamObserver<MovementResult> responseObserver) {
        responseObserver.onError(NotPorted.error("epl.inventory.v1.StockService/Divide", "EPL-IV-160W"));
    }

    @Override
    public void export(SearchStockRequest request, StreamObserver<StockLot> responseObserver) {
        responseObserver.onError(NotPorted.error("epl.inventory.v1.StockService/Export", "EPL-IV-110W"));
    }
}

package com.epl.inventory.grpc;

import com.epl.inventory.services.StockQueryService;
import com.epl.inventory.v1.AdjustStockRequest;
import com.epl.inventory.v1.ChangePickKeyRequest;
import com.epl.inventory.v1.ChangeSubInventoryRequest;
import com.epl.inventory.v1.DivideStockRequest;
import com.epl.inventory.v1.GetStockRequest;
import com.epl.inventory.v1.MovementResult;
import com.epl.inventory.v1.RelocateStockRequest;
import com.epl.inventory.v1.SearchStockRequest;
import com.epl.inventory.v1.SearchStockResponse;
import com.epl.inventory.v1.StockLot;
import com.epl.inventory.v1.StockServiceGrpc;
import com.epl.grpc.server.models.annotations.GrpcServer;
import com.epl.service.core.GrpcCalls;
import io.grpc.stub.StreamObserver;

/**
 * gRPC endpoint epl.inventory.v1.StockService (contract: proto/src/main/proto/epl/inventory/v1/inventory.proto).
 * Search and Get are implemented; the stock operations answer UNIMPLEMENTED, naming their screens, until they are.
 */
@GrpcServer(StockServiceGrpc.class)
public class StockGrpcServer extends StockServiceGrpc.StockServiceImplBase {

    private final StockQueryService stockQueryService;

    public StockGrpcServer(StockQueryService stockQueryService) {
        this.stockQueryService = stockQueryService;
    }

    @Override
    public void search(SearchStockRequest request, StreamObserver<SearchStockResponse> responseObserver) {
        GrpcCalls.unary(responseObserver, () -> stockQueryService.search(request));
    }

    @Override
    public void get(GetStockRequest request, StreamObserver<StockLot> responseObserver) {
        GrpcCalls.unary(responseObserver, () -> stockQueryService.get(request.getId()));
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

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
 * Every RPC answers UNIMPLEMENTED until the legacy logic named in its Javadoc is ported.
 */
@GrpcServer(StockServiceGrpc.class)
public class StockGrpcServer extends StockServiceGrpc.StockServiceImplBase {

    /**
     * Legacy: gwh_lpadj_010 / getGwhTjST, HHT stock inquiry (gwh_lb*)
     */
    @Override
    public void search(SearchStockRequest request, StreamObserver<SearchStockResponse> responseObserver) {
        responseObserver.onError(NotPorted.error("epl.inventory.v1.StockService/Search", "gwh_lpadj_010 / getGwhTjST, HHT stock inquiry (gwh_lb*)"));
    }

    /**
     * Legacy: gwh_lpadj_010 / gwhLpadj010Reg (writes GWH_TJ_UJ_R)
     */
    @Override
    public void adjust(AdjustStockRequest request, StreamObserver<MovementResult> responseObserver) {
        responseObserver.onError(NotPorted.error("epl.inventory.v1.StockService/Adjust", "gwh_lpadj_010 / gwhLpadj010Reg (writes GWH_TJ_UJ_R)"));
    }

    /**
     * Legacy: gwh_lprls_* / gwh_lprla_* (PC), gwh_lbrls / gwh_lhrls (HHT); writes GWH_TJ_TR
     */
    @Override
    public void relocate(RelocateStockRequest request, StreamObserver<MovementResult> responseObserver) {
        responseObserver.onError(NotPorted.error("epl.inventory.v1.StockService/Relocate", "gwh_lprls_* / gwh_lprla_* (PC), gwh_lbrls / gwh_lhrls (HHT); writes GWH_TJ_TR"));
    }

    /**
     * Legacy: gwh_lprlc_010, gwh_lbrlc / gwh_lhrlc (cargo ID / pick key change, lock/unlock)
     */
    @Override
    public void changePickKey(ChangePickKeyRequest request, StreamObserver<MovementResult> responseObserver) {
        responseObserver.onError(NotPorted.error("epl.inventory.v1.StockService/ChangePickKey", "gwh_lprlc_010, gwh_lbrlc / gwh_lhrlc (cargo ID / pick key change, lock/unlock)"));
    }

    /**
     * Legacy: gwh_lpsbc_010 (sub-inventory change, transaction kind I1)
     */
    @Override
    public void changeSubInventory(ChangeSubInventoryRequest request, StreamObserver<MovementResult> responseObserver) {
        responseObserver.onError(NotPorted.error("epl.inventory.v1.StockService/ChangeSubInventory", "gwh_lpsbc_010 (sub-inventory change, transaction kind I1)"));
    }

    /**
     * Legacy: gwh_lpdiv_010 (divide cargo ID)
     */
    @Override
    public void divide(DivideStockRequest request, StreamObserver<MovementResult> responseObserver) {
        responseObserver.onError(NotPorted.error("epl.inventory.v1.StockService/Divide", "gwh_lpdiv_010 (divide cargo ID)"));
    }

    /**
     * Legacy: gwh_lpski_* / *DwlBigData — streamed instead of building a file in memory
     */
    @Override
    public void export(SearchStockRequest request, StreamObserver<StockLot> responseObserver) {
        responseObserver.onError(NotPorted.error("epl.inventory.v1.StockService/Export", "gwh_lpski_* / *DwlBigData — streamed instead of building a file in memory"));
    }
}

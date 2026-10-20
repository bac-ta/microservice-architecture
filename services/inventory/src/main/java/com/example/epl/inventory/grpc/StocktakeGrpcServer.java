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
 * Every RPC answers UNIMPLEMENTED until the legacy logic named in its Javadoc is ported.
 */
@GrpcServer(StocktakeServiceGrpc.class)
public class StocktakeGrpcServer extends StocktakeServiceGrpc.StocktakeServiceImplBase {

    /**
     * Legacy: gwh_lptki_040 / gwhLptki040Ini (instruction)
     */
    @Override
    public void create(CreateStocktakeRequest request, StreamObserver<Stocktake> responseObserver) {
        responseObserver.onError(NotPorted.error("epl.inventory.v1.StocktakeService/Create", "gwh_lptki_040 / gwhLptki040Ini (instruction)"));
    }

    /**
     * Legacy: gwh_lptki_010..050 inquiry
     */
    @Override
    public void get(GetStocktakeRequest request, StreamObserver<Stocktake> responseObserver) {
        responseObserver.onError(NotPorted.error("epl.inventory.v1.StocktakeService/Get", "gwh_lptki_010..050 inquiry"));
    }

    /**
     * Legacy: new in EPL
     */
    @Override
    public void list(ListStocktakesRequest request, StreamObserver<ListStocktakesResponse> responseObserver) {
        responseObserver.onError(NotPorted.error("epl.inventory.v1.StocktakeService/List", "new in EPL"));
    }

    /**
     * Legacy: gwh_lptki_060..080, gwh_lptks_010, HHT gwh_lbtki / gwh_lbski
     */
    @Override
    public void recordCount(RecordCountRequest request, StreamObserver<RecordCountResponse> responseObserver) {
        responseObserver.onError(NotPorted.error("epl.inventory.v1.StocktakeService/RecordCount", "gwh_lptki_060..080, gwh_lptks_010, HHT gwh_lbtki / gwh_lbski"));
    }

    /**
     * Legacy: gwh_lptki_010 / gwhLptki010Cnf + gwh_lptku_010 (update stock)
     */
    @Override
    public void confirm(ConfirmStocktakeRequest request, StreamObserver<Stocktake> responseObserver) {
        responseObserver.onError(NotPorted.error("epl.inventory.v1.StocktakeService/Confirm", "gwh_lptki_010 / gwhLptki010Cnf + gwh_lptku_010 (update stock)"));
    }

    /**
     * Legacy: gwh_lptki_010 / gwhLptki010Cnc
     */
    @Override
    public void cancel(CancelStocktakeRequest request, StreamObserver<Stocktake> responseObserver) {
        responseObserver.onError(NotPorted.error("epl.inventory.v1.StocktakeService/Cancel", "gwh_lptki_010 / gwhLptki010Cnc"));
    }
}

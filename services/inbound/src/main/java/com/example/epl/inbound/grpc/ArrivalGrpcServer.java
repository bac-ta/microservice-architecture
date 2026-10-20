package com.example.epl.inbound.grpc;

import com.example.epl.inbound.v1.Arrival;
import com.example.epl.inbound.v1.ArrivalServiceGrpc;
import com.example.epl.inbound.v1.CancelArrivalRequest;
import com.example.epl.inbound.v1.ConfirmArrivalRequest;
import com.example.epl.inbound.v1.CreateArrivalRequest;
import com.example.epl.inbound.v1.GetArrivalRequest;
import com.example.epl.inbound.v1.RecordInspectionRequest;
import com.example.epl.inbound.v1.RecordInspectionResponse;
import com.example.epl.inbound.v1.RecordPutawayRequest;
import com.example.epl.inbound.v1.RecordPutawayResponse;
import com.example.epl.inbound.v1.RegisterSerialsRequest;
import com.example.epl.inbound.v1.SearchArrivalsRequest;
import com.example.epl.inbound.v1.SearchArrivalsResponse;
import com.example.epl.inbound.v1.SendArrivalResultRequest;
import com.example.grpc.server.models.annotations.GrpcServer;
import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;

/**
 * gRPC endpoint epl.inbound.v1.ArrivalService (contract: proto/src/main/proto/epl/inbound/v1/inbound.proto).
 * Every RPC answers UNIMPLEMENTED until the legacy logic named in its Javadoc is ported.
 */
@GrpcServer(ArrivalServiceGrpc.class)
public class ArrivalGrpcServer extends ArrivalServiceGrpc.ArrivalServiceImplBase {

    /**
     * Legacy: gwh_apent_010 (PC), gwh_abent (HHT unplanned arrival), EDI via integration
     */
    @Override
    public void createArrival(CreateArrivalRequest request, StreamObserver<Arrival> responseObserver) {
        responseObserver.onError(NotPorted.error("epl.inbound.v1.ArrivalService/CreateArrival", "gwh_apent_010 (PC), gwh_abent (HHT unplanned arrival), EDI via integration"));
    }

    /**
     * Legacy: gwh_apinq_010 / gwh_abinq (HHT)
     */
    @Override
    public void getArrival(GetArrivalRequest request, StreamObserver<Arrival> responseObserver) {
        responseObserver.onError(NotPorted.error("epl.inbound.v1.ArrivalService/GetArrival", "gwh_apinq_010 / gwh_abinq (HHT)"));
    }

    /**
     * Legacy: gwh_apinq_010 search
     */
    @Override
    public void searchArrivals(SearchArrivalsRequest request, StreamObserver<SearchArrivalsResponse> responseObserver) {
        responseObserver.onError(NotPorted.error("epl.inbound.v1.ArrivalService/SearchArrivals", "gwh_apinq_010 search"));
    }

    /**
     * Legacy: gwh_apent_010 correction / delete
     */
    @Override
    public void cancelArrival(CancelArrivalRequest request, StreamObserver<Arrival> responseObserver) {
        responseObserver.onError(NotPorted.error("epl.inbound.v1.ArrivalService/CancelArrival", "gwh_apent_010 correction / delete"));
    }

    /**
     * Legacy: gwh_apins_010 (PC), gwh_abins / gwh_ahins (HHT) — inspection results per line
     */
    @Override
    public void recordInspection(RecordInspectionRequest request, StreamObserver<RecordInspectionResponse> responseObserver) {
        responseObserver.onError(NotPorted.error("epl.inbound.v1.ArrivalService/RecordInspection", "gwh_apins_010 (PC), gwh_abins / gwh_ahins (HHT) — inspection results per line"));
    }

    /**
     * Legacy: gwh_abloc / gwh_ahloc (HHT put-away: cargo ID → location)
     */
    @Override
    public void recordPutaway(RecordPutawayRequest request, StreamObserver<RecordPutawayResponse> responseObserver) {
        responseObserver.onError(NotPorted.error("epl.inbound.v1.ArrivalService/RecordPutaway", "gwh_abloc / gwh_ahloc (HHT put-away: cargo ID → location)"));
    }

    /**
     * Legacy: gwh_apcnf_010 — confirm, creates stock lots
     */
    @Override
    public void confirmArrival(ConfirmArrivalRequest request, StreamObserver<Arrival> responseObserver) {
        responseObserver.onError(NotPorted.error("epl.inbound.v1.ArrivalService/ConfirmArrival", "gwh_apcnf_010 — confirm, creates stock lots"));
    }

    /**
     * Legacy: gwh_apsrl_010 — serial numbers captured on receipt
     */
    @Override
    public void registerSerials(RegisterSerialsRequest request, StreamObserver<Empty> responseObserver) {
        responseObserver.onError(NotPorted.error("epl.inbound.v1.ArrivalService/RegisterSerials", "gwh_apsrl_010 — serial numbers captured on receipt"));
    }

    /**
     * Legacy: gwh_apscf_010 — send arrival result to the customer (EDI / CSV / mail)
     */
    @Override
    public void sendArrivalResult(SendArrivalResultRequest request, StreamObserver<Empty> responseObserver) {
        responseObserver.onError(NotPorted.error("epl.inbound.v1.ArrivalService/SendArrivalResult", "gwh_apscf_010 — send arrival result to the customer (EDI / CSV / mail)"));
    }
}

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
 * Every RPC answers UNIMPLEMENTED, naming the screens that call it, until it is implemented.
 */
@GrpcServer(ArrivalServiceGrpc.class)
public class ArrivalGrpcServer extends ArrivalServiceGrpc.ArrivalServiceImplBase {

    @Override
    public void createArrival(CreateArrivalRequest request, StreamObserver<Arrival> responseObserver) {
        responseObserver.onError(NotPorted.error("epl.inbound.v1.ArrivalService/CreateArrival", "EPL-IB-110W", "EPL-IB-110H"));
    }

    @Override
    public void getArrival(GetArrivalRequest request, StreamObserver<Arrival> responseObserver) {
        responseObserver.onError(NotPorted.error("epl.inbound.v1.ArrivalService/GetArrival", "EPL-IB-120W", "EPL-IB-120H"));
    }

    @Override
    public void searchArrivals(SearchArrivalsRequest request, StreamObserver<SearchArrivalsResponse> responseObserver) {
        responseObserver.onError(NotPorted.error("epl.inbound.v1.ArrivalService/SearchArrivals", "EPL-IB-120W", "EPL-IB-120H"));
    }

    @Override
    public void cancelArrival(CancelArrivalRequest request, StreamObserver<Arrival> responseObserver) {
        responseObserver.onError(NotPorted.error("epl.inbound.v1.ArrivalService/CancelArrival", "EPL-IB-110W"));
    }

    @Override
    public void recordInspection(RecordInspectionRequest request, StreamObserver<RecordInspectionResponse> responseObserver) {
        responseObserver.onError(NotPorted.error("epl.inbound.v1.ArrivalService/RecordInspection", "EPL-IB-210W", "EPL-IB-210H"));
    }

    @Override
    public void recordPutaway(RecordPutawayRequest request, StreamObserver<RecordPutawayResponse> responseObserver) {
        responseObserver.onError(NotPorted.error("epl.inbound.v1.ArrivalService/RecordPutaway", "EPL-IB-220H"));
    }

    @Override
    public void confirmArrival(ConfirmArrivalRequest request, StreamObserver<Arrival> responseObserver) {
        responseObserver.onError(NotPorted.error("epl.inbound.v1.ArrivalService/ConfirmArrival", "EPL-IB-310W"));
    }

    @Override
    public void registerSerials(RegisterSerialsRequest request, StreamObserver<Empty> responseObserver) {
        responseObserver.onError(NotPorted.error("epl.inbound.v1.ArrivalService/RegisterSerials", "EPL-IB-230W"));
    }

    @Override
    public void sendArrivalResult(SendArrivalResultRequest request, StreamObserver<Empty> responseObserver) {
        responseObserver.onError(NotPorted.error("epl.inbound.v1.ArrivalService/SendArrivalResult", "EPL-IB-320W"));
    }
}

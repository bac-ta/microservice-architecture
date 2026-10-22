package com.example.epl.inbound.grpc;

import com.example.epl.inbound.services.ArrivalService;
import com.example.epl.inbound.v1.Arrival;
import com.example.epl.inbound.v1.ArrivalServiceGrpc;
import com.example.epl.inbound.v1.CancelArrivalRequest;
import com.example.epl.inbound.v1.ConfirmArrivalRequest;
import com.example.epl.inbound.v1.CreateArrivalRequest;
import com.example.epl.inbound.v1.DeleteArrivalRequest;
import com.example.epl.inbound.v1.GetArrivalRequest;
import com.example.epl.inbound.v1.RecordInspectionRequest;
import com.example.epl.inbound.v1.RecordInspectionResponse;
import com.example.epl.inbound.v1.RecordPutawayRequest;
import com.example.epl.inbound.v1.RecordPutawayResponse;
import com.example.epl.inbound.v1.RegisterSerialsRequest;
import com.example.epl.inbound.v1.SearchArrivalsRequest;
import com.example.epl.inbound.v1.SearchArrivalsResponse;
import com.example.epl.inbound.v1.SendArrivalResultRequest;
import com.example.epl.inbound.v1.UpdateArrivalRequest;
import com.example.grpc.server.models.annotations.GrpcServer;
import com.example.service.core.GrpcCalls;
import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;

/**
 * gRPC endpoint epl.inbound.v1.ArrivalService (contract: proto/src/main/proto/epl/inbound/v1/inbound.proto).
 * Arrival CRUD is implemented; inspection, put-away, confirmation and result sending answer
 * UNIMPLEMENTED, naming their screens, until they are.
 */
@GrpcServer(ArrivalServiceGrpc.class)
public class ArrivalGrpcServer extends ArrivalServiceGrpc.ArrivalServiceImplBase {

    private final ArrivalService arrivalService;

    public ArrivalGrpcServer(ArrivalService arrivalService) {
        this.arrivalService = arrivalService;
    }

    @Override
    public void createArrival(CreateArrivalRequest request, StreamObserver<Arrival> responseObserver) {
        GrpcCalls.unary(responseObserver, () -> arrivalService.create(request.getArrival()));
    }

    @Override
    public void getArrival(GetArrivalRequest request, StreamObserver<Arrival> responseObserver) {
        GrpcCalls.unary(responseObserver, () -> arrivalService.get(request.getArrivalNo()));
    }

    @Override
    public void searchArrivals(SearchArrivalsRequest request, StreamObserver<SearchArrivalsResponse> responseObserver) {
        GrpcCalls.unary(responseObserver, () -> arrivalService.search(request));
    }

    @Override
    public void updateArrival(UpdateArrivalRequest request, StreamObserver<Arrival> responseObserver) {
        GrpcCalls.unary(responseObserver, () -> arrivalService.update(request.getArrival()));
    }

    @Override
    public void cancelArrival(CancelArrivalRequest request, StreamObserver<Arrival> responseObserver) {
        GrpcCalls.unary(responseObserver, () -> arrivalService.cancel(request.getArrivalNo(), request.getReasonCode(), request.getVersion()));
    }

    @Override
    public void deleteArrival(DeleteArrivalRequest request, StreamObserver<Empty> responseObserver) {
        GrpcCalls.unaryEmpty(responseObserver, () -> arrivalService.delete(request.getArrivalNo(), request.getVersion()));
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

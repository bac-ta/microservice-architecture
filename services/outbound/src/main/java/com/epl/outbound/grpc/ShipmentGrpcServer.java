package com.epl.outbound.grpc;

import com.epl.outbound.services.ShipmentService;
import com.epl.outbound.v1.CancelShipmentRequest;
import com.epl.outbound.v1.ConfirmShipmentRequest;
import com.epl.outbound.v1.ConfirmShipmentResponse;
import com.epl.outbound.v1.CreateShipmentRequest;
import com.epl.outbound.v1.DeleteShipmentRequest;
import com.epl.outbound.v1.GetShipmentRequest;
import com.epl.outbound.v1.SearchShipmentsRequest;
import com.epl.outbound.v1.SearchShipmentsResponse;
import com.epl.outbound.v1.SendShipmentResultRequest;
import com.epl.outbound.v1.Shipment;
import com.epl.outbound.v1.ShipmentServiceGrpc;
import com.epl.outbound.v1.UpdateShipmentRequest;
import com.epl.grpc.server.models.annotations.GrpcServer;
import com.epl.service.core.GrpcCalls;
import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;

/**
 * gRPC endpoint epl.outbound.v1.ShipmentService (contract: proto/src/main/proto/epl/outbound/v1/outbound.proto).
 * Shipment CRUD is implemented; confirmation and result sending answer UNIMPLEMENTED,
 * naming their screens, until they are.
 */
@RequiredArgsConstructor
@GrpcServer(ShipmentServiceGrpc.class)
public class ShipmentGrpcServer extends ShipmentServiceGrpc.ShipmentServiceImplBase {

    private final ShipmentService shipmentService;

    @Override
    public void createShipment(CreateShipmentRequest request, StreamObserver<Shipment> responseObserver) {
        GrpcCalls.unary(responseObserver, () -> shipmentService.create(request.getShipment()));
    }

    @Override
    public void getShipment(GetShipmentRequest request, StreamObserver<Shipment> responseObserver) {
        GrpcCalls.unary(responseObserver, () -> shipmentService.get(request.getShipmentNo()));
    }

    @Override
    public void searchShipments(SearchShipmentsRequest request, StreamObserver<SearchShipmentsResponse> responseObserver) {
        GrpcCalls.unary(responseObserver, () -> shipmentService.search(request));
    }

    @Override
    public void updateShipment(UpdateShipmentRequest request, StreamObserver<Shipment> responseObserver) {
        GrpcCalls.unary(responseObserver, () -> shipmentService.update(request.getShipment()));
    }

    @Override
    public void cancelShipment(CancelShipmentRequest request, StreamObserver<Shipment> responseObserver) {
        GrpcCalls.unary(responseObserver, () -> shipmentService.cancel(request.getShipmentNo(), request.getReasonCode(), request.getVersion()));
    }

    @Override
    public void deleteShipment(DeleteShipmentRequest request, StreamObserver<Empty> responseObserver) {
        GrpcCalls.unaryEmpty(responseObserver, () -> shipmentService.delete(request.getShipmentNo(), request.getVersion()));
    }

    @Override
    public void confirmShipment(ConfirmShipmentRequest request, StreamObserver<ConfirmShipmentResponse> responseObserver) {
        responseObserver.onError(NotPorted.error("epl.outbound.v1.ShipmentService/ConfirmShipment", "EPL-OB-510W"));
    }

    @Override
    public void sendShipmentResult(SendShipmentResultRequest request, StreamObserver<Empty> responseObserver) {
        responseObserver.onError(NotPorted.error("epl.outbound.v1.ShipmentService/SendShipmentResult", "EPL-OB-520W"));
    }
}

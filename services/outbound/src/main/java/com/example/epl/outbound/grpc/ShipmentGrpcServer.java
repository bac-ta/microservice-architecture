package com.example.epl.outbound.grpc;

import com.example.epl.outbound.services.ShipmentService;
import com.example.epl.outbound.v1.CancelShipmentRequest;
import com.example.epl.outbound.v1.ConfirmShipmentRequest;
import com.example.epl.outbound.v1.ConfirmShipmentResponse;
import com.example.epl.outbound.v1.CreateShipmentRequest;
import com.example.epl.outbound.v1.DeleteShipmentRequest;
import com.example.epl.outbound.v1.GetShipmentRequest;
import com.example.epl.outbound.v1.SearchShipmentsRequest;
import com.example.epl.outbound.v1.SearchShipmentsResponse;
import com.example.epl.outbound.v1.SendShipmentResultRequest;
import com.example.epl.outbound.v1.Shipment;
import com.example.epl.outbound.v1.ShipmentServiceGrpc;
import com.example.epl.outbound.v1.UpdateShipmentRequest;
import com.example.grpc.server.models.annotations.GrpcServer;
import com.example.service.core.GrpcCalls;
import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;

/**
 * gRPC endpoint epl.outbound.v1.ShipmentService (contract: proto/src/main/proto/epl/outbound/v1/outbound.proto).
 * Shipment CRUD is implemented; confirmation and result sending answer UNIMPLEMENTED,
 * naming their screens, until they are.
 */
@GrpcServer(ShipmentServiceGrpc.class)
public class ShipmentGrpcServer extends ShipmentServiceGrpc.ShipmentServiceImplBase {

    private final ShipmentService shipmentService;

    public ShipmentGrpcServer(ShipmentService shipmentService) {
        this.shipmentService = shipmentService;
    }

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

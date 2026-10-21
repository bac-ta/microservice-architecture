package com.example.epl.outbound.grpc;

import com.example.epl.outbound.v1.CancelShipmentRequest;
import com.example.epl.outbound.v1.ConfirmShipmentRequest;
import com.example.epl.outbound.v1.ConfirmShipmentResponse;
import com.example.epl.outbound.v1.CreateShipmentRequest;
import com.example.epl.outbound.v1.GetShipmentRequest;
import com.example.epl.outbound.v1.SearchShipmentsRequest;
import com.example.epl.outbound.v1.SearchShipmentsResponse;
import com.example.epl.outbound.v1.SendShipmentResultRequest;
import com.example.epl.outbound.v1.Shipment;
import com.example.epl.outbound.v1.ShipmentServiceGrpc;
import com.example.grpc.server.models.annotations.GrpcServer;
import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;

/**
 * gRPC endpoint epl.outbound.v1.ShipmentService (contract: proto/src/main/proto/epl/outbound/v1/outbound.proto).
 * Every RPC answers UNIMPLEMENTED, naming the screens that call it, until it is implemented.
 */
@GrpcServer(ShipmentServiceGrpc.class)
public class ShipmentGrpcServer extends ShipmentServiceGrpc.ShipmentServiceImplBase {

    @Override
    public void createShipment(CreateShipmentRequest request, StreamObserver<Shipment> responseObserver) {
        responseObserver.onError(NotPorted.error("epl.outbound.v1.ShipmentService/CreateShipment", "EPL-OB-110W"));
    }

    @Override
    public void getShipment(GetShipmentRequest request, StreamObserver<Shipment> responseObserver) {
        responseObserver.onError(NotPorted.error("epl.outbound.v1.ShipmentService/GetShipment", "EPL-OB-120W", "EPL-OB-120H"));
    }

    @Override
    public void searchShipments(SearchShipmentsRequest request, StreamObserver<SearchShipmentsResponse> responseObserver) {
        responseObserver.onError(NotPorted.error("epl.outbound.v1.ShipmentService/SearchShipments", "EPL-OB-120W", "EPL-OB-120H"));
    }

    @Override
    public void cancelShipment(CancelShipmentRequest request, StreamObserver<Shipment> responseObserver) {
        responseObserver.onError(NotPorted.error("epl.outbound.v1.ShipmentService/CancelShipment", "EPL-OB-110W"));
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

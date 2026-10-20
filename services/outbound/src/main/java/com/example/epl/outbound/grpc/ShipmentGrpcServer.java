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
 * Every RPC answers UNIMPLEMENTED until the legacy logic named in its Javadoc is ported.
 */
@GrpcServer(ShipmentServiceGrpc.class)
public class ShipmentGrpcServer extends ShipmentServiceGrpc.ShipmentServiceImplBase {

    /**
     * Legacy: gwh_spent_010, EDI / upload via integration
     */
    @Override
    public void createShipment(CreateShipmentRequest request, StreamObserver<Shipment> responseObserver) {
        responseObserver.onError(NotPorted.error("epl.outbound.v1.ShipmentService/CreateShipment", "gwh_spent_010, EDI / upload via integration"));
    }

    /**
     * Legacy: gwh_spinq_010 / gwh_sbinq (HHT)
     */
    @Override
    public void getShipment(GetShipmentRequest request, StreamObserver<Shipment> responseObserver) {
        responseObserver.onError(NotPorted.error("epl.outbound.v1.ShipmentService/GetShipment", "gwh_spinq_010 / gwh_sbinq (HHT)"));
    }

    /**
     * Legacy: gwh_spinq_010 search, gwh_sbiqb / gwh_sbiqc (HHT inquiry by batch / carton)
     */
    @Override
    public void searchShipments(SearchShipmentsRequest request, StreamObserver<SearchShipmentsResponse> responseObserver) {
        responseObserver.onError(NotPorted.error("epl.outbound.v1.ShipmentService/SearchShipments", "gwh_spinq_010 search, gwh_sbiqb / gwh_sbiqc (HHT inquiry by batch / carton)"));
    }

    /**
     * Legacy: gwh_spcnc_010 (cancel shipping order)
     */
    @Override
    public void cancelShipment(CancelShipmentRequest request, StreamObserver<Shipment> responseObserver) {
        responseObserver.onError(NotPorted.error("epl.outbound.v1.ShipmentService/CancelShipment", "gwh_spcnc_010 (cancel shipping order)"));
    }

    /**
     * Legacy: gwh_spcnf_010 (shipping confirmation: stock leaves the warehouse)
     */
    @Override
    public void confirmShipment(ConfirmShipmentRequest request, StreamObserver<ConfirmShipmentResponse> responseObserver) {
        responseObserver.onError(NotPorted.error("epl.outbound.v1.ShipmentService/ConfirmShipment", "gwh_spcnf_010 (shipping confirmation: stock leaves the warehouse)"));
    }

    /**
     * Legacy: gwh_spscf_010 (send result: EDI / CSV / mail)
     */
    @Override
    public void sendShipmentResult(SendShipmentResultRequest request, StreamObserver<Empty> responseObserver) {
        responseObserver.onError(NotPorted.error("epl.outbound.v1.ShipmentService/SendShipmentResult", "gwh_spscf_010 (send result: EDI / CSV / mail)"));
    }
}

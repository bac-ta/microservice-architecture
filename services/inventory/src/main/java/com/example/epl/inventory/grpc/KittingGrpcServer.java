package com.example.epl.inventory.grpc;

import com.example.epl.inventory.v1.CreateKittingOrderRequest;
import com.example.epl.inventory.v1.KittingOrder;
import com.example.epl.inventory.v1.KittingServiceGrpc;
import com.example.epl.inventory.v1.ReceiveKittingRequest;
import com.example.epl.inventory.v1.SendKittingConfirmationRequest;
import com.example.grpc.server.models.annotations.GrpcServer;
import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;

/**
 * gRPC endpoint epl.inventory.v1.KittingService (contract: proto/src/main/proto/epl/inventory/v1/inventory.proto).
 * Every RPC answers UNIMPLEMENTED until the legacy logic named in its Javadoc is ported.
 */
@GrpcServer(KittingServiceGrpc.class)
public class KittingGrpcServer extends KittingServiceGrpc.KittingServiceImplBase {

    /**
     * Legacy: gwh_lpkte_010 (kitting order = component shipment)
     */
    @Override
    public void createOrder(CreateKittingOrderRequest request, StreamObserver<KittingOrder> responseObserver) {
        responseObserver.onError(NotPorted.error("epl.inventory.v1.KittingService/CreateOrder", "gwh_lpkte_010 (kitting order = component shipment)"));
    }

    /**
     * Legacy: gwh_lpkta_020 (kit arrival = finished set receipt)
     */
    @Override
    public void receive(ReceiveKittingRequest request, StreamObserver<KittingOrder> responseObserver) {
        responseObserver.onError(NotPorted.error("epl.inventory.v1.KittingService/Receive", "gwh_lpkta_020 (kit arrival = finished set receipt)"));
    }

    /**
     * Legacy: gwh_lpkts_010 (send kitting confirmation)
     */
    @Override
    public void sendConfirmation(SendKittingConfirmationRequest request, StreamObserver<Empty> responseObserver) {
        responseObserver.onError(NotPorted.error("epl.inventory.v1.KittingService/SendConfirmation", "gwh_lpkts_010 (send kitting confirmation)"));
    }
}

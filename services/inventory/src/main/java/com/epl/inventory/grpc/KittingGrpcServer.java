package com.epl.inventory.grpc;

import com.epl.inventory.v1.CreateKittingOrderRequest;
import com.epl.inventory.v1.KittingOrder;
import com.epl.inventory.v1.KittingServiceGrpc;
import com.epl.inventory.v1.ReceiveKittingRequest;
import com.epl.inventory.v1.SendKittingConfirmationRequest;
import com.epl.grpc.server.models.annotations.GrpcServer;
import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;

/**
 * gRPC endpoint epl.inventory.v1.KittingService (contract: proto/src/main/proto/epl/inventory/v1/inventory.proto).
 * Every RPC answers UNIMPLEMENTED, naming the screens that call it, until it is implemented.
 */
@GrpcServer(KittingServiceGrpc.class)
public class KittingGrpcServer extends KittingServiceGrpc.KittingServiceImplBase {

    @Override
    public void createOrder(CreateKittingOrderRequest request, StreamObserver<KittingOrder> responseObserver) {
        responseObserver.onError(NotPorted.error("epl.inventory.v1.KittingService/CreateOrder", "EPL-IV-510W"));
    }

    @Override
    public void receive(ReceiveKittingRequest request, StreamObserver<KittingOrder> responseObserver) {
        responseObserver.onError(NotPorted.error("epl.inventory.v1.KittingService/Receive", "EPL-IV-520W"));
    }

    @Override
    public void sendConfirmation(SendKittingConfirmationRequest request, StreamObserver<Empty> responseObserver) {
        responseObserver.onError(NotPorted.error("epl.inventory.v1.KittingService/SendConfirmation", "EPL-IV-530W"));
    }
}

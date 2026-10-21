package com.example.epl.masterdata.grpc;

import com.example.epl.masterdata.v1.ListPartiesRequest;
import com.example.epl.masterdata.v1.ListPartiesResponse;
import com.example.epl.masterdata.v1.PartyServiceGrpc;
import com.example.grpc.server.models.annotations.GrpcServer;
import io.grpc.stub.StreamObserver;

/**
 * gRPC endpoint epl.masterdata.v1.PartyService (contract: proto/src/main/proto/epl/masterdata/v1/masterdata.proto).
 * Every RPC answers UNIMPLEMENTED, naming the screens that call it, until it is implemented.
 */
@GrpcServer(PartyServiceGrpc.class)
public class PartyGrpcServer extends PartyServiceGrpc.PartyServiceImplBase {

    @Override
    public void listDeliveryDestinations(ListPartiesRequest request, StreamObserver<ListPartiesResponse> responseObserver) {
        responseObserver.onError(NotPorted.error("epl.masterdata.v1.PartyService/ListDeliveryDestinations", "EPL-MD-310W"));
    }

    @Override
    public void listSuppliers(ListPartiesRequest request, StreamObserver<ListPartiesResponse> responseObserver) {
        responseObserver.onError(NotPorted.error("epl.masterdata.v1.PartyService/ListSuppliers", "EPL-MD-320W"));
    }
}

package com.example.epl.billing.grpc;

import com.example.epl.billing.v1.DeleteRateRequest;
import com.example.epl.billing.v1.ListRatesRequest;
import com.example.epl.billing.v1.ListRatesResponse;
import com.example.epl.billing.v1.Rate;
import com.example.epl.billing.v1.RateServiceGrpc;
import com.example.grpc.server.models.annotations.GrpcServer;
import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;

/**
 * gRPC endpoint epl.billing.v1.RateService (contract: proto/src/main/proto/epl/billing/v1/billing.proto).
 * Every RPC answers UNIMPLEMENTED, naming the screens that call it, until it is implemented.
 */
@GrpcServer(RateServiceGrpc.class)
public class RateGrpcServer extends RateServiceGrpc.RateServiceImplBase {

    @Override
    public void listRates(ListRatesRequest request, StreamObserver<ListRatesResponse> responseObserver) {
        responseObserver.onError(NotPorted.error("epl.billing.v1.RateService/ListRates", "EPL-BL-110W"));
    }

    @Override
    public void upsertRate(Rate request, StreamObserver<Rate> responseObserver) {
        responseObserver.onError(NotPorted.error("epl.billing.v1.RateService/UpsertRate", "EPL-BL-110W"));
    }

    @Override
    public void deleteRate(DeleteRateRequest request, StreamObserver<Empty> responseObserver) {
        responseObserver.onError(NotPorted.error("epl.billing.v1.RateService/DeleteRate", "EPL-BL-110W"));
    }
}

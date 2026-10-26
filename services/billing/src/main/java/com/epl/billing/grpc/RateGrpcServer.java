package com.epl.billing.grpc;

import com.epl.billing.services.RateService;
import com.epl.billing.v1.DeleteRateRequest;
import com.epl.billing.v1.GetRateRequest;
import com.epl.billing.v1.ListRatesRequest;
import com.epl.billing.v1.ListRatesResponse;
import com.epl.billing.v1.Rate;
import com.epl.billing.v1.RateServiceGrpc;
import com.epl.grpc.server.models.annotations.GrpcServer;
import com.epl.service.core.GrpcCalls;
import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;

/**
 * gRPC endpoint epl.billing.v1.RateService (contract: proto/src/main/proto/epl/billing/v1/billing.proto).
 */
@GrpcServer(RateServiceGrpc.class)
public class RateGrpcServer extends RateServiceGrpc.RateServiceImplBase {

    private final RateService rateService;

    public RateGrpcServer(RateService rateService) {
        this.rateService = rateService;
    }

    @Override
    public void listRates(ListRatesRequest request, StreamObserver<ListRatesResponse> responseObserver) {
        GrpcCalls.unary(responseObserver, () -> ListRatesResponse.newBuilder()
                .addAllRates(rateService.list(request.getChargeType(), request.getValidOn())).build());
    }

    @Override
    public void getRate(GetRateRequest request, StreamObserver<Rate> responseObserver) {
        GrpcCalls.unary(responseObserver, () -> rateService.get(request.getCode(), request.getValidOn()));
    }

    @Override
    public void upsertRate(Rate request, StreamObserver<Rate> responseObserver) {
        GrpcCalls.unary(responseObserver, () -> rateService.upsert(request));
    }

    @Override
    public void deleteRate(DeleteRateRequest request, StreamObserver<Empty> responseObserver) {
        GrpcCalls.unaryEmpty(responseObserver, () -> rateService.delete(request.getCode()));
    }
}

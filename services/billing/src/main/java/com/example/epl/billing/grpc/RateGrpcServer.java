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
 * Every RPC answers UNIMPLEMENTED until the legacy logic named in its Javadoc is ported.
 */
@GrpcServer(RateServiceGrpc.class)
public class RateGrpcServer extends RateServiceGrpc.RateServiceImplBase {

    /**
     * Legacy: gwh_mpwch / mpwrc / mpwrt (charge heads, rate conditions, rate tables)
     */
    @Override
    public void listRates(ListRatesRequest request, StreamObserver<ListRatesResponse> responseObserver) {
        responseObserver.onError(NotPorted.error("epl.billing.v1.RateService/ListRates", "gwh_mpwch / mpwrc / mpwrt (charge heads, rate conditions, rate tables)"));
    }

    /**
     * Legacy: new in EPL
     */
    @Override
    public void upsertRate(Rate request, StreamObserver<Rate> responseObserver) {
        responseObserver.onError(NotPorted.error("epl.billing.v1.RateService/UpsertRate", "new in EPL"));
    }

    /**
     * Legacy: new in EPL
     */
    @Override
    public void deleteRate(DeleteRateRequest request, StreamObserver<Empty> responseObserver) {
        responseObserver.onError(NotPorted.error("epl.billing.v1.RateService/DeleteRate", "new in EPL"));
    }
}

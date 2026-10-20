package com.example.epl.billing.grpc;

import com.example.epl.billing.v1.ChargeServiceGrpc;
import com.example.epl.billing.v1.ChargeStatement;
import com.example.epl.billing.v1.GetChargeStatementRequest;
import com.example.epl.billing.v1.UpdateChargeStatementRequest;
import com.example.grpc.server.models.annotations.GrpcServer;
import io.grpc.stub.StreamObserver;

/**
 * gRPC endpoint epl.billing.v1.ChargeService (contract: proto/src/main/proto/epl/billing/v1/billing.proto).
 * Every RPC answers UNIMPLEMENTED until the legacy logic named in its Javadoc is ported.
 */
@GrpcServer(ChargeServiceGrpc.class)
public class ChargeGrpcServer extends ChargeServiceGrpc.ChargeServiceImplBase {

    /**
     * Legacy: gwh_lpwpe_010..050 / *Get, *Inq (WSSP / WSPR)
     */
    @Override
    public void get(GetChargeStatementRequest request, StreamObserver<ChargeStatement> responseObserver) {
        responseObserver.onError(NotPorted.error("epl.billing.v1.ChargeService/Get", "gwh_lpwpe_010..050 / *Get, *Inq (WSSP / WSPR)"));
    }

    /**
     * Legacy: gwh_lpwpe_* / *Reg, *Mod, *Del
     */
    @Override
    public void update(UpdateChargeStatementRequest request, StreamObserver<ChargeStatement> responseObserver) {
        responseObserver.onError(NotPorted.error("epl.billing.v1.ChargeService/Update", "gwh_lpwpe_* / *Reg, *Mod, *Del"));
    }
}

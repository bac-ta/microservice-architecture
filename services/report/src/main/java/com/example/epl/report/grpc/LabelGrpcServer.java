package com.example.epl.report.grpc;

import com.example.epl.report.v1.LabelServiceGrpc;
import com.example.epl.report.v1.PrintLabelsRequest;
import com.example.epl.report.v1.PrintLabelsResponse;
import com.example.grpc.server.models.annotations.GrpcServer;
import io.grpc.stub.StreamObserver;

/**
 * gRPC endpoint epl.report.v1.LabelService (contract: proto/src/main/proto/epl/report/v1/report.proto).
 * Every RPC answers UNIMPLEMENTED until the legacy logic named in its Javadoc is ported.
 */
@GrpcServer(LabelServiceGrpc.class)
public class LabelGrpcServer extends LabelServiceGrpc.LabelServiceImplBase {

    /**
     * Legacy: gwh_rbcid (HHT cargo-ID label), gwh_rpcid / rplbp (PC labels), TMNCUPRTPRTPAIRMST
     */
    @Override
    public void printLabels(PrintLabelsRequest request, StreamObserver<PrintLabelsResponse> responseObserver) {
        responseObserver.onError(NotPorted.error("epl.report.v1.LabelService/PrintLabels", "gwh_rbcid (HHT cargo-ID label), gwh_rpcid / rplbp (PC labels), TMNCUPRTPRTPAIRMST"));
    }
}

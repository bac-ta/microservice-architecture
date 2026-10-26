package com.epl.report.grpc;

import com.epl.report.v1.LabelServiceGrpc;
import com.epl.report.v1.PrintLabelsRequest;
import com.epl.report.v1.PrintLabelsResponse;
import com.epl.grpc.server.models.annotations.GrpcServer;
import io.grpc.stub.StreamObserver;

/**
 * gRPC endpoint epl.report.v1.LabelService (contract: proto/src/main/proto/epl/report/v1/report.proto).
 * Every RPC answers UNIMPLEMENTED, naming the screens that call it, until it is implemented.
 */
@GrpcServer(LabelServiceGrpc.class)
public class LabelGrpcServer extends LabelServiceGrpc.LabelServiceImplBase {

    @Override
    public void printLabels(PrintLabelsRequest request, StreamObserver<PrintLabelsResponse> responseObserver) {
        responseObserver.onError(NotPorted.error("epl.report.v1.LabelService/PrintLabels", "EPL-RP-210W", "EPL-RP-210H"));
    }
}

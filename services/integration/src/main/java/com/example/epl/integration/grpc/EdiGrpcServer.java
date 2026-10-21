package com.example.epl.integration.grpc;

import com.example.epl.integration.v1.EdiMessage;
import com.example.epl.integration.v1.EdiServiceGrpc;
import com.example.epl.integration.v1.ResendEdiRequest;
import com.example.epl.integration.v1.SearchEdiRequest;
import com.example.epl.integration.v1.SearchEdiResponse;
import com.example.grpc.server.models.annotations.GrpcServer;
import io.grpc.stub.StreamObserver;

/**
 * gRPC endpoint epl.integration.v1.EdiService (contract: proto/src/main/proto/epl/integration/v1/integration.proto).
 * Every RPC answers UNIMPLEMENTED, naming the screens that call it, until it is implemented.
 */
@GrpcServer(EdiServiceGrpc.class)
public class EdiGrpcServer extends EdiServiceGrpc.EdiServiceImplBase {

    @Override
    public void search(SearchEdiRequest request, StreamObserver<SearchEdiResponse> responseObserver) {
        responseObserver.onError(NotPorted.error("epl.integration.v1.EdiService/Search", "EPL-IT-210W"));
    }

    @Override
    public void resend(ResendEdiRequest request, StreamObserver<EdiMessage> responseObserver) {
        responseObserver.onError(NotPorted.error("epl.integration.v1.EdiService/Resend", "EPL-IT-210W"));
    }
}

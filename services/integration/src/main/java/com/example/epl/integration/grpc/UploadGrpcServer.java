package com.example.epl.integration.grpc;

import com.example.epl.integration.v1.GetUploadErrorsRequest;
import com.example.epl.integration.v1.GetUploadErrorsResponse;
import com.example.epl.integration.v1.UploadChunk;
import com.example.epl.integration.v1.UploadResult;
import com.example.epl.integration.v1.UploadServiceGrpc;
import com.example.grpc.server.models.annotations.GrpcServer;
import io.grpc.stub.StreamObserver;

/**
 * gRPC endpoint epl.integration.v1.UploadService (contract: proto/src/main/proto/epl/integration/v1/integration.proto).
 * Every RPC answers UNIMPLEMENTED, naming the screens that call it, until it is implemented.
 */
@GrpcServer(UploadServiceGrpc.class)
public class UploadGrpcServer extends UploadServiceGrpc.UploadServiceImplBase {

    @Override
    public StreamObserver<UploadChunk> upload(StreamObserver<UploadResult> responseObserver) {
        responseObserver.onError(NotPorted.error("epl.integration.v1.UploadService/Upload", "EPL-IT-110W"));
        return NotPorted.ignoreRequests();
    }

    @Override
    public void getUploadErrors(GetUploadErrorsRequest request, StreamObserver<GetUploadErrorsResponse> responseObserver) {
        responseObserver.onError(NotPorted.error("epl.integration.v1.UploadService/GetUploadErrors", "EPL-IT-110W"));
    }
}

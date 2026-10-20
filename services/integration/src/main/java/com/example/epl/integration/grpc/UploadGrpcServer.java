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
 * Every RPC answers UNIMPLEMENTED until the legacy logic named in its Javadoc is ported.
 */
@GrpcServer(UploadServiceGrpc.class)
public class UploadGrpcServer extends UploadServiceGrpc.UploadServiceImplBase {

    /**
     * Legacy: gwh_lpupl_010..110 / *Upd (FILE_KND 01..57), gwh_lpflb_010, gwh_upupl
     */
    @Override
    public StreamObserver<UploadChunk> upload(StreamObserver<UploadResult> responseObserver) {
        responseObserver.onError(NotPorted.error("epl.integration.v1.UploadService/Upload", "gwh_lpupl_010..110 / *Upd (FILE_KND 01..57), gwh_lpflb_010, gwh_upupl"));
        return NotPorted.ignoreRequests();
    }

    /**
     * Legacy: gwh_lpupl_020 / GWH_TJ_EE (upload error list)
     */
    @Override
    public void getUploadErrors(GetUploadErrorsRequest request, StreamObserver<GetUploadErrorsResponse> responseObserver) {
        responseObserver.onError(NotPorted.error("epl.integration.v1.UploadService/GetUploadErrors", "gwh_lpupl_020 / GWH_TJ_EE (upload error list)"));
    }
}

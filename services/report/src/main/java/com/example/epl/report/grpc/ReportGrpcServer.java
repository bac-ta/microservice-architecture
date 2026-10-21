package com.example.epl.report.grpc;

import com.example.epl.report.v1.FileChunk;
import com.example.epl.report.v1.ListReportsRequest;
import com.example.epl.report.v1.ListReportsResponse;
import com.example.epl.report.v1.RenderRequest;
import com.example.epl.report.v1.ReportServiceGrpc;
import com.example.grpc.server.models.annotations.GrpcServer;
import io.grpc.stub.StreamObserver;

/**
 * gRPC endpoint epl.report.v1.ReportService (contract: proto/src/main/proto/epl/report/v1/report.proto).
 * Every RPC answers UNIMPLEMENTED, naming the screens that call it, until it is implemented.
 */
@GrpcServer(ReportServiceGrpc.class)
public class ReportGrpcServer extends ReportServiceGrpc.ReportServiceImplBase {

    @Override
    public void listReports(ListReportsRequest request, StreamObserver<ListReportsResponse> responseObserver) {
        responseObserver.onError(NotPorted.error("epl.report.v1.ReportService/ListReports", "EPL-RP-110W"));
    }

    /**
     * Streams the rendered file in chunks.
     */
    @Override
    public void render(RenderRequest request, StreamObserver<FileChunk> responseObserver) {
        responseObserver.onError(NotPorted.error("epl.report.v1.ReportService/Render", "EPL-RP-110W"));
    }
}

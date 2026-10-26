package com.epl.report.grpc;

import com.epl.report.services.ReportCatalogService;
import com.epl.report.v1.FileChunk;
import com.epl.report.v1.ListReportsRequest;
import com.epl.report.v1.ListReportsResponse;
import com.epl.report.v1.RenderRequest;
import com.epl.report.v1.ReportServiceGrpc;
import com.epl.grpc.server.models.annotations.GrpcServer;
import com.epl.service.core.GrpcCalls;
import io.grpc.stub.StreamObserver;

/**
 * gRPC endpoint epl.report.v1.ReportService (contract: proto/src/main/proto/epl/report/v1/report.proto).
 * ListReports is implemented; Render answers UNIMPLEMENTED, naming its screen, until it is.
 */
@GrpcServer(ReportServiceGrpc.class)
public class ReportGrpcServer extends ReportServiceGrpc.ReportServiceImplBase {

    private final ReportCatalogService reportCatalogService;

    public ReportGrpcServer(ReportCatalogService reportCatalogService) {
        this.reportCatalogService = reportCatalogService;
    }

    @Override
    public void listReports(ListReportsRequest request, StreamObserver<ListReportsResponse> responseObserver) {
        GrpcCalls.unary(responseObserver, () -> ListReportsResponse.newBuilder()
                .addAllReports(reportCatalogService.list(request.getProcessCode())).build());
    }

    /**
     * Streams the rendered file in chunks.
     */
    @Override
    public void render(RenderRequest request, StreamObserver<FileChunk> responseObserver) {
        responseObserver.onError(NotPorted.error("epl.report.v1.ReportService/Render", "EPL-RP-110W"));
    }
}

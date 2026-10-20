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
 * Every RPC answers UNIMPLEMENTED until the legacy logic named in its Javadoc is ported.
 */
@GrpcServer(ReportServiceGrpc.class)
public class ReportGrpcServer extends ReportServiceGrpc.ReportServiceImplBase {

    /**
     * Legacy: report list per process (GWH_TM_RPT / RPTP / RPTC)
     */
    @Override
    public void listReports(ListReportsRequest request, StreamObserver<ListReportsResponse> responseObserver) {
        responseObserver.onError(NotPorted.error("epl.report.v1.ReportService/ListReports", "report list per process (GWH_TM_RPT / RPTP / RPTC)"));
    }

    /**
     * Legacy: gwh_rp*_010 screens, e.g. rpain (arrival inspection list), rpinv (inventory), rppks (picking
     * list), rpdvn (delivery note), rpstk (stocktake sheet), rpwcs (charge statement) Streams the rendered
     * file in chunks.
     */
    @Override
    public void render(RenderRequest request, StreamObserver<FileChunk> responseObserver) {
        responseObserver.onError(NotPorted.error("epl.report.v1.ReportService/Render", "gwh_rp*_010 screens, e.g. rpain (arrival inspection list), rpinv (inventory), rppks (picking list), rpdvn (delivery note), rpstk (stocktake sheet), rpwcs (charge statement) Streams the rendered file in chunks."));
    }
}

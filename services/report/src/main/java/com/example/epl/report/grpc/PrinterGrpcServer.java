package com.example.epl.report.grpc;

import com.example.epl.report.services.PrinterService;
import com.example.epl.report.v1.DeletePrinterRequest;
import com.example.epl.report.v1.GetPrinterRequest;
import com.example.epl.report.v1.ListPrintersRequest;
import com.example.epl.report.v1.ListPrintersResponse;
import com.example.epl.report.v1.Printer;
import com.example.epl.report.v1.PrinterServiceGrpc;
import com.example.grpc.server.models.annotations.GrpcServer;
import com.example.service.core.GrpcCalls;
import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;

/**
 * gRPC endpoint epl.report.v1.PrinterService (contract: proto/src/main/proto/epl/report/v1/report.proto).
 * Printers of the warehouse in x-warehouse.
 */
@GrpcServer(PrinterServiceGrpc.class)
public class PrinterGrpcServer extends PrinterServiceGrpc.PrinterServiceImplBase {

    private final PrinterService printerService;

    public PrinterGrpcServer(PrinterService printerService) {
        this.printerService = printerService;
    }

    @Override
    public void listPrinters(ListPrintersRequest request, StreamObserver<ListPrintersResponse> responseObserver) {
        GrpcCalls.unary(responseObserver, () -> ListPrintersResponse.newBuilder().addAllPrinters(printerService.list(request.getKind())).build());
    }

    @Override
    public void getPrinter(GetPrinterRequest request, StreamObserver<Printer> responseObserver) {
        GrpcCalls.unary(responseObserver, () -> printerService.get(request.getCode()));
    }

    @Override
    public void upsertPrinter(Printer request, StreamObserver<Printer> responseObserver) {
        GrpcCalls.unary(responseObserver, () -> printerService.upsert(request));
    }

    @Override
    public void deletePrinter(DeletePrinterRequest request, StreamObserver<Empty> responseObserver) {
        GrpcCalls.unaryEmpty(responseObserver, () -> printerService.delete(request.getCode()));
    }
}

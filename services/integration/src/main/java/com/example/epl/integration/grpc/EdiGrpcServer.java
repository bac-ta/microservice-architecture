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
 * Every RPC answers UNIMPLEMENTED until the legacy logic named in its Javadoc is ported.
 */
@GrpcServer(EdiServiceGrpc.class)
public class EdiGrpcServer extends EdiServiceGrpc.EdiServiceImplBase {

    /**
     * Legacy: gwh_lpedi_010 / gwhLpedi010Get, GetEdidDat (GWH_TJ_EDI_H / EDI_D, GWH_TJ_ER)
     */
    @Override
    public void search(SearchEdiRequest request, StreamObserver<SearchEdiResponse> responseObserver) {
        responseObserver.onError(NotPorted.error("epl.integration.v1.EdiService/Search", "gwh_lpedi_010 / gwhLpedi010Get, GetEdidDat (GWH_TJ_EDI_H / EDI_D, GWH_TJ_ER)"));
    }

    /**
     * Legacy: gwh_lpedi_010 / *Snd, *Rec, *Rer; gwh_lpwes_010 / gwhLpwes010Snd (GWES_HIS re-send)
     */
    @Override
    public void resend(ResendEdiRequest request, StreamObserver<EdiMessage> responseObserver) {
        responseObserver.onError(NotPorted.error("epl.integration.v1.EdiService/Resend", "gwh_lpedi_010 / *Snd, *Rec, *Rer; gwh_lpwes_010 / gwhLpwes010Snd (GWES_HIS re-send)"));
    }
}

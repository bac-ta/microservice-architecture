package com.example.epl.masterdata.grpc;

import com.example.epl.masterdata.v1.ConfigServiceGrpc;
import com.example.epl.masterdata.v1.GetEffectiveSettingsRequest;
import com.example.epl.masterdata.v1.GetEffectiveSettingsResponse;
import com.example.epl.masterdata.v1.ListCodesRequest;
import com.example.epl.masterdata.v1.ListCodesResponse;
import com.example.epl.masterdata.v1.ListPickingKeyDefinitionsResponse;
import com.example.epl.masterdata.v1.ListStatusesRequest;
import com.example.epl.masterdata.v1.ListStatusesResponse;
import com.example.epl.masterdata.v1.ListSubInventoriesResponse;
import com.example.epl.masterdata.v1.ListTransactionKindsRequest;
import com.example.epl.masterdata.v1.ListTransactionKindsResponse;
import com.example.grpc.server.models.annotations.GrpcServer;
import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;

/**
 * gRPC endpoint epl.masterdata.v1.ConfigService (contract: proto/src/main/proto/epl/masterdata/v1/masterdata.proto).
 * Every RPC answers UNIMPLEMENTED, naming the screens that call it, until it is implemented.
 */
@GrpcServer(ConfigServiceGrpc.class)
public class ConfigGrpcServer extends ConfigServiceGrpc.ConfigServiceImplBase {

    @Override
    public void listSubInventories(Empty request, StreamObserver<ListSubInventoriesResponse> responseObserver) {
        responseObserver.onError(NotPorted.error("epl.masterdata.v1.ConfigService/ListSubInventories", "EPL-MD-410W"));
    }

    @Override
    public void listPickingKeyDefinitions(Empty request, StreamObserver<ListPickingKeyDefinitionsResponse> responseObserver) {
        responseObserver.onError(NotPorted.error("epl.masterdata.v1.ConfigService/ListPickingKeyDefinitions", "EPL-MD-420W"));
    }

    @Override
    public void listTransactionKinds(ListTransactionKindsRequest request, StreamObserver<ListTransactionKindsResponse> responseObserver) {
        responseObserver.onError(NotPorted.error("epl.masterdata.v1.ConfigService/ListTransactionKinds", "EPL-MD-430W"));
    }

    @Override
    public void listStatuses(ListStatusesRequest request, StreamObserver<ListStatusesResponse> responseObserver) {
        responseObserver.onError(NotPorted.error("epl.masterdata.v1.ConfigService/ListStatuses", "EPL-MD-440W"));
    }

    @Override
    public void listCodes(ListCodesRequest request, StreamObserver<ListCodesResponse> responseObserver) {
        responseObserver.onError(NotPorted.error("epl.masterdata.v1.ConfigService/ListCodes", "EPL-MD-450W"));
    }

    @Override
    public void getEffectiveSettings(GetEffectiveSettingsRequest request, StreamObserver<GetEffectiveSettingsResponse> responseObserver) {
        responseObserver.onError(NotPorted.error("epl.masterdata.v1.ConfigService/GetEffectiveSettings", "EPL-MD-460W"));
    }
}

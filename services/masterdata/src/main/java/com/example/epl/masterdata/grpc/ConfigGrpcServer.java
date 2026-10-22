package com.example.epl.masterdata.grpc;

import com.example.epl.masterdata.services.SubInventoryService;
import com.example.epl.masterdata.v1.ConfigServiceGrpc;
import com.example.epl.masterdata.v1.DeleteSubInventoryRequest;
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
import com.example.epl.masterdata.v1.SubInventory;
import com.example.grpc.server.models.annotations.GrpcServer;
import com.example.service.core.GrpcCalls;
import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;

/**
 * gRPC endpoint epl.masterdata.v1.ConfigService (contract: proto/src/main/proto/epl/masterdata/v1/masterdata.proto).
 * Sub-inventories are implemented; the other RPCs answer UNIMPLEMENTED, naming their screens, until they are.
 */
@GrpcServer(ConfigServiceGrpc.class)
public class ConfigGrpcServer extends ConfigServiceGrpc.ConfigServiceImplBase {

    private final SubInventoryService subInventoryService;

    public ConfigGrpcServer(SubInventoryService subInventoryService) {
        this.subInventoryService = subInventoryService;
    }

    @Override
    public void listSubInventories(Empty request, StreamObserver<ListSubInventoriesResponse> responseObserver) {
        GrpcCalls.unary(responseObserver, () -> ListSubInventoriesResponse.newBuilder()
                .addAllSubInventories(subInventoryService.list()).build());
    }

    @Override
    public void upsertSubInventory(SubInventory request, StreamObserver<SubInventory> responseObserver) {
        GrpcCalls.unary(responseObserver, () -> subInventoryService.upsert(request));
    }

    @Override
    public void deleteSubInventory(DeleteSubInventoryRequest request, StreamObserver<Empty> responseObserver) {
        GrpcCalls.unaryEmpty(responseObserver, () -> subInventoryService.delete(request.getCode()));
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

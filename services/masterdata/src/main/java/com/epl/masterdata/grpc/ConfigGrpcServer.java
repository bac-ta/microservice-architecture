package com.epl.masterdata.grpc;

import com.epl.masterdata.services.SubInventoryService;
import com.epl.masterdata.v1.ConfigServiceGrpc;
import com.epl.masterdata.v1.DeleteSubInventoryRequest;
import com.epl.masterdata.v1.GetEffectiveSettingsRequest;
import com.epl.masterdata.v1.GetEffectiveSettingsResponse;
import com.epl.masterdata.v1.ListCodesRequest;
import com.epl.masterdata.v1.ListCodesResponse;
import com.epl.masterdata.v1.ListPickingKeyDefinitionsResponse;
import com.epl.masterdata.v1.ListStatusesRequest;
import com.epl.masterdata.v1.ListStatusesResponse;
import com.epl.masterdata.v1.ListSubInventoriesResponse;
import com.epl.masterdata.v1.ListTransactionKindsRequest;
import com.epl.masterdata.v1.ListTransactionKindsResponse;
import com.epl.masterdata.v1.SubInventory;
import com.epl.grpc.server.models.annotations.GrpcServer;
import com.epl.service.core.GrpcCalls;
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

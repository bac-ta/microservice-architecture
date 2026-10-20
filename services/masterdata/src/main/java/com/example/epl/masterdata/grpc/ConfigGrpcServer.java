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
 * Every RPC answers UNIMPLEMENTED until the legacy logic named in its Javadoc is ported.
 */
@GrpcServer(ConfigServiceGrpc.class)
public class ConfigGrpcServer extends ConfigServiceGrpc.ConfigServiceImplBase {

    /**
     * Legacy: gwh_mpsub_010, gwh_mbsub (GWH_TM_SBIV)
     */
    @Override
    public void listSubInventories(Empty request, StreamObserver<ListSubInventoriesResponse> responseObserver) {
        responseObserver.onError(NotPorted.error("epl.masterdata.v1.ConfigService/ListSubInventories", "gwh_mpsub_010, gwh_mbsub (GWH_TM_SBIV)"));
    }

    /**
     * Legacy: gwh_mppik_010 (GWH_TM_PIK)
     */
    @Override
    public void listPickingKeyDefinitions(Empty request, StreamObserver<ListPickingKeyDefinitionsResponse> responseObserver) {
        responseObserver.onError(NotPorted.error("epl.masterdata.v1.ConfigService/ListPickingKeyDefinitions", "gwh_mppik_010 (GWH_TM_PIK)"));
    }

    /**
     * Legacy: gwh_mptrn_010 (GWH_TM_TRN)
     */
    @Override
    public void listTransactionKinds(ListTransactionKindsRequest request, StreamObserver<ListTransactionKindsResponse> responseObserver) {
        responseObserver.onError(NotPorted.error("epl.masterdata.v1.ConfigService/ListTransactionKinds", "gwh_mptrn_010 (GWH_TM_TRN)"));
    }

    /**
     * Legacy: gwh_mpsts_010 (GWH_TM_STS) + GWH_TM_PRCC process control
     */
    @Override
    public void listStatuses(ListStatusesRequest request, StreamObserver<ListStatusesResponse> responseObserver) {
        responseObserver.onError(NotPorted.error("epl.masterdata.v1.ConfigService/ListStatuses", "gwh_mpsts_010 (GWH_TM_STS) + GWH_TM_PRCC process control"));
    }

    /**
     * Legacy: gwh_mpcod_010, gwh_mprsn_010 (GWH_TM_CODE, GWH_TM_RSN)
     */
    @Override
    public void listCodes(ListCodesRequest request, StreamObserver<ListCodesResponse> responseObserver) {
        responseObserver.onError(NotPorted.error("epl.masterdata.v1.ConfigService/ListCodes", "gwh_mpcod_010, gwh_mprsn_010 (GWH_TM_CODE, GWH_TM_RSN)"));
    }

    /**
     * Legacy: gwh_mpcus_010 (≈249 behaviour switches of GWH_TM_CUST)
     */
    @Override
    public void getEffectiveSettings(GetEffectiveSettingsRequest request, StreamObserver<GetEffectiveSettingsResponse> responseObserver) {
        responseObserver.onError(NotPorted.error("epl.masterdata.v1.ConfigService/GetEffectiveSettings", "gwh_mpcus_010 (≈249 behaviour switches of GWH_TM_CUST)"));
    }
}

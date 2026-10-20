package com.example.epl.masterdata.grpc;

import com.example.epl.masterdata.v1.Area;
import com.example.epl.masterdata.v1.GetLocationRequest;
import com.example.epl.masterdata.v1.ListAreasRequest;
import com.example.epl.masterdata.v1.ListAreasResponse;
import com.example.epl.masterdata.v1.ListLocationsRequest;
import com.example.epl.masterdata.v1.ListLocationsResponse;
import com.example.epl.masterdata.v1.Location;
import com.example.epl.masterdata.v1.LocationServiceGrpc;
import com.example.grpc.server.models.annotations.GrpcServer;
import io.grpc.stub.StreamObserver;

/**
 * gRPC endpoint epl.masterdata.v1.LocationService (contract: proto/src/main/proto/epl/masterdata/v1/masterdata.proto).
 * Every RPC answers UNIMPLEMENTED until the legacy logic named in its Javadoc is ported.
 */
@GrpcServer(LocationServiceGrpc.class)
public class LocationGrpcServer extends LocationServiceGrpc.LocationServiceImplBase {

    /**
     * Legacy: gwh_mpare_010, gwh_cpcmn (area lookup)
     */
    @Override
    public void listAreas(ListAreasRequest request, StreamObserver<ListAreasResponse> responseObserver) {
        responseObserver.onError(NotPorted.error("epl.masterdata.v1.LocationService/ListAreas", "gwh_mpare_010, gwh_cpcmn (area lookup)"));
    }

    /**
     * Legacy: gwh_mpare_010 register
     */
    @Override
    public void upsertArea(Area request, StreamObserver<Area> responseObserver) {
        responseObserver.onError(NotPorted.error("epl.masterdata.v1.LocationService/UpsertArea", "gwh_mpare_010 register"));
    }

    /**
     * Legacy: gwh_mploc_010, gwh_cploc_010 (location popup)
     */
    @Override
    public void listLocations(ListLocationsRequest request, StreamObserver<ListLocationsResponse> responseObserver) {
        responseObserver.onError(NotPorted.error("epl.masterdata.v1.LocationService/ListLocations", "gwh_mploc_010, gwh_cploc_010 (location popup)"));
    }

    /**
     * Legacy: new in EPL
     */
    @Override
    public void getLocation(GetLocationRequest request, StreamObserver<Location> responseObserver) {
        responseObserver.onError(NotPorted.error("epl.masterdata.v1.LocationService/GetLocation", "new in EPL"));
    }

    /**
     * Legacy: gwh_mploc_010 register
     */
    @Override
    public void upsertLocation(Location request, StreamObserver<Location> responseObserver) {
        responseObserver.onError(NotPorted.error("epl.masterdata.v1.LocationService/UpsertLocation", "gwh_mploc_010 register"));
    }
}

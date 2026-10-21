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
 * Every RPC answers UNIMPLEMENTED, naming the screens that call it, until it is implemented.
 */
@GrpcServer(LocationServiceGrpc.class)
public class LocationGrpcServer extends LocationServiceGrpc.LocationServiceImplBase {

    @Override
    public void listAreas(ListAreasRequest request, StreamObserver<ListAreasResponse> responseObserver) {
        responseObserver.onError(NotPorted.error("epl.masterdata.v1.LocationService/ListAreas", "EPL-MD-110W"));
    }

    @Override
    public void upsertArea(Area request, StreamObserver<Area> responseObserver) {
        responseObserver.onError(NotPorted.error("epl.masterdata.v1.LocationService/UpsertArea", "EPL-MD-110W"));
    }

    @Override
    public void listLocations(ListLocationsRequest request, StreamObserver<ListLocationsResponse> responseObserver) {
        responseObserver.onError(NotPorted.error("epl.masterdata.v1.LocationService/ListLocations", "EPL-MD-120W"));
    }

    @Override
    public void getLocation(GetLocationRequest request, StreamObserver<Location> responseObserver) {
        responseObserver.onError(NotPorted.error("epl.masterdata.v1.LocationService/GetLocation", "EPL-MD-120W"));
    }

    @Override
    public void upsertLocation(Location request, StreamObserver<Location> responseObserver) {
        responseObserver.onError(NotPorted.error("epl.masterdata.v1.LocationService/UpsertLocation", "EPL-MD-120W"));
    }
}

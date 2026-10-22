package com.example.epl.masterdata.grpc;

import com.example.epl.masterdata.services.AreaService;
import com.example.epl.masterdata.services.LocationService;
import com.example.epl.masterdata.v1.Area;
import com.example.epl.masterdata.v1.DeleteAreaRequest;
import com.example.epl.masterdata.v1.DeleteLocationRequest;
import com.example.epl.masterdata.v1.GetAreaRequest;
import com.example.epl.masterdata.v1.GetLocationRequest;
import com.example.epl.masterdata.v1.ListAreasRequest;
import com.example.epl.masterdata.v1.ListAreasResponse;
import com.example.epl.masterdata.v1.ListLocationsRequest;
import com.example.epl.masterdata.v1.ListLocationsResponse;
import com.example.epl.masterdata.v1.Location;
import com.example.epl.masterdata.v1.LocationServiceGrpc;
import com.example.grpc.server.models.annotations.GrpcServer;
import com.example.service.core.GrpcCalls;
import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;

/**
 * gRPC endpoint epl.masterdata.v1.LocationService (contract: proto/src/main/proto/epl/masterdata/v1/masterdata.proto).
 * Areas and locations of the warehouse in x-warehouse.
 */
@GrpcServer(LocationServiceGrpc.class)
public class LocationGrpcServer extends LocationServiceGrpc.LocationServiceImplBase {

    private final AreaService areaService;
    private final LocationService locationService;

    public LocationGrpcServer(AreaService areaService, LocationService locationService) {
        this.areaService = areaService;
        this.locationService = locationService;
    }

    @Override
    public void listAreas(ListAreasRequest request, StreamObserver<ListAreasResponse> responseObserver) {
        GrpcCalls.unary(responseObserver, () -> areaService.list(request));
    }

    @Override
    public void getArea(GetAreaRequest request, StreamObserver<Area> responseObserver) {
        GrpcCalls.unary(responseObserver, () -> areaService.get(request.getCode()));
    }

    @Override
    public void upsertArea(Area request, StreamObserver<Area> responseObserver) {
        GrpcCalls.unary(responseObserver, () -> areaService.upsert(request));
    }

    @Override
    public void deleteArea(DeleteAreaRequest request, StreamObserver<Empty> responseObserver) {
        GrpcCalls.unaryEmpty(responseObserver, () -> areaService.delete(request.getCode(), request.getVersion()));
    }

    @Override
    public void listLocations(ListLocationsRequest request, StreamObserver<ListLocationsResponse> responseObserver) {
        GrpcCalls.unary(responseObserver, () -> locationService.list(request));
    }

    @Override
    public void getLocation(GetLocationRequest request, StreamObserver<Location> responseObserver) {
        GrpcCalls.unary(responseObserver, () -> locationService.get(request.getCode()));
    }

    @Override
    public void upsertLocation(Location request, StreamObserver<Location> responseObserver) {
        GrpcCalls.unary(responseObserver, () -> locationService.upsert(request));
    }

    @Override
    public void deleteLocation(DeleteLocationRequest request, StreamObserver<Empty> responseObserver) {
        GrpcCalls.unaryEmpty(responseObserver, () -> locationService.delete(request.getCode(), request.getVersion()));
    }
}

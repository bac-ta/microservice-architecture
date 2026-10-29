package com.epl.masterdata.services;

import com.epl.masterdata.entities.LocationEntity;
import com.epl.masterdata.entities.WarehouseCodeId;
import com.epl.masterdata.mappers.MasterdataMappers;
import com.epl.masterdata.repositories.AreaRepository;
import com.epl.masterdata.repositories.LocationRepository;
import com.epl.masterdata.v1.ListLocationsRequest;
import com.epl.masterdata.v1.ListLocationsResponse;
import com.epl.masterdata.v1.Location;
import com.epl.service.core.Errors;
import com.epl.service.core.Pages;
import com.epl.service.core.RequestContext;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

/** Locations of the warehouse in x-warehouse. */
@RequiredArgsConstructor
@Service
public class LocationService {

    private final LocationRepository locationRepository;
    private final AreaRepository areaRepository;

    @Transactional(readOnly = true)
    public ListLocationsResponse list(ListLocationsRequest request) {
        if (request.getOnlyEmpty()) {
            throw Errors.unsupported("only_empty needs the inventory service and is not supported yet");
        }
        var warehouseId = RequestContext.current().requireWarehouse();
        var page = locationRepository.search(warehouseId, request.getAreaCode(), request.getCodeFrom(), request.getCodeTo(),
                Pages.pageable(request.getPage(), Sort.by("code")));
        return ListLocationsResponse.newBuilder()
                .addAllLocations(page.map(MasterdataMappers::location).getContent())
                .setPage(Pages.response(page))
                .build();
    }

    @Transactional(readOnly = true)
    public Location get(String code) {
        return MasterdataMappers.location(find(code));
    }

    @Transactional
    public Location upsert(Location request) {
        var warehouseId = RequestContext.current().requireWarehouse();
        var code = Errors.required(request.getCode(), "code");
        Errors.required(request.getRackCode(), "rack_code");
        Errors.required(request.getPositionCode(), "position_code");
        Errors.required(request.getLevelCode(), "level_code");
        areaRepository.findByWarehouseIdAndCodeAndDeletedAtIsNull(warehouseId, Errors.required(request.getAreaCode(), "area_code"))
                .orElseThrow(() -> Errors.notFound("Area", request.getAreaCode()));

        var entity = locationRepository.findById(new WarehouseCodeId(warehouseId, code)).orElse(null);
        var live = entity != null && entity.getDeletedAt() == null;
        Errors.checkUpsert("Location", code, live, request.hasAudit(), live ? entity.getVersion() : 0, request.getAudit().getVersion());
        if (entity == null) {
            entity = new LocationEntity();
            entity.setWarehouseId(warehouseId);
            entity.setCode(code);
        }
        entity.setDeletedAt(null);
        MasterdataMappers.apply(entity, request);
        return MasterdataMappers.location(locationRepository.saveAndFlush(entity));
    }

    /** Soft delete: stock and history keep the code. */
    @Transactional
    public void delete(String code, int version) {
        var entity = find(code);
        Errors.checkVersion(entity.getVersion(), version);
        entity.setDeletedAt(Instant.now());
    }

    private LocationEntity find(String code) {
        var warehouseId = RequestContext.current().requireWarehouse();
        return locationRepository.findByWarehouseIdAndCodeAndDeletedAtIsNull(warehouseId, Errors.required(code, "code"))
                .orElseThrow(() -> Errors.notFound("Location", code));
    }
}

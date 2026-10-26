package com.epl.masterdata.services;

import com.epl.masterdata.entities.AreaEntity;
import com.epl.masterdata.entities.WarehouseCodeId;
import com.epl.masterdata.mappers.MasterdataMappers;
import com.epl.masterdata.repositories.AreaRepository;
import com.epl.masterdata.repositories.LocationRepository;
import com.epl.masterdata.v1.Area;
import com.epl.masterdata.v1.ListAreasRequest;
import com.epl.masterdata.v1.ListAreasResponse;
import com.epl.service.core.Errors;
import com.epl.service.core.Pages;
import com.epl.service.core.RequestContext;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

/** Areas of the warehouse in x-warehouse. */
@Service
public class AreaService {

    private final AreaRepository areaRepository;
    private final LocationRepository locationRepository;

    public AreaService(AreaRepository areaRepository, LocationRepository locationRepository) {
        this.areaRepository = areaRepository;
        this.locationRepository = locationRepository;
    }

    @Transactional(readOnly = true)
    public ListAreasResponse list(ListAreasRequest request) {
        var warehouseId = RequestContext.current().requireWarehouse();
        var page = areaRepository.findAllByWarehouseIdAndDeletedAtIsNull(warehouseId, Pages.pageable(request.getPage(), Sort.by("code")));
        return ListAreasResponse.newBuilder()
                .addAllAreas(page.map(MasterdataMappers::area).getContent())
                .setPage(Pages.response(page))
                .build();
    }

    @Transactional(readOnly = true)
    public Area get(String code) {
        return MasterdataMappers.area(find(code));
    }

    @Transactional
    public Area upsert(Area request) {
        var warehouseId = RequestContext.current().requireWarehouse();
        var code = Errors.required(request.getCode(), "code");
        Errors.required(request.getName(), "name");
        var entity = areaRepository.findById(new WarehouseCodeId(warehouseId, code)).orElse(null);
        var live = entity != null && entity.getDeletedAt() == null;
        Errors.checkUpsert("Area", code, live, request.hasAudit(), live ? entity.getVersion() : 0, request.getAudit().getVersion());
        if (entity == null) {
            entity = new AreaEntity();
            entity.setWarehouseId(warehouseId);
            entity.setCode(code);
        }
        entity.setDeletedAt(null);
        MasterdataMappers.apply(entity, request);
        return MasterdataMappers.area(areaRepository.saveAndFlush(entity));
    }

    /** Soft delete; refused while the area still has locations. */
    @Transactional
    public void delete(String code, int version) {
        var entity = find(code);
        Errors.checkVersion(entity.getVersion(), version);
        if (locationRepository.existsByWarehouseIdAndAreaCodeAndDeletedAtIsNull(entity.getWarehouseId(), code)) {
            throw Errors.failedPrecondition("Area " + code + " still has locations");
        }
        entity.setDeletedAt(Instant.now());
    }

    private AreaEntity find(String code) {
        var warehouseId = RequestContext.current().requireWarehouse();
        return areaRepository.findByWarehouseIdAndCodeAndDeletedAtIsNull(warehouseId, Errors.required(code, "code"))
                .orElseThrow(() -> Errors.notFound("Area", code));
    }
}

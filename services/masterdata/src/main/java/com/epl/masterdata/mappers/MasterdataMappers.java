package com.epl.masterdata.mappers;

import com.epl.masterdata.entities.AreaEntity;
import com.epl.masterdata.entities.LocationEntity;
import com.epl.masterdata.entities.ProductEntity;
import com.epl.masterdata.entities.SubInventoryEntity;
import com.epl.masterdata.v1.Area;
import com.epl.masterdata.v1.Location;
import com.epl.masterdata.v1.Packing;
import com.epl.masterdata.v1.Product;
import com.epl.masterdata.v1.SubInventory;

import java.util.List;

import static com.epl.service.core.Protos.audit;
import static com.epl.service.core.Protos.bigDecimal;
import static com.epl.service.core.Protos.decimal;
import static com.epl.service.core.Protos.emptyIfNull;
import static com.epl.service.core.Protos.map;
import static com.epl.service.core.Protos.nullIfEmpty;
import static com.epl.service.core.Protos.nullIfZero;
import static com.epl.service.core.Protos.struct;
import static com.epl.service.core.Protos.zeroIfNull;

/** Master data entities <-> proto messages (keys are set by the services). */
public final class MasterdataMappers {

    private MasterdataMappers() {
    }

    // area --------------------------------------------------------------------

    public static Area area(AreaEntity entity) {
        return Area.newBuilder()
                .setCode(entity.getCode())
                .setName(entity.getName())
                .setTemperatureKind(emptyIfNull(entity.getTemperatureKind()))
                .setPickingGroupCode(emptyIfNull(entity.getPickingGroupCode()))
                .setNoAllocation(entity.isNoAllocation())
                .setNoReplenishment(entity.isNoReplenishment())
                .setSingleProductOnly(entity.isSingleProductOnly())
                .setSinglePickKeyOnly(entity.isSinglePickKeyOnly())
                .setLocked(entity.isLocked())
                .setAudit(audit(entity))
                .build();
    }

    public static void apply(AreaEntity entity, Area request) {
        entity.setName(request.getName());
        entity.setTemperatureKind(nullIfEmpty(request.getTemperatureKind()));
        entity.setPickingGroupCode(nullIfEmpty(request.getPickingGroupCode()));
        entity.setNoAllocation(request.getNoAllocation());
        entity.setNoReplenishment(request.getNoReplenishment());
        entity.setSingleProductOnly(request.getSingleProductOnly());
        entity.setSinglePickKeyOnly(request.getSinglePickKeyOnly());
        entity.setLocked(request.getLocked());
    }

    // location ----------------------------------------------------------------

    public static Location location(LocationEntity entity) {
        return Location.newBuilder()
                .setCode(entity.getCode())
                .setAreaCode(entity.getAreaCode())
                .setRackCode(entity.getRackCode())
                .setPositionCode(entity.getPositionCode())
                .setLevelCode(entity.getLevelCode())
                .setZoneCode(emptyIfNull(entity.getZoneCode()))
                .setNoAllocation(entity.isNoAllocation())
                .setNoReplenishment(entity.isNoReplenishment())
                .setSingleProductOnly(entity.isSingleProductOnly())
                .setSinglePickKeyOnly(entity.isSinglePickKeyOnly())
                .setAllocationPriority(zeroIfNull(entity.getAllocationPriority()))
                .setPickingSequence(zeroIfNull(entity.getPickingSequence()))
                .setDedicatedProductCode(emptyIfNull(entity.getDedicatedProductCode()))
                .setSubInventoryCode(emptyIfNull(entity.getSubInventoryCode()))
                .setBondKind(emptyIfNull(entity.getBondKind()))
                .setTemperatureControl(emptyIfNull(entity.getTemperatureControl()))
                .setRemarks(emptyIfNull(entity.getRemarks()))
                .setExt(struct(entity.getExt()))
                .setAudit(audit(entity))
                .build();
    }

    public static void apply(LocationEntity entity, Location request) {
        entity.setAreaCode(request.getAreaCode());
        entity.setRackCode(request.getRackCode());
        entity.setPositionCode(request.getPositionCode());
        entity.setLevelCode(request.getLevelCode());
        entity.setZoneCode(nullIfEmpty(request.getZoneCode()));
        entity.setNoAllocation(request.getNoAllocation());
        entity.setNoReplenishment(request.getNoReplenishment());
        entity.setSingleProductOnly(request.getSingleProductOnly());
        entity.setSinglePickKeyOnly(request.getSinglePickKeyOnly());
        entity.setAllocationPriority(nullIfZero(request.getAllocationPriority()));
        entity.setPickingSequence(nullIfZero(request.getPickingSequence()));
        entity.setDedicatedProductCode(nullIfEmpty(request.getDedicatedProductCode()));
        entity.setSubInventoryCode(nullIfEmpty(request.getSubInventoryCode()));
        entity.setBondKind(nullIfEmpty(request.getBondKind()));
        entity.setTemperatureControl(nullIfEmpty(request.getTemperatureControl()));
        entity.setRemarks(nullIfEmpty(request.getRemarks()));
        entity.setExt(map(request.getExt()));
    }

    // product -----------------------------------------------------------------

    public static Product product(ProductEntity entity, List<String> barcodes) {
        return Product.newBuilder()
                .setCode(entity.getCode())
                .addAllNames(entity.getNames())
                .setProductGroupCode(emptyIfNull(entity.getProductGroupCode()))
                .addAllHierarchy(entity.getHierarchy())
                .setPacking(Packing.newBuilder()
                        .setUnitsPerCase(zeroIfNull(entity.getUnitsPerCase()))
                        .setUnitsPerBall(zeroIfNull(entity.getUnitsPerBall()))
                        .setCasesPerPallet(zeroIfNull(entity.getCasesPerPallet()))
                        .setUomPiece(emptyIfNull(entity.getUomPiece()))
                        .setUomCase(emptyIfNull(entity.getUomCase()))
                        .setUomBall(emptyIfNull(entity.getUomBall()))
                        .setCaseWeight(decimal(entity.getCaseWeight()))
                        .setCaseVolume(decimal(entity.getCaseVolume()))
                        .setPieceWeight(decimal(entity.getPieceWeight()))
                        .setPieceVolume(decimal(entity.getPieceVolume())))
                .setStorageKind(emptyIfNull(entity.getStorageKind()))
                .setAbcClass(emptyIfNull(entity.getAbcClass()))
                .setExpiryKind(emptyIfNull(entity.getExpiryKind()))
                .setExpiryDays(zeroIfNull(entity.getExpiryDays()))
                .setNotShippable(entity.isNotShippable())
                .setIsKitParent(entity.isKitParent())
                .setHsCode(emptyIfNull(entity.getHsCode()))
                .setBondKind(emptyIfNull(entity.getBondKind()))
                .setOriginCountry(emptyIfNull(entity.getOriginCountry()))
                .setRecommendedLocation(emptyIfNull(entity.getRecommendedLocation()))
                .addAllBarcodes(barcodes)
                .setExt(struct(entity.getExt()))
                .setAudit(audit(entity))
                .build();
    }

    public static void apply(ProductEntity entity, Product request) {
        entity.setNames(List.copyOf(request.getNamesList()));
        entity.setProductGroupCode(nullIfEmpty(request.getProductGroupCode()));
        entity.setHierarchy(List.copyOf(request.getHierarchyList()));
        var packing = request.getPacking();
        entity.setUnitsPerCase(nullIfZero(packing.getUnitsPerCase()));
        entity.setUnitsPerBall(nullIfZero(packing.getUnitsPerBall()));
        entity.setCasesPerPallet(nullIfZero(packing.getCasesPerPallet()));
        entity.setUomPiece(nullIfEmpty(packing.getUomPiece()));
        entity.setUomCase(nullIfEmpty(packing.getUomCase()));
        entity.setUomBall(nullIfEmpty(packing.getUomBall()));
        entity.setCaseWeight(bigDecimal(packing.getCaseWeight()));
        entity.setCaseVolume(bigDecimal(packing.getCaseVolume()));
        entity.setPieceWeight(bigDecimal(packing.getPieceWeight()));
        entity.setPieceVolume(bigDecimal(packing.getPieceVolume()));
        entity.setStorageKind(nullIfEmpty(request.getStorageKind()));
        entity.setAbcClass(nullIfEmpty(request.getAbcClass()));
        entity.setExpiryKind(nullIfEmpty(request.getExpiryKind()));
        entity.setExpiryDays(nullIfZero(request.getExpiryDays()));
        entity.setNotShippable(request.getNotShippable());
        entity.setKitParent(request.getIsKitParent());
        entity.setHsCode(nullIfEmpty(request.getHsCode()));
        entity.setBondKind(nullIfEmpty(request.getBondKind()));
        entity.setOriginCountry(nullIfEmpty(request.getOriginCountry()));
        entity.setRecommendedLocation(nullIfEmpty(request.getRecommendedLocation()));
        entity.setExt(map(request.getExt()));
    }

    // sub-inventory -----------------------------------------------------------

    public static SubInventory subInventory(SubInventoryEntity entity) {
        return SubInventory.newBuilder()
                .setCode(entity.getCode())
                .setName(entity.getName())
                .setIsDefault(entity.isDefaultSubInventory())
                .build();
    }
}

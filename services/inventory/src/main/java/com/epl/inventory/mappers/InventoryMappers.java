package com.epl.inventory.mappers;

import com.epl.common.v1.PickKeys;
import com.epl.common.v1.Quantity;
import com.epl.common.v1.ReceiptRef;
import com.epl.inventory.entities.PickKeyColumns;
import com.epl.inventory.entities.StockEntity;
import com.epl.inventory.entities.StocktakeEntity;
import com.epl.inventory.entities.StocktakeLineEntity;
import com.epl.inventory.v1.StockLot;
import com.epl.inventory.v1.Stocktake;
import com.epl.inventory.v1.StocktakeLine;
import com.epl.inventory.v1.StocktakeStatus;

import java.math.BigDecimal;

import static com.epl.service.core.Protos.audit;
import static com.epl.service.core.Protos.date;
import static com.epl.service.core.Protos.decimal;
import static com.epl.service.core.Protos.emptyIfNull;
import static com.epl.service.core.Protos.struct;
import static com.epl.service.core.Protos.text;
import static com.epl.service.core.Protos.zeroIfNull;

public final class InventoryMappers {

    private InventoryMappers() {
    }

    public static StockLot stockLot(StockEntity entity) {
        return StockLot.newBuilder()
                .setId(text(entity.getId()))
                .setReceipt(ReceiptRef.newBuilder()
                        .setReceiptNo(entity.getReceiptNo()).setLineNo(entity.getReceiptLine()).setSeqNo(entity.getReceiptSeq()))
                .setStockKind(entity.getStockKind())
                .setReceivedOn(date(entity.getReceivedOn()))
                .setProductCode(entity.getProductCode())
                .setOriginCountry(emptyIfNull(entity.getOriginCountry()))
                .setPickKeys(pickKeys(entity.getLot().getPickKeys()))
                .setLotId(text(entity.getLot().getId()))
                .setLotNo(emptyIfNull(entity.getLot().getLotNo()))
                .setExpiryDate(date(entity.getLot().getExpiryDate()))
                .setLocationCode(entity.getLocationCode())
                .setAreaCode(entity.getAreaCode())
                .setSubInventoryCode(entity.getSubInventoryCode())
                .setPhysicalQty(decimal(entity.getPhysicalQty()))
                .setAvailableQty(decimal(entity.getAvailableQty()))
                .setAllocatedQty(decimal(entity.getAllocatedQty()))
                .setUnitsPerCase(zeroIfNull(entity.getUnitsPerCase()))
                .setUnitsPerBall(zeroIfNull(entity.getUnitsPerBall()))
                .setBonded(entity.isBonded())
                .setOnHold(entity.isOnHold())
                .setLocked(entity.isLocked())
                .setExt(struct(entity.getExt()))
                .setVersion(entity.getVersion())
                .build();
    }

    public static Stocktake stocktake(StocktakeEntity entity) {
        return Stocktake.newBuilder()
                .setId(text(entity.getId()))
                .setStocktakeNo(entity.getStocktakeNo())
                .setStocktakeDate(date(entity.getStocktakeDate()))
                .setConfirmedDate(date(entity.getConfirmedDate()))
                .setStatus(StocktakeStatus.valueOf("STOCKTAKE_STATUS_" + entity.getStatus()))
                .setKind(entity.getKind())
                .setTargetKind(entity.getTargetKind())
                .setIncludeEmptyLocations(entity.isIncludeEmptyLocations())
                .setUpdateStockOnConfirm(entity.isUpdateStockOnConfirm())
                .setCriteria(struct(entity.getCriteria()))
                .setPlannedQty(decimal(entity.getPlannedQty()))
                .setCountedQty(decimal(entity.getCountedQty()))
                .setAudit(audit(entity))
                .build();
    }

    /** DB value of a proto status (without the STOCKTAKE_STATUS_ prefix). */
    public static String status(StocktakeStatus status) {
        return status.name().substring("STOCKTAKE_STATUS_".length());
    }

    public static StocktakeLine stocktakeLine(StocktakeLineEntity entity) {
        var builder = StocktakeLine.newBuilder()
                .setId(text(entity.getId()))
                .setCountNo(entity.getCountNo())
                .setStockId(text(entity.getStockId()))
                .setProductCode(emptyIfNull(entity.getProductCode()))
                .setPickKeys(entity.getLot() == null ? PickKeys.getDefaultInstance() : pickKeys(entity.getLot().getPickKeys()))
                .setLotId(entity.getLot() == null ? "" : text(entity.getLot().getId()))
                .setLocationCode(entity.getLocationCode())
                .setSubInventoryCode(emptyIfNull(entity.getSubInventoryCode()))
                .setBook(quantity(entity.getBookCaseQty(), entity.getBookBallQty(), entity.getBookPieceQty(),
                        entity.getBookTotalQty(), entity))
                .setDiffTotalQty(decimal(entity.getDiffTotalQty()))
                .setRemarks(emptyIfNull(entity.getRemarks()))
                .setVersion(entity.getVersion());
        if (entity.getReceiptNo() != null) {
            builder.setReceipt(ReceiptRef.newBuilder().setReceiptNo(entity.getReceiptNo())
                    .setLineNo(zeroIfNull(entity.getReceiptLine())).setSeqNo(zeroIfNull(entity.getReceiptSeq())));
        }
        if (entity.getCountedTotalQty() != null) {
            builder.setCounted(quantity(zeroIfNull(entity.getCountedCaseQty()), zeroIfNull(entity.getCountedBallQty()),
                    entity.getCountedPieceQty(), entity.getCountedTotalQty(), entity));
        }
        return builder.build();
    }

    /** Hibernate loads the embeddable as null when all seven columns are null (a lot without keys). */
    public static PickKeys pickKeys(PickKeyColumns columns) {
        if (columns == null) {
            return PickKeys.getDefaultInstance();
        }
        return PickKeys.newBuilder()
                .setKey1(emptyIfNull(columns.getKey1())).setKey2(emptyIfNull(columns.getKey2()))
                .setKey3(emptyIfNull(columns.getKey3())).setKey4(emptyIfNull(columns.getKey4()))
                .setKey5(emptyIfNull(columns.getKey5())).setKey6(emptyIfNull(columns.getKey6()))
                .setKey7(emptyIfNull(columns.getKey7()))
                .build();
    }

    private static Quantity quantity(int cases, int balls, BigDecimal pieces, BigDecimal total, StocktakeLineEntity line) {
        return Quantity.newBuilder()
                .setCases(cases)
                .setBalls(balls)
                .setPieces(decimal(pieces))
                .setTotalPieces(decimal(total))
                .setUnitsPerCase(zeroIfNull(line.getUnitsPerCase()))
                .setUnitsPerBall(zeroIfNull(line.getUnitsPerBall()))
                .build();
    }
}

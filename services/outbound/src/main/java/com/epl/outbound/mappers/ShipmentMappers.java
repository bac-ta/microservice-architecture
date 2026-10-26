package com.epl.outbound.mappers;

import com.epl.common.v1.Party;
import com.epl.common.v1.PickKeys;
import com.epl.common.v1.Quantity;
import com.epl.outbound.entities.PickKeyColumns;
import com.epl.outbound.entities.ShipmentEntity;
import com.epl.outbound.entities.ShipmentLineEntity;
import com.epl.outbound.v1.Shipment;
import com.epl.outbound.v1.ShipmentLine;
import com.epl.service.core.Errors;

import java.util.Map;

import static com.epl.service.core.Protos.audit;
import static com.epl.service.core.Protos.bigDecimal;
import static com.epl.service.core.Protos.bigDecimalOrZero;
import static com.epl.service.core.Protos.date;
import static com.epl.service.core.Protos.decimal;
import static com.epl.service.core.Protos.emptyIfNull;
import static com.epl.service.core.Protos.localDate;
import static com.epl.service.core.Protos.map;
import static com.epl.service.core.Protos.merge;
import static com.epl.service.core.Protos.nullIfEmpty;
import static com.epl.service.core.Protos.nullIfZero;
import static com.epl.service.core.Protos.struct;
import static com.epl.service.core.Protos.zeroIfNull;

public final class ShipmentMappers {

    private ShipmentMappers() {
    }

    /** Header only (Search); add the lines with {@link #shipmentWithLines}. */
    public static Shipment shipment(ShipmentEntity entity) {
        var batch = entity.getPickingBatch();
        return Shipment.newBuilder()
                .setShipmentNo(entity.getShipmentNo())
                .setScheduledDate(date(entity.getScheduledDate()))
                .setTransactionKind(entity.getTransactionKind())
                .setStatus(entity.getStatus())
                .setShippedDate(date(entity.getShippedDate()))
                .setConfirmedDate(date(entity.getConfirmedDate()))
                .setBatchNo(batch == null ? "" : batch.getBatchNo())
                .setTotalPickingNo(batch == null ? "" : emptyIfNull(batch.getTotalPickingNo()))
                .setInvoiceNo(emptyIfNull(entity.getInvoiceNo()))
                .setCustomerRef(emptyIfNull(entity.getCustomerRef()))
                .setPoNo(emptyIfNull(entity.getPoNo()))
                .setShipTo(party(entity.getShipToCode(), entity.getShipTo()))
                .setFinalShipTo(party(entity.getFinalShipToCode(), entity.getFinalShipTo()))
                .setShipper(party(entity.getShipperCode(), entity.getShipper()))
                .setBillTo(party(entity.getBillToCode(), entity.getBillTo()))
                .setCarrierCode(emptyIfNull(entity.getCarrierCode()))
                .setWarehouseInstructions(emptyIfNull(entity.getWarehouseInstructions()))
                .setDeliveryNoteRemarks(emptyIfNull(entity.getDeliveryNoteRemarks()))
                .setWaybillRemarks(emptyIfNull(entity.getWaybillRemarks()))
                .setTotalWeight(decimal(entity.getTotalWeight()))
                .setTotalVolume(decimal(entity.getTotalVolume()))
                .setExt(struct(entity.getExt()))
                .setAudit(audit(entity))
                .build();
    }

    public static Shipment shipmentWithLines(ShipmentEntity entity) {
        return shipment(entity).toBuilder()
                .addAllLines(entity.getLines().stream().map(ShipmentMappers::line).toList())
                .build();
    }

    public static void apply(ShipmentEntity entity, Shipment request) {
        entity.setScheduledDate(localDate(request.getScheduledDate()));
        if (entity.getScheduledDate() == null) {
            throw Errors.invalid("scheduled_date is required");
        }
        entity.setTransactionKind(Errors.required(request.getTransactionKind(), "transaction_kind"));
        entity.setInvoiceNo(nullIfEmpty(request.getInvoiceNo()));
        entity.setCustomerRef(nullIfEmpty(request.getCustomerRef()));
        entity.setPoNo(nullIfEmpty(request.getPoNo()));
        entity.setShipToCode(nullIfEmpty(request.getShipTo().getCode()));
        entity.setShipTo(snapshot(request.getShipTo()));
        entity.setFinalShipToCode(nullIfEmpty(request.getFinalShipTo().getCode()));
        entity.setFinalShipTo(snapshot(request.getFinalShipTo()));
        entity.setShipperCode(nullIfEmpty(request.getShipper().getCode()));
        entity.setShipper(snapshot(request.getShipper()));
        entity.setBillToCode(nullIfEmpty(request.getBillTo().getCode()));
        entity.setBillTo(snapshot(request.getBillTo()));
        entity.setCarrierCode(nullIfEmpty(request.getCarrierCode()));
        entity.setWarehouseInstructions(nullIfEmpty(request.getWarehouseInstructions()));
        entity.setDeliveryNoteRemarks(nullIfEmpty(request.getDeliveryNoteRemarks()));
        entity.setWaybillRemarks(nullIfEmpty(request.getWaybillRemarks()));
        entity.setTotalWeight(bigDecimal(request.getTotalWeight()));
        entity.setTotalVolume(bigDecimal(request.getTotalVolume()));
        entity.setExt(map(request.getExt()));
    }

    public static ShipmentLine line(ShipmentLineEntity entity) {
        return ShipmentLine.newBuilder()
                .setLineNo(entity.getLineNo())
                .setStatus(entity.getStatus())
                .setProductCode(entity.getProductCode())
                .setProductName(emptyIfNull(entity.getProductName()))
                .setOriginCountry(emptyIfNull(entity.getOriginCountry()))
                .setOrdered(Quantity.newBuilder()
                        .setCases(entity.getOrderedCaseQty())
                        .setBalls(entity.getOrderedBallQty())
                        .setPieces(decimal(entity.getOrderedPieceQty()))
                        .setTotalPieces(decimal(entity.getOrderedTotalQty()))
                        .setUnitsPerCase(zeroIfNull(entity.getUnitsPerCase()))
                        .setUnitsPerBall(zeroIfNull(entity.getUnitsPerBall())))
                .setAllocated(Quantity.newBuilder().setTotalPieces(decimal(entity.getAllocatedTotalQty())))
                .setShipped(Quantity.newBuilder().setTotalPieces(decimal(entity.getShippedTotalQty())))
                .setSubInventoryCode(emptyIfNull(entity.getSubInventoryCode()))
                .setPickKeys(pickKeys(entity.getPickKeys()))
                .setAllocationKind(emptyIfNull(entity.getAllocationKind()))
                .setNotShippable(entity.isNotShippable())
                .setCustomerLineSeq(zeroIfNull(entity.getCustomerLineSeq()))
                .setWarehouseInstructions(emptyIfNull(entity.getWarehouseInstructions()))
                .setDeliveryNoteRemarks(emptyIfNull(entity.getDeliveryNoteRemarks()))
                .setExt(struct(entity.getExt()))
                .build();
    }

    /** total_pieces is authoritative (epl.common.v1.Quantity). */
    public static void apply(ShipmentLineEntity entity, ShipmentLine request) {
        entity.setProductCode(Errors.required(request.getProductCode(), "lines.product_code"));
        entity.setProductName(nullIfEmpty(request.getProductName()));
        entity.setOriginCountry(nullIfEmpty(request.getOriginCountry()));
        var ordered = request.getOrdered();
        var total = bigDecimal(ordered.getTotalPieces());
        if (total == null || total.signum() <= 0) {
            throw Errors.invalid("lines.ordered.total_pieces must be > 0 (line " + request.getLineNo() + ")");
        }
        entity.setOrderedTotalQty(total);
        entity.setOrderedCaseQty(ordered.getCases());
        entity.setOrderedBallQty(ordered.getBalls());
        entity.setOrderedPieceQty(bigDecimalOrZero(ordered.getPieces()));
        entity.setUnitsPerCase(nullIfZero(ordered.getUnitsPerCase()));
        entity.setUnitsPerBall(nullIfZero(ordered.getUnitsPerBall()));
        entity.setSubInventoryCode(nullIfEmpty(request.getSubInventoryCode()));
        if (entity.getPickKeys() == null) {
            entity.setPickKeys(new PickKeyColumns());
        }
        apply(entity.getPickKeys(), request.getPickKeys());
        entity.setAllocationKind(nullIfEmpty(request.getAllocationKind()));
        entity.setNotShippable(request.getNotShippable());
        entity.setCustomerLineSeq(nullIfZero(request.getCustomerLineSeq()));
        entity.setWarehouseInstructions(nullIfEmpty(request.getWarehouseInstructions()));
        entity.setDeliveryNoteRemarks(nullIfEmpty(request.getDeliveryNoteRemarks()));
        entity.setExt(map(request.getExt()));
    }

    /** Hibernate loads the embeddable as null when all seven columns are null. */
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

    private static void apply(PickKeyColumns columns, PickKeys keys) {
        columns.setKey1(nullIfEmpty(keys.getKey1()));
        columns.setKey2(nullIfEmpty(keys.getKey2()));
        columns.setKey3(nullIfEmpty(keys.getKey3()));
        columns.setKey4(nullIfEmpty(keys.getKey4()));
        columns.setKey5(nullIfEmpty(keys.getKey5()));
        columns.setKey6(nullIfEmpty(keys.getKey6()));
        columns.setKey7(nullIfEmpty(keys.getKey7()));
    }

    private static Party party(String code, Map<String, Object> snapshot) {
        return merge(snapshot, Party.newBuilder()).setCode(emptyIfNull(code)).build();
    }

    private static Map<String, Object> snapshot(Party party) {
        return map(party.toBuilder().clearCode());
    }
}

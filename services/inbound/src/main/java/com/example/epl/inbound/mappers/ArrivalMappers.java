package com.example.epl.inbound.mappers;

import com.example.epl.common.v1.Party;
import com.example.epl.common.v1.PickKeys;
import com.example.epl.common.v1.Quantity;
import com.example.epl.inbound.entities.ArrivalEntity;
import com.example.epl.inbound.entities.ArrivalLineEntity;
import com.example.epl.inbound.entities.PickKeyColumns;
import com.example.epl.inbound.v1.Arrival;
import com.example.epl.inbound.v1.ArrivalLine;
import com.example.service.core.Errors;

import java.math.BigDecimal;

import static com.example.service.core.Protos.audit;
import static com.example.service.core.Protos.bigDecimal;
import static com.example.service.core.Protos.bigDecimalOrZero;
import static com.example.service.core.Protos.date;
import static com.example.service.core.Protos.decimal;
import static com.example.service.core.Protos.emptyIfNull;
import static com.example.service.core.Protos.localDate;
import static com.example.service.core.Protos.map;
import static com.example.service.core.Protos.merge;
import static com.example.service.core.Protos.nullIfEmpty;
import static com.example.service.core.Protos.nullIfZero;
import static com.example.service.core.Protos.struct;
import static com.example.service.core.Protos.zeroIfNull;

public final class ArrivalMappers {

    private ArrivalMappers() {
    }

    /** Header only (Search); add the lines with {@link #arrivalWithLines}. */
    public static Arrival arrival(ArrivalEntity entity) {
        return Arrival.newBuilder()
                .setArrivalNo(entity.getArrivalNo())
                .setScheduledDate(date(entity.getScheduledDate()))
                .setTransactionKind(entity.getTransactionKind())
                .setStatus(entity.getStatus())
                .setArrivedDate(date(entity.getArrivedDate()))
                .setConfirmedDate(date(entity.getConfirmedDate()))
                .setInvoiceNo(emptyIfNull(entity.getInvoiceNo()))
                .setCustomerRef(emptyIfNull(entity.getCustomerRef()))
                .setPoNo(emptyIfNull(entity.getPoNo()))
                .setSupplier(merge(entity.getSupplier(), Party.newBuilder()).setCode(emptyIfNull(entity.getSupplierCode())))
                .setBonded(entity.isBonded())
                .setBlNo(emptyIfNull(entity.getBlNo()))
                .setContainerNo(emptyIfNull(entity.getContainerNo()))
                .setTransportCode(emptyIfNull(entity.getTransportCode()))
                .setReturnShipmentNo(emptyIfNull(entity.getReturnShipmentNo()))
                .setRemarks(emptyIfNull(entity.getRemarks()))
                .setExt(struct(entity.getExt()))
                .setAudit(audit(entity))
                .build();
    }

    public static Arrival arrivalWithLines(ArrivalEntity entity) {
        return arrival(entity).toBuilder()
                .addAllLines(entity.getLines().stream().map(ArrivalMappers::line).toList())
                .build();
    }

    public static void apply(ArrivalEntity entity, Arrival request) {
        entity.setScheduledDate(localDate(request.getScheduledDate()));
        if (entity.getScheduledDate() == null) {
            throw Errors.invalid("scheduled_date is required");
        }
        entity.setTransactionKind(Errors.required(request.getTransactionKind(), "transaction_kind"));
        entity.setInvoiceNo(nullIfEmpty(request.getInvoiceNo()));
        entity.setCustomerRef(nullIfEmpty(request.getCustomerRef()));
        entity.setPoNo(nullIfEmpty(request.getPoNo()));
        entity.setSupplierCode(nullIfEmpty(request.getSupplier().getCode()));
        entity.setSupplier(map(request.getSupplier().toBuilder().clearCode()));
        entity.setBonded(request.getBonded());
        entity.setBlNo(nullIfEmpty(request.getBlNo()));
        entity.setContainerNo(nullIfEmpty(request.getContainerNo()));
        entity.setTransportCode(nullIfEmpty(request.getTransportCode()));
        entity.setReturnShipmentNo(nullIfEmpty(request.getReturnShipmentNo()));
        entity.setRemarks(nullIfEmpty(request.getRemarks()));
        entity.setExt(map(request.getExt()));
    }

    public static ArrivalLine line(ArrivalLineEntity entity) {
        return ArrivalLine.newBuilder()
                .setLineNo(entity.getLineNo())
                .setStatus(entity.getStatus())
                .setInspected(entity.isInspected())
                .setProductCode(entity.getProductCode())
                .setProductName(emptyIfNull(entity.getProductName()))
                .setOriginCountry(emptyIfNull(entity.getOriginCountry()))
                .setSupplierProductCode(emptyIfNull(entity.getSupplierProductCode()))
                .setPlanned(Quantity.newBuilder()
                        .setCases(entity.getPlannedCaseQty())
                        .setBalls(entity.getPlannedBallQty())
                        .setPieces(decimal(entity.getPlannedPieceQty()))
                        .setTotalPieces(decimal(entity.getPlannedTotalQty()))
                        .setUnitsPerCase(zeroIfNull(entity.getUnitsPerCase()))
                        .setUnitsPerBall(zeroIfNull(entity.getUnitsPerBall())))
                .setReceived(Quantity.newBuilder().setTotalPieces(decimal(entity.getReceivedTotalQty())))
                .setSubInventoryCode(emptyIfNull(entity.getSubInventoryCode()))
                .setPickKeys(pickKeys(entity.getPickKeys()))
                .setOnHold(entity.isOnHold())
                .setCustomerLineSeq(zeroIfNull(entity.getCustomerLineSeq()))
                .setRemarks(emptyIfNull(entity.getRemarks()))
                .setExt(struct(entity.getExt()))
                .build();
    }

    /** total_pieces is authoritative (epl.common.v1.Quantity). */
    public static void apply(ArrivalLineEntity entity, ArrivalLine request) {
        entity.setProductCode(Errors.required(request.getProductCode(), "lines.product_code"));
        entity.setProductName(nullIfEmpty(request.getProductName()));
        entity.setOriginCountry(nullIfEmpty(request.getOriginCountry()));
        entity.setSupplierProductCode(nullIfEmpty(request.getSupplierProductCode()));
        var planned = request.getPlanned();
        var total = bigDecimal(planned.getTotalPieces());
        if (total == null || total.signum() <= 0) {
            throw Errors.invalid("lines.planned.total_pieces must be > 0 (line " + request.getLineNo() + ")");
        }
        entity.setPlannedTotalQty(total);
        entity.setPlannedCaseQty(planned.getCases());
        entity.setPlannedBallQty(planned.getBalls());
        entity.setPlannedPieceQty(bigDecimalOrZero(planned.getPieces()));
        entity.setUnitsPerCase(nullIfZero(planned.getUnitsPerCase()));
        entity.setUnitsPerBall(nullIfZero(planned.getUnitsPerBall()));
        entity.setSubInventoryCode(nullIfEmpty(request.getSubInventoryCode()));
        if (entity.getPickKeys() == null) {
            entity.setPickKeys(new PickKeyColumns());
        }
        apply(entity.getPickKeys(), request.getPickKeys());
        entity.setOnHold(request.getOnHold());
        entity.setCustomerLineSeq(nullIfZero(request.getCustomerLineSeq()));
        entity.setRemarks(nullIfEmpty(request.getRemarks()));
        entity.setExt(map(request.getExt()));
        if (entity.getReceivedTotalQty() == null) {
            entity.setReceivedTotalQty(BigDecimal.ZERO);
        }
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
}

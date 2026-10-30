package com.epl.outbound.services;

import com.epl.outbound.entities.ShipmentEntity;
import com.epl.outbound.entities.ShipmentLineEntity;
import com.epl.outbound.mappers.ShipmentMappers;
import com.epl.outbound.repositories.ShipmentRepository;
import com.epl.outbound.v1.SearchShipmentsRequest;
import com.epl.outbound.v1.SearchShipmentsResponse;
import com.epl.outbound.v1.Shipment;
import com.epl.outbound.v1.ShipmentLine;
import com.epl.service.core.DocumentNumbers;
import com.epl.service.core.Errors;
import com.epl.service.core.Pages;
import com.epl.service.core.RequestContext;
import com.epl.service.core.ResponseMask;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.epl.service.core.Protos.localDate;

/**
 * Shipping orders of the tenant: entry, change and cancellation while nothing is reserved yet.
 * Allocation, picking, packing and confirmation are separate RPCs.
 */
@RequiredArgsConstructor
@Service
public class ShipmentService {

    private final ShipmentRepository shipmentRepository;
    private final DocumentNumbers documentNumbers;

    @Transactional
    public Shipment create(Shipment request) {
        var context = RequestContext.current();
        var tenantId = context.requireTenant();
        var entity = new ShipmentEntity();
        entity.setTenantId(tenantId);
        entity.setWarehouseId(context.warehouseId());
        entity.setShipmentNo(request.getShipmentNo().isBlank() ? documentNumbers.next(tenantId, "SHIPMENT", "SH") : request.getShipmentNo());
        entity.setStatus("ORDERED");
        ShipmentMappers.apply(entity, request);
        mergeLines(entity, request.getLinesList());
        return ShipmentMappers.shipmentWithLines(shipmentRepository.saveAndFlush(entity));
    }

    @Transactional(readOnly = true)
    public Shipment get(String shipmentNo) {
        if (!ResponseMask.current().includes("lines")) {
            // the caller does not read the lines (GraphQL selection): no join
            var tenantId = RequestContext.current().requireTenant();
            return ShipmentMappers.shipment(shipmentRepository.findByTenantIdAndShipmentNo(tenantId, Errors.required(shipmentNo, "shipment_no"))
                    .orElseThrow(() -> Errors.notFound("Shipment", shipmentNo)));
        }
        return ShipmentMappers.shipmentWithLines(find(shipmentNo));
    }

    @Transactional(readOnly = true)
    public SearchShipmentsResponse search(SearchShipmentsRequest request) {
        var tenantId = RequestContext.current().requireTenant();
        var sort = Sort.by(Sort.Direction.DESC, "scheduledDate").and(Sort.by("shipmentNo"));
        var page = shipmentRepository.findAll(specification(tenantId, request), Pages.pageable(request.getPage(), sort));
        return SearchShipmentsResponse.newBuilder()
                .addAllShipments(page.map(ShipmentMappers::shipment).getContent())
                .setPage(Pages.response(page))
                .build();
    }

    /** Header and lines, while ORDERED. Lines are matched by line_no: missing ones are removed. */
    @Transactional
    public Shipment update(Shipment request) {
        var entity = find(request.getShipmentNo());
        Errors.checkVersion(entity.getVersion(), request.getAudit().getVersion());
        requireOrdered(entity, "changed");
        ShipmentMappers.apply(entity, request);
        mergeLines(entity, request.getLinesList());
        return ShipmentMappers.shipmentWithLines(shipmentRepository.saveAndFlush(entity));
    }

    /** Only while ORDERED: an allocated shipment must release its stock first (AllocationService/CancelAllocation). */
    @Transactional
    public Shipment cancel(String shipmentNo, String reasonCode, int version) {
        var entity = find(shipmentNo);
        Errors.checkVersion(entity.getVersion(), version);
        requireOrdered(entity, "cancelled (cancel its allocation first)");
        entity.setCancelReasonCode(Errors.required(reasonCode, "reason_code"));
        entity.setStatus("CANCELLED");
        entity.getLines().forEach(line -> line.setStatus("CANCELLED"));
        return ShipmentMappers.shipmentWithLines(shipmentRepository.saveAndFlush(entity));
    }

    @Transactional
    public void delete(String shipmentNo, int version) {
        var entity = find(shipmentNo);
        Errors.checkVersion(entity.getVersion(), version);
        requireOrdered(entity, "deleted (cancel it instead)");
        shipmentRepository.delete(entity);
    }

    private ShipmentEntity find(String shipmentNo) {
        var tenantId = RequestContext.current().requireTenant();
        return shipmentRepository.findWithLinesByTenantIdAndShipmentNo(tenantId, Errors.required(shipmentNo, "shipment_no"))
                .orElseThrow(() -> Errors.notFound("Shipment", shipmentNo));
    }

    private static void requireOrdered(ShipmentEntity entity, String action) {
        if (!"ORDERED".equals(entity.getStatus())) {
            throw Errors.failedPrecondition("Shipment " + entity.getShipmentNo() + " is " + entity.getStatus()
                    + "; only an ORDERED shipment can be " + action);
        }
    }

    /** Lines without line_no get the next free number. */
    private static void mergeLines(ShipmentEntity entity, List<ShipmentLine> requested) {
        if (requested.isEmpty()) {
            throw Errors.invalid("A shipment needs at least one line");
        }
        var seen = new HashSet<Integer>();
        var next = requested.stream().mapToInt(ShipmentLine::getLineNo).max().orElse(0);
        var numbered = new ArrayList<ShipmentLine>();
        for (var line : requested) {
            var numberedLine = line.getLineNo() > 0 ? line : line.toBuilder().setLineNo(++next).build();
            if (!seen.add(numberedLine.getLineNo())) {
                throw Errors.invalid("Duplicate line_no " + numberedLine.getLineNo());
            }
            numbered.add(numberedLine);
        }
        var existing = entity.getLines().stream().collect(Collectors.toMap(ShipmentLineEntity::getLineNo, Function.identity()));
        entity.getLines().removeIf(line -> !seen.contains(line.getLineNo()));
        for (var line : numbered) {
            var lineEntity = existing.get(line.getLineNo());
            if (lineEntity == null) {
                lineEntity = new ShipmentLineEntity();
                lineEntity.setTenantId(entity.getTenantId());
                lineEntity.setShipment(entity);
                lineEntity.setLineNo(line.getLineNo());
                lineEntity.setStatus("ORDERED");
                entity.getLines().add(lineEntity);
            }
            ShipmentMappers.apply(lineEntity, line);
        }
        entity.getLines().sort((a, b) -> Integer.compare(a.getLineNo(), b.getLineNo()));
    }

    private static Specification<ShipmentEntity> specification(UUID tenantId, SearchShipmentsRequest request) {
        return (root, query, cb) -> {
            var where = new ArrayList<Predicate>();
            where.add(cb.equal(root.get("tenantId"), tenantId));
            var from = localDate(request.getScheduledFrom());
            if (from != null) {
                where.add(cb.greaterThanOrEqualTo(root.get("scheduledDate"), from));
            }
            var to = localDate(request.getScheduledTo());
            if (to != null) {
                where.add(cb.lessThanOrEqualTo(root.get("scheduledDate"), to));
            }
            if (request.getStatusesCount() > 0) {
                where.add(root.get("status").in(request.getStatusesList()));
            }
            if (!request.getInvoiceNo().isBlank()) {
                where.add(cb.equal(root.get("invoiceNo"), request.getInvoiceNo()));
            }
            if (!request.getCustomerRef().isBlank()) {
                where.add(cb.equal(root.get("customerRef"), request.getCustomerRef()));
            }
            if (!request.getShipToCode().isBlank()) {
                where.add(cb.equal(root.get("shipToCode"), request.getShipToCode()));
            }
            if (!request.getBatchNo().isBlank()) {
                where.add(cb.equal(root.join("pickingBatch").get("batchNo"), request.getBatchNo()));
            }
            if (!request.getProductCode().isBlank()) {
                var lines = query.subquery(Integer.class);
                var line = lines.from(ShipmentLineEntity.class);
                lines.select(cb.literal(1)).where(cb.equal(line.get("shipment"), root),
                        cb.equal(line.get("productCode"), request.getProductCode()));
                where.add(cb.exists(lines));
            }
            return cb.and(where.toArray(Predicate[]::new));
        };
    }
}

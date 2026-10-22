package com.example.epl.inbound.services;

import com.example.epl.inbound.entities.ArrivalEntity;
import com.example.epl.inbound.entities.ArrivalLineEntity;
import com.example.epl.inbound.mappers.ArrivalMappers;
import com.example.epl.inbound.repositories.ArrivalRepository;
import com.example.epl.inbound.v1.Arrival;
import com.example.epl.inbound.v1.ArrivalLine;
import com.example.epl.inbound.v1.SearchArrivalsRequest;
import com.example.epl.inbound.v1.SearchArrivalsResponse;
import com.example.service.core.DocumentNumbers;
import com.example.service.core.Errors;
import com.example.service.core.Pages;
import com.example.service.core.RequestContext;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.example.service.core.Protos.localDate;

/** Arrival documents of the tenant: entry, change and cancellation (inspection etc. are separate RPCs). */
@Service
public class ArrivalService {

    private static final Set<String> CANCELLABLE = Set.of("PLANNED", "INSPECTING", "INSPECTED");

    private final ArrivalRepository arrivalRepository;
    private final DocumentNumbers documentNumbers;

    public ArrivalService(ArrivalRepository arrivalRepository, DocumentNumbers documentNumbers) {
        this.arrivalRepository = arrivalRepository;
        this.documentNumbers = documentNumbers;
    }

    @Transactional
    public Arrival create(Arrival request) {
        var context = RequestContext.current();
        var tenantId = context.requireTenant();
        var entity = new ArrivalEntity();
        entity.setTenantId(tenantId);
        entity.setWarehouseId(context.warehouseId());
        entity.setArrivalNo(request.getArrivalNo().isBlank() ? documentNumbers.next(tenantId, "ARRIVAL", "AR") : request.getArrivalNo());
        entity.setStatus("PLANNED");
        ArrivalMappers.apply(entity, request);
        mergeLines(entity, request.getLinesList());
        return ArrivalMappers.arrivalWithLines(arrivalRepository.saveAndFlush(entity));
    }

    @Transactional(readOnly = true)
    public Arrival get(String arrivalNo) {
        return ArrivalMappers.arrivalWithLines(find(arrivalNo));
    }

    @Transactional(readOnly = true)
    public SearchArrivalsResponse search(SearchArrivalsRequest request) {
        var tenantId = RequestContext.current().requireTenant();
        var sort = Sort.by(Sort.Direction.DESC, "scheduledDate").and(Sort.by("arrivalNo"));
        var page = arrivalRepository.findAll(specification(tenantId, request), Pages.pageable(request.getPage(), sort));
        return SearchArrivalsResponse.newBuilder()
                .addAllArrivals(page.map(ArrivalMappers::arrival).getContent())
                .setPage(Pages.response(page))
                .build();
    }

    /** Header and lines, while PLANNED. Lines are matched by line_no: missing ones are removed. */
    @Transactional
    public Arrival update(Arrival request) {
        var entity = find(request.getArrivalNo());
        Errors.checkVersion(entity.getVersion(), request.getAudit().getVersion());
        requirePlanned(entity, "changed");
        ArrivalMappers.apply(entity, request);
        mergeLines(entity, request.getLinesList());
        return ArrivalMappers.arrivalWithLines(arrivalRepository.saveAndFlush(entity));
    }

    @Transactional
    public Arrival cancel(String arrivalNo, String reasonCode, int version) {
        var entity = find(arrivalNo);
        Errors.checkVersion(entity.getVersion(), version);
        if (!CANCELLABLE.contains(entity.getStatus())) {
            throw Errors.failedPrecondition("Arrival " + arrivalNo + " is " + entity.getStatus());
        }
        entity.setCancelReasonCode(Errors.required(reasonCode, "reason_code"));
        entity.setStatus("CANCELLED");
        entity.getLines().forEach(line -> line.setStatus("CANCELLED"));
        return ArrivalMappers.arrivalWithLines(arrivalRepository.saveAndFlush(entity));
    }

    @Transactional
    public void delete(String arrivalNo, int version) {
        var entity = find(arrivalNo);
        Errors.checkVersion(entity.getVersion(), version);
        requirePlanned(entity, "deleted (cancel it instead)");
        arrivalRepository.delete(entity);
    }

    private ArrivalEntity find(String arrivalNo) {
        var tenantId = RequestContext.current().requireTenant();
        return arrivalRepository.findWithLinesByTenantIdAndArrivalNo(tenantId, Errors.required(arrivalNo, "arrival_no"))
                .orElseThrow(() -> Errors.notFound("Arrival", arrivalNo));
    }

    private static void requirePlanned(ArrivalEntity entity, String action) {
        if (!"PLANNED".equals(entity.getStatus())) {
            throw Errors.failedPrecondition("Arrival " + entity.getArrivalNo() + " is " + entity.getStatus()
                    + "; only a PLANNED arrival can be " + action);
        }
    }

    /** Lines without line_no get the next free number. */
    private static void mergeLines(ArrivalEntity entity, List<ArrivalLine> requested) {
        if (requested.isEmpty()) {
            throw Errors.invalid("An arrival needs at least one line");
        }
        var seen = new HashSet<Integer>();
        var next = requested.stream().mapToInt(ArrivalLine::getLineNo).max().orElse(0);
        var numbered = new ArrayList<ArrivalLine>();
        for (var line : requested) {
            var numberedLine = line.getLineNo() > 0 ? line : line.toBuilder().setLineNo(++next).build();
            if (!seen.add(numberedLine.getLineNo())) {
                throw Errors.invalid("Duplicate line_no " + numberedLine.getLineNo());
            }
            numbered.add(numberedLine);
        }
        var existing = entity.getLines().stream().collect(Collectors.toMap(ArrivalLineEntity::getLineNo, Function.identity()));
        entity.getLines().removeIf(line -> !seen.contains(line.getLineNo()));
        for (var line : numbered) {
            var lineEntity = existing.get(line.getLineNo());
            if (lineEntity == null) {
                lineEntity = new ArrivalLineEntity();
                lineEntity.setTenantId(entity.getTenantId());
                lineEntity.setArrival(entity);
                lineEntity.setLineNo(line.getLineNo());
                lineEntity.setStatus("PLANNED");
                entity.getLines().add(lineEntity);
            }
            ArrivalMappers.apply(lineEntity, line);
        }
        entity.getLines().sort((a, b) -> Integer.compare(a.getLineNo(), b.getLineNo()));
    }

    private static Specification<ArrivalEntity> specification(UUID tenantId, SearchArrivalsRequest request) {
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
            if (!request.getSupplierCode().isBlank()) {
                where.add(cb.equal(root.get("supplierCode"), request.getSupplierCode()));
            }
            if (!request.getProductCode().isBlank()) {
                var lines = query.subquery(Integer.class);
                var line = lines.from(ArrivalLineEntity.class);
                lines.select(cb.literal(1)).where(cb.equal(line.get("arrival"), root),
                        cb.equal(line.get("productCode"), request.getProductCode()));
                where.add(cb.exists(lines));
            }
            return cb.and(where.toArray(Predicate[]::new));
        };
    }
}

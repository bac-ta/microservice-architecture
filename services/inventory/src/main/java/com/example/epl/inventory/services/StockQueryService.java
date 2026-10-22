package com.example.epl.inventory.services;

import com.example.epl.common.v1.PickKeys;
import com.example.epl.inventory.entities.StockEntity;
import com.example.epl.inventory.mappers.InventoryMappers;
import com.example.epl.inventory.repositories.StockRepository;
import com.example.epl.inventory.v1.SearchStockRequest;
import com.example.epl.inventory.v1.SearchStockResponse;
import com.example.epl.inventory.v1.StockLot;
import com.example.service.core.Errors;
import com.example.service.core.Pages;
import com.example.service.core.RequestContext;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.example.service.core.Protos.uuid;

/**
 * Stock inquiry. Stock rows are only changed by the stock operations (adjust, relocate …),
 * never by plain CRUD, so the ledger always explains the quantities.
 */
@Service
public class StockQueryService {

    private final StockRepository stockRepository;

    public StockQueryService(StockRepository stockRepository) {
        this.stockRepository = stockRepository;
    }

    @Transactional(readOnly = true)
    public SearchStockResponse search(SearchStockRequest request) {
        var tenantId = RequestContext.current().requireTenant();
        var sort = Sort.by("productCode", "locationCode", "receiptNo", "receiptLine", "receiptSeq");
        var page = stockRepository.findAll(specification(tenantId, request), Pages.pageable(request.getPage(), sort));
        return SearchStockResponse.newBuilder()
                .addAllLots(page.map(InventoryMappers::stockLot).getContent())
                .setPage(Pages.response(page))
                .build();
    }

    @Transactional(readOnly = true)
    public StockLot get(String id) {
        var tenantId = RequestContext.current().requireTenant();
        return stockRepository.findByTenantIdAndId(tenantId, uuid(id, "id"))
                .map(InventoryMappers::stockLot)
                .orElseThrow(() -> Errors.notFound("Stock", id));
    }

    private static Specification<StockEntity> specification(UUID tenantId, SearchStockRequest request) {
        return (root, query, cb) -> {
            var where = new ArrayList<jakarta.persistence.criteria.Predicate>();
            where.add(cb.equal(root.get("tenantId"), tenantId));
            if (!request.getProductCode().isBlank()) {
                where.add(cb.equal(root.get("productCode"), request.getProductCode()));
            }
            if (!request.getLocationCodeFrom().isBlank()) {
                where.add(cb.greaterThanOrEqualTo(root.get("locationCode"), request.getLocationCodeFrom()));
            }
            if (!request.getLocationCodeTo().isBlank()) {
                where.add(cb.lessThanOrEqualTo(root.get("locationCode"), request.getLocationCodeTo()));
            }
            if (!request.getAreaCode().isBlank()) {
                where.add(cb.equal(root.get("areaCode"), request.getAreaCode()));
            }
            if (!request.getSubInventoryCode().isBlank()) {
                where.add(cb.equal(root.get("subInventoryCode"), request.getSubInventoryCode()));
            }
            if (request.hasReceipt() && !request.getReceipt().getReceiptNo().isBlank()) {
                where.add(cb.equal(root.get("receiptNo"), request.getReceipt().getReceiptNo()));
                if (request.getReceipt().getLineNo() > 0) {
                    where.add(cb.equal(root.get("receiptLine"), request.getReceipt().getLineNo()));
                }
            }
            var keys = pickKeyFilters(request.getPickKeys());
            for (int i = 0; i < keys.size(); i++) {
                if (!keys.get(i).isBlank()) {
                    where.add(cb.equal(root.get("pickKeys").get("key" + (i + 1)), keys.get(i)));
                }
            }
            if (request.getOnlyAvailable()) {
                where.add(cb.greaterThan(root.get("availableQty"), BigDecimal.ZERO));
                where.add(cb.isFalse(root.get("onHold")));
                where.add(cb.isFalse(root.get("locked")));
            } else if (!request.getIncludeZero()) {
                where.add(cb.greaterThan(root.get("physicalQty"), BigDecimal.ZERO));
            }
            return cb.and(where.toArray(jakarta.persistence.criteria.Predicate[]::new));
        };
    }

    private static List<String> pickKeyFilters(PickKeys keys) {
        return List.of(keys.getKey1(), keys.getKey2(), keys.getKey3(), keys.getKey4(), keys.getKey5(), keys.getKey6(), keys.getKey7());
    }
}

package com.epl.inventory.services;

import com.epl.inventory.entities.StocktakeEntity;
import com.epl.inventory.mappers.InventoryMappers;
import com.epl.inventory.repositories.StocktakeLineRepository;
import com.epl.inventory.repositories.StocktakeRepository;
import com.epl.inventory.v1.ListStocktakesRequest;
import com.epl.inventory.v1.ListStocktakesResponse;
import com.epl.inventory.v1.Stocktake;
import com.epl.inventory.v1.StocktakeStatus;
import com.epl.service.core.DocumentNumbers;
import com.epl.service.core.Errors;
import com.epl.service.core.Pages;
import com.epl.service.core.RequestContext;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;

import static com.epl.service.core.Protos.localDate;
import static com.epl.service.core.Protos.map;

/**
 * Stocktake headers. Lines are produced from stock when counting starts and filled by RecordCount;
 * confirmation adjusts stock — both are stock operations, not CRUD.
 */
@Service
public class StocktakeService {

    private static final Set<String> CANCELLABLE = Set.of("INSTRUCTED", "COUNTING", "COUNTED");

    private final StocktakeRepository stocktakeRepository;
    private final StocktakeLineRepository lineRepository;
    private final DocumentNumbers documentNumbers;

    public StocktakeService(StocktakeRepository stocktakeRepository, StocktakeLineRepository lineRepository,
                            DocumentNumbers documentNumbers) {
        this.stocktakeRepository = stocktakeRepository;
        this.lineRepository = lineRepository;
        this.documentNumbers = documentNumbers;
    }

    @Transactional
    public Stocktake create(Stocktake request) {
        var context = RequestContext.current();
        var tenantId = context.requireTenant();
        var entity = new StocktakeEntity();
        entity.setTenantId(tenantId);
        entity.setWarehouseId(context.warehouseId());
        entity.setStocktakeNo(request.getStocktakeNo().isBlank()
                ? documentNumbers.next(tenantId, "STOCKTAKE", "ST")
                : request.getStocktakeNo());
        entity.setStatus("INSTRUCTED");
        apply(entity, request);
        return InventoryMappers.stocktake(stocktakeRepository.saveAndFlush(entity));
    }

    @Transactional(readOnly = true)
    public Stocktake get(String stocktakeNo, boolean onlyDifferences) {
        var entity = find(stocktakeNo);
        return InventoryMappers.stocktake(entity).toBuilder()
                .addAllLines(lineRepository.findLines(entity.getId(), onlyDifferences).stream()
                        .map(InventoryMappers::stocktakeLine).toList())
                .build();
    }

    @Transactional(readOnly = true)
    public ListStocktakesResponse list(ListStocktakesRequest request) {
        var tenantId = RequestContext.current().requireTenant();
        var statuses = request.getStatusesList().stream().map(InventoryMappers::status).toList();
        var page = stocktakeRepository.search(tenantId, statuses.isEmpty(), statuses.isEmpty() ? Set.of("") : statuses,
                Optional.ofNullable(localDate(request.getDateFrom())).orElse(LocalDate.of(1900, 1, 1)),
                Optional.ofNullable(localDate(request.getDateTo())).orElse(LocalDate.of(9999, 12, 31)),
                Pages.pageable(request.getPage(), Sort.by(Sort.Direction.DESC, "stocktakeDate").and(Sort.by("stocktakeNo"))));
        return ListStocktakesResponse.newBuilder()
                .addAllStocktakes(page.map(InventoryMappers::stocktake).getContent())
                .setPage(Pages.response(page))
                .build();
    }

    @Transactional
    public Stocktake update(Stocktake request) {
        var entity = find(request.getStocktakeNo());
        Errors.checkVersion(entity.getVersion(), request.getAudit().getVersion());
        if (!"INSTRUCTED".equals(entity.getStatus())) {
            throw Errors.failedPrecondition("Stocktake " + entity.getStocktakeNo() + " is " + entity.getStatus()
                    + "; only an INSTRUCTED stocktake can be changed");
        }
        apply(entity, request);
        return InventoryMappers.stocktake(stocktakeRepository.saveAndFlush(entity));
    }

    @Transactional
    public Stocktake cancel(String stocktakeNo, int version) {
        var entity = find(stocktakeNo);
        Errors.checkVersion(entity.getVersion(), version);
        if (!CANCELLABLE.contains(entity.getStatus())) {
            throw Errors.failedPrecondition("Stocktake " + stocktakeNo + " is " + entity.getStatus());
        }
        entity.setStatus(InventoryMappers.status(StocktakeStatus.STOCKTAKE_STATUS_CANCELLED));
        return InventoryMappers.stocktake(stocktakeRepository.saveAndFlush(entity));
    }

    private StocktakeEntity find(String stocktakeNo) {
        var tenantId = RequestContext.current().requireTenant();
        return stocktakeRepository.findByTenantIdAndStocktakeNo(tenantId, Errors.required(stocktakeNo, "stocktake_no"))
                .orElseThrow(() -> Errors.notFound("Stocktake", stocktakeNo));
    }

    private static void apply(StocktakeEntity entity, Stocktake request) {
        entity.setStocktakeDate(Optional.ofNullable(localDate(request.getStocktakeDate())).orElse(LocalDate.now()));
        entity.setKind(Errors.required(request.getKind(), "kind"));
        entity.setTargetKind(Errors.required(request.getTargetKind(), "target_kind"));
        entity.setIncludeEmptyLocations(request.getIncludeEmptyLocations());
        entity.setUpdateStockOnConfirm(request.getUpdateStockOnConfirm());
        entity.setCriteria(map(request.getCriteria()));
    }
}

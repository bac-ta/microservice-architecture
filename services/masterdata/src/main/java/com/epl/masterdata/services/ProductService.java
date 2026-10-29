package com.epl.masterdata.services;

import com.epl.masterdata.entities.ProductBarcodeEntity;
import com.epl.masterdata.entities.ProductEntity;
import com.epl.masterdata.entities.TenantCodeId;
import com.epl.masterdata.mappers.MasterdataMappers;
import com.epl.masterdata.repositories.ProductBarcodeRepository;
import com.epl.masterdata.repositories.ProductRepository;
import com.epl.masterdata.v1.ListProductsRequest;
import com.epl.masterdata.v1.ListProductsResponse;
import com.epl.masterdata.v1.Product;
import com.epl.masterdata.v1.ResolveBarcodeResponse;
import com.epl.service.core.Errors;
import com.epl.service.core.Pages;
import com.epl.service.core.RequestContext;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;

/** Products of the tenant in x-company / x-warehouse / x-customer, with their barcodes. */
@RequiredArgsConstructor
@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductBarcodeRepository barcodeRepository;

    @Transactional(readOnly = true)
    public Product get(String code) {
        return toProto(find(code));
    }

    @Transactional(readOnly = true)
    public ListProductsResponse list(ListProductsRequest request) {
        var tenantId = RequestContext.current().requireTenant();
        var pattern = request.getQuery().isBlank() ? "" : "%" + request.getQuery().trim().toLowerCase(Locale.ROOT) + "%";
        // the native query orders by code itself
        var page = productRepository.search(tenantId, pattern, request.getProductGroupCode(),
                Pages.pageable(request.getPage(), Sort.unsorted()));
        return ListProductsResponse.newBuilder()
                .addAllProducts(page.map(this::toProto).getContent())
                .setPage(Pages.response(page))
                .build();
    }

    /** Barcodes in the request replace the product's barcodes (all as PIECE level). */
    @Transactional
    public Product upsert(Product request) {
        var tenantId = RequestContext.current().requireTenant();
        var code = Errors.required(request.getCode(), "code");
        if (request.getNamesCount() < 1 || request.getNamesCount() > 4) {
            throw Errors.invalid("names must have 1 to 4 entries");
        }
        var entity = productRepository.findById(new TenantCodeId(tenantId, code)).orElse(null);
        var live = entity != null && entity.getDeletedAt() == null;
        Errors.checkUpsert("Product", code, live, request.hasAudit(), live ? entity.getVersion() : 0, request.getAudit().getVersion());
        if (entity == null) {
            entity = new ProductEntity();
            entity.setTenantId(tenantId);
            entity.setCode(code);
        }
        entity.setDeletedAt(null);
        MasterdataMappers.apply(entity, request);
        entity = productRepository.saveAndFlush(entity);

        barcodeRepository.deleteAllOfProduct(tenantId, code);
        barcodeRepository.flush();
        for (var barcode : new LinkedHashSet<>(request.getBarcodesList())) {
            var row = new ProductBarcodeEntity();
            row.setTenantId(tenantId);
            row.setBarcode(barcode);
            row.setProductCode(code);
            barcodeRepository.save(row);
        }
        barcodeRepository.flush();
        return toProto(entity);
    }

    /** Soft delete: stock and history keep the code; its barcodes stop resolving. */
    @Transactional
    public void delete(String code, int version) {
        var entity = find(code);
        Errors.checkVersion(entity.getVersion(), version);
        entity.setDeletedAt(Instant.now());
    }

    @Transactional(readOnly = true)
    public ResolveBarcodeResponse resolveBarcode(String barcode) {
        var tenantId = RequestContext.current().requireTenant();
        var row = barcodeRepository.findByTenantIdAndBarcode(tenantId, Errors.required(barcode, "barcode"))
                .orElseThrow(() -> Errors.notFound("Barcode", barcode));
        var product = productRepository.findByTenantIdAndCodeAndDeletedAtIsNull(tenantId, row.getProductCode())
                .orElseThrow(() -> Errors.notFound("Barcode", barcode));
        return ResolveBarcodeResponse.newBuilder().setProduct(toProto(product)).setUomLevel(row.getUomLevel()).build();
    }

    private ProductEntity find(String code) {
        var tenantId = RequestContext.current().requireTenant();
        return productRepository.findByTenantIdAndCodeAndDeletedAtIsNull(tenantId, Errors.required(code, "code"))
                .orElseThrow(() -> Errors.notFound("Product", code));
    }

    private Product toProto(ProductEntity entity) {
        List<String> barcodes = barcodeRepository.findAllByTenantIdAndProductCodeOrderByBarcode(entity.getTenantId(), entity.getCode())
                .stream().map(ProductBarcodeEntity::getBarcode).toList();
        return MasterdataMappers.product(entity, barcodes);
    }
}

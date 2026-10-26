package com.epl.masterdata.repositories;

import com.epl.masterdata.entities.ProductBarcodeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductBarcodeRepository extends JpaRepository<ProductBarcodeEntity, ProductBarcodeEntity.Id> {

    List<ProductBarcodeEntity> findAllByTenantIdAndProductCodeOrderByBarcode(UUID tenantId, String productCode);

    Optional<ProductBarcodeEntity> findByTenantIdAndBarcode(UUID tenantId, String barcode);

    @Modifying
    @Query("delete from ProductBarcodeEntity b where b.tenantId = :tenantId and b.productCode = :productCode")
    void deleteAllOfProduct(UUID tenantId, String productCode);
}

package com.example.epl.report.repositories;

import com.example.epl.report.entities.PrinterEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface PrinterRepository extends JpaRepository<PrinterEntity, PrinterEntity.Id> {

    /** kind '' = all */
    @Query("select p from PrinterEntity p where p.warehouseId = :warehouseId and (:kind = '' or p.kind = :kind) order by p.code")
    List<PrinterEntity> search(UUID warehouseId, String kind);
}

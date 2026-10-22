package com.example.epl.inventory.repositories;

import com.example.epl.inventory.entities.StocktakeLineEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface StocktakeLineRepository extends JpaRepository<StocktakeLineEntity, UUID> {

    @Query("""
            select l from StocktakeLineEntity l left join fetch l.lot
            where l.stocktakeId = :stocktakeId and (:onlyDifferences = false or l.diffTotalQty <> 0)
            order by l.locationCode, l.countNo
            """)
    List<StocktakeLineEntity> findLines(UUID stocktakeId, boolean onlyDifferences);
}

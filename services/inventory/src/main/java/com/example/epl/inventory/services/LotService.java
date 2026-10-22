package com.example.epl.inventory.services;

import com.example.epl.common.v1.PickKeys;
import com.example.service.core.Errors;
import com.example.service.core.RequestContext;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.example.service.core.Protos.nullIfEmpty;

/**
 * Lots (inv.lot): the same picking keys of a product always give the same lot id, so stock,
 * movements and allocations compare lots by id.
 */
@Service
public class LotService {

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * Id of the lot with these keys, created when new. Runs inside the caller's transaction
     * (receipt, ChangePickKey, found stock in a stocktake …).
     *
     * @param lotNo      the key the tenant defines as lot number (md.picking_key_def), null if none
     * @param expiryDate the key with expiry control, parsed with its date format, null if none
     */
    @Transactional(propagation = Propagation.MANDATORY)
    public UUID findOrCreate(String productCode, PickKeys keys, String lotNo, LocalDate expiryDate) {
        var tenantId = RequestContext.current().requireTenant();
        Errors.required(productCode, "product_code");
        var values = List.of(keys.getKey1(), keys.getKey2(), keys.getKey3(), keys.getKey4(),
                keys.getKey5(), keys.getKey6(), keys.getKey7());
        // the INSERT is skipped when the lot exists; the SELECT then finds it
        // (a lot inserted concurrently after this statement started is found on the retry)
        for (int attempt = 0; attempt < 2; attempt++) {
            var query = entityManager.createNativeQuery("""
                    WITH inserted AS (
                        INSERT INTO lot (tenant_id, product_code, pick_key1, pick_key2, pick_key3, pick_key4,
                                         pick_key5, pick_key6, pick_key7, lot_no, expiry_date, created_by)
                        VALUES (?1, ?2, ?3, ?4, ?5, ?6, ?7, ?8, ?9, ?10, ?11, ?12)
                        ON CONFLICT DO NOTHING
                        RETURNING id)
                    SELECT id FROM inserted
                    UNION ALL
                    SELECT id FROM lot
                    WHERE tenant_id = ?1 AND product_code = ?2
                      AND pick_key1 IS NOT DISTINCT FROM ?3 AND pick_key2 IS NOT DISTINCT FROM ?4
                      AND pick_key3 IS NOT DISTINCT FROM ?5 AND pick_key4 IS NOT DISTINCT FROM ?6
                      AND pick_key5 IS NOT DISTINCT FROM ?7 AND pick_key6 IS NOT DISTINCT FROM ?8
                      AND pick_key7 IS NOT DISTINCT FROM ?9
                    LIMIT 1
                    """);
            query.setParameter(1, tenantId);
            query.setParameter(2, productCode);
            for (int i = 0; i < values.size(); i++) {
                query.setParameter(i + 3, nullIfEmpty(values.get(i)));
            }
            query.setParameter(10, nullIfEmpty(lotNo));
            query.setParameter(11, expiryDate);
            query.setParameter(12, Optional.ofNullable(RequestContext.current().user()).orElse("system"));
            var rows = query.getResultList();
            if (!rows.isEmpty()) {
                return (UUID) rows.get(0);
            }
        }
        throw new IllegalStateException("Lot of " + productCode + " neither inserted nor found");
    }
}

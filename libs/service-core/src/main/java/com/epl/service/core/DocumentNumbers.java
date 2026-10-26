package com.epl.service.core;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Next document number from the schema's number_sequence table (one row per tenant and code),
 * e.g. ARRIVAL -> "AR00000001". The row is created on first use; the UPDATE lock serialises callers.
 */
public class DocumentNumbers {

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional(propagation = Propagation.MANDATORY)
    public String next(UUID tenantId, String code, String prefix) {
        var row = (Object[]) entityManager.createNativeQuery("""
                        INSERT INTO number_sequence (tenant_id, code, prefix, start_value, end_value, current_value, pad_length)
                        VALUES (?1, ?2, ?3, 1, 99999999, 1, 8)
                        ON CONFLICT (tenant_id, code) DO UPDATE SET
                            current_value = CASE
                                WHEN number_sequence.current_value < number_sequence.end_value THEN number_sequence.current_value + 1
                                WHEN number_sequence.allow_wrap THEN number_sequence.start_value
                                ELSE number_sequence.current_value + 1      -- violates the CHECK: sequence exhausted
                            END,
                            updated_at = now()
                        RETURNING prefix, current_value, pad_length
                        """)
                .setParameter(1, tenantId)
                .setParameter(2, code)
                .setParameter(3, prefix)
                .getSingleResult();
        var number = String.valueOf(((Number) row[1]).longValue());
        var pad = ((Number) row[2]).intValue();
        return row[0] + "0".repeat(Math.max(0, pad - number.length())) + number;
    }
}

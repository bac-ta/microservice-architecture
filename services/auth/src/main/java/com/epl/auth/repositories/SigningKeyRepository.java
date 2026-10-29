package com.epl.auth.repositories;

import com.epl.auth.entities.SigningKeyEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface SigningKeyRepository extends JpaRepository<SigningKeyEntity, String> {

    Optional<SigningKeyEntity> findByStatus(String status);

    /** Keys a token may still be signed with: ACTIVE, NEXT, and RETIRED ones retired after {@code retiredAfter}. */
    @Query("""
            select k from SigningKeyEntity k
            where k.status in ('ACTIVE', 'NEXT') or (k.status = 'RETIRED' and k.retiredAt > :retiredAfter)
            order by k.createdAt desc
            """)
    List<SigningKeyEntity> findPublished(Instant retiredAfter);
}

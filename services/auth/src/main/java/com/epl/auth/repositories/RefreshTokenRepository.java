package com.epl.auth.repositories;

import com.epl.auth.entities.RefreshTokenEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepository extends JpaRepository<RefreshTokenEntity, UUID> {

    /** Locked: two concurrent refreshes with the same token cannot both rotate it. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<RefreshTokenEntity> findByTokenHash(byte[] tokenHash);

    @Modifying(flushAutomatically = true)
    @Query("update RefreshTokenEntity t set t.revokedAt = :now where t.familyId = :familyId and t.revokedAt is null")
    int revokeFamily(UUID familyId, Instant now);

    @Modifying(flushAutomatically = true)
    @Query("update RefreshTokenEntity t set t.revokedAt = :now where t.userId = :userId and t.revokedAt is null")
    int revokeAllOfUser(UUID userId, Instant now);
}

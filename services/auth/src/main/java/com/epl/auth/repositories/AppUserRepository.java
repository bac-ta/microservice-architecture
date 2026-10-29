package com.epl.auth.repositories;

import com.epl.auth.entities.AppUserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface AppUserRepository extends JpaRepository<AppUserEntity, UUID> {

    boolean existsByUsernameIgnoreCase(String username);

    /** pattern = '%text%' in lower case, '' = no filter; userType '' = all. */
    @Query("""
            select u from AppUserEntity u
            where (:pattern = '' or lower(u.username) like :pattern or lower(u.displayName) like :pattern)
              and (:userType = '' or u.userType = :userType)
            """)
    Page<AppUserEntity> search(String pattern, String userType, Pageable pageable);

    Optional<AppUserEntity> findByUsernameIgnoreCase(String username);

    // Login bookkeeping: bulk updates, so neither version nor updated_at / updated_by change
    // (an admin editing the user meanwhile does not get a conflict).

    @Modifying(flushAutomatically = true)
    @Query("update AppUserEntity u set u.lastLoginAt = :now, u.failedLoginCount = 0, u.lockedUntil = null where u.id = :id")
    void recordLogin(UUID id, Instant now);

    @Modifying(flushAutomatically = true)
    @Query("update AppUserEntity u set u.failedLoginCount = :failedLoginCount, u.lockedUntil = :lockedUntil where u.id = :id")
    void recordFailedLogin(UUID id, int failedLoginCount, Instant lockedUntil);

    /** Every JWT issued to the user so far becomes invalid ("tv" claim). */
    @Modifying(flushAutomatically = true)
    @Query("update AppUserEntity u set u.tokenVersion = u.tokenVersion + 1 where u.id = :id")
    void bumpTokenVersion(UUID id);
}

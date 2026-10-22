package com.example.epl.auth.repositories;

import com.example.epl.auth.entities.AppUserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

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
}

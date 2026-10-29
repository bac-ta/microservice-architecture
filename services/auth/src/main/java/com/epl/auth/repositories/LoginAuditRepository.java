package com.epl.auth.repositories;

import com.epl.auth.entities.LoginAuditEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LoginAuditRepository extends JpaRepository<LoginAuditEntity, Long> {
}

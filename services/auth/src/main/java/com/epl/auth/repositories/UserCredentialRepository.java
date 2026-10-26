package com.epl.auth.repositories;

import com.epl.auth.entities.UserCredentialEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UserCredentialRepository extends JpaRepository<UserCredentialEntity, UUID> {
}

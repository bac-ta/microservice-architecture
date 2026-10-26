package com.epl.auth.repositories;

import com.epl.auth.entities.RoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RoleRepository extends JpaRepository<RoleEntity, String> {

    List<RoleEntity> findAllByOrderByCode();
}

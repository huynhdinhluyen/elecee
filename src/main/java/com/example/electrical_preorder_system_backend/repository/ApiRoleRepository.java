package com.example.electrical_preorder_system_backend.repository;

import com.example.electrical_preorder_system_backend.entity.ApiRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ApiRoleRepository extends JpaRepository<ApiRole, UUID> {
    Optional<ApiRole> findByRoleName(String roleName);
}

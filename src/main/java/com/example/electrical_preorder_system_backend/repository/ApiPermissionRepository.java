package com.example.electrical_preorder_system_backend.repository;

import com.example.electrical_preorder_system_backend.entity.ApiPermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ApiPermissionRepository extends JpaRepository<ApiPermission, UUID> {
    List<ApiPermission> findAll();
    boolean existsByPathPattern(String pathPattern);
    @Query("SELECT p FROM ApiPermission p JOIN FETCH p.roles")
    List<ApiPermission> findAllWithRoles();
}

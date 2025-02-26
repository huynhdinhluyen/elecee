package com.example.electrical_preorder_system_backend.repository;

import com.example.electrical_preorder_system_backend.entity.Campaign;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.UUID;

public interface CampaignRepository extends JpaRepository<Campaign, UUID> {
    Page<Campaign> findByIsDeletedFalse(Pageable pageable);

    boolean existsByName(String name);

    @Query(value = "SELECT * FROM campaign WHERE is_deleted = false ORDER BY created_at DESC",
            countQuery = "SELECT count(*) FROM campaign WHERE is_deleted = false",
            nativeQuery = true)
    Page<Campaign> findActiveCampaigns(Pageable pageable);
}

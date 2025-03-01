package com.example.electrical_preorder_system_backend.repository;

import com.example.electrical_preorder_system_backend.entity.Campaign;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CampaignRepository extends JpaRepository<Campaign, UUID> {
    Page<Campaign> findByIsDeletedFalse(Pageable pageable);

    boolean existsByName(String name);

    @Query(value = "SELECT * " +
            "FROM campaign " +
            "WHERE is_deleted = false ",
            nativeQuery = true)
    List<Campaign> findActiveCampaigns();

    @Query(value = "SELECT * " +
            "FROM campaign " +
            "WHERE is_deleted = false AND id = ?1 ", nativeQuery = true)
    Campaign findActiveCampaignById(UUID id);

    Campaign findByName(String name);
}

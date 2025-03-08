package com.example.electrical_preorder_system_backend.repository;

import com.example.electrical_preorder_system_backend.entity.Campaign;
import com.example.electrical_preorder_system_backend.entity.CampaignStage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CampaignStageRepository extends JpaRepository<CampaignStage, UUID> {
    @Query("SELECT cs FROM CampaignStage cs WHERE cs.campaign = :campaign AND cs.isDeleted = false")
    List<CampaignStage> findCampaignStagesByCampaign(@Param("campaign") Campaign campaign);

    @Query(value = "SELECT * FROM campaign_stage " +
            "WHERE is_deleted = false ", nativeQuery = true)
    List<CampaignStage> findByIsDeletedFalse();

    CampaignStage findByName(String name);
}

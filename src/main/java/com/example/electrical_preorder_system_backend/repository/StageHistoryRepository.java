package com.example.electrical_preorder_system_backend.repository;

import com.example.electrical_preorder_system_backend.entity.StageHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface StageHistoryRepository extends JpaRepository<StageHistory, UUID> {
    @Query("SELECT sh FROM StageHistory sh WHERE sh.campaignStage.campaign.id = ?1 ORDER BY sh.transitionTime DESC")
    List<StageHistory> findHistoriesByCampaignId(UUID campaignId);
}

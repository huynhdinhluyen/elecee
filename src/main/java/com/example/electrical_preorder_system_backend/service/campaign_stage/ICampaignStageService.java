package com.example.electrical_preorder_system_backend.service.campaign_stage;

import com.example.electrical_preorder_system_backend.dto.request.campaign_stage.CreateCampaignStageRequest;
import com.example.electrical_preorder_system_backend.dto.request.campaign_stage.UpdateCampaignStageRequest;
import com.example.electrical_preorder_system_backend.dto.response.campaign_stage.CampaignStageDTO;
import com.example.electrical_preorder_system_backend.entity.CampaignStage;

import java.util.List;
import java.util.UUID;

public interface ICampaignStageService {
    List<CampaignStage> getCampaignStagesByCampaignId(UUID campaignId);

    CampaignStage getCampaignStageById(UUID id);

//    Map<String, Object> getCampaignPerformanceMetrics(UUID campaignId);

    CampaignStage createCampaignStage(CreateCampaignStageRequest request, UUID campaignId);

    CampaignStage updateCampaignStage(UUID campaignId, UUID stageId, UpdateCampaignStageRequest request);

    void deleteCampaignStage(UUID campaignId, UUID stageId);

    List<CampaignStageDTO> getConvertedCampaignStages(UUID id);

    CampaignStageDTO convertToDto(CampaignStage stage);
}

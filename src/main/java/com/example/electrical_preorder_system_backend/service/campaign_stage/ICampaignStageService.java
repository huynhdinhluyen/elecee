package com.example.electrical_preorder_system_backend.service.campaign_stage;

import com.example.electrical_preorder_system_backend.dto.request.CreateCampaignStageRequest;
import com.example.electrical_preorder_system_backend.dto.request.UpdateCampaignStageRequest;
import com.example.electrical_preorder_system_backend.dto.response.CampaignStageDTO;
import com.example.electrical_preorder_system_backend.entity.CampaignStage;

import java.util.List;
import java.util.UUID;

public interface ICampaignStageService {
    List<CampaignStage> getCampaignStagesByCampaignId(UUID campaignId);

    CampaignStage getCampaignStageById(UUID id);

    CampaignStage createCampaignStage(CreateCampaignStageRequest request, UUID campaignId);

    CampaignStage updateCampaignStage(UUID campaignId, UUID stageId, UpdateCampaignStageRequest request);

    void deleteCampaignStage(UUID campaignId, UUID stageId);

    List<CampaignStageDTO> getConvertedCampaignStages(UUID id);

    CampaignStageDTO convertToDto(CampaignStage stage);
}

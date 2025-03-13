package com.example.electrical_preorder_system_backend.service.campaign;

import com.example.electrical_preorder_system_backend.dto.filter.CampaignFilterCriteria;
import com.example.electrical_preorder_system_backend.dto.request.campaign.CreateCampaignRequest;
import com.example.electrical_preorder_system_backend.dto.request.campaign.UpdateCampaignRequest;
import com.example.electrical_preorder_system_backend.dto.response.campaign.CampaignDTO;
import com.example.electrical_preorder_system_backend.entity.Campaign;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface ICampaignService {
    Page<CampaignDTO> getFilteredCampaigns(CampaignFilterCriteria criteria, Pageable pageable);

    Campaign createCampaign(CreateCampaignRequest request);

    CampaignDTO getCampaignById(UUID id);

    Campaign updateCampaign(UUID id, UpdateCampaignRequest request);

    void deleteCampaign(UUID id);

    void updateCampaignStatuses();

    CampaignDTO convertToDto(Campaign campaign);

    void clearCampaignCache();
}
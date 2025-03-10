package com.example.electrical_preorder_system_backend.service.campaign;

import com.example.electrical_preorder_system_backend.dto.request.CreateCampaignRequest;
import com.example.electrical_preorder_system_backend.dto.request.UpdateCampaignRequest;
import com.example.electrical_preorder_system_backend.dto.response.CampaignDTO;
import com.example.electrical_preorder_system_backend.entity.Campaign;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface ICampaignService {
//    Page<Campaign> getFilteredCampaigns(CampaignFilterCriteria criteria, Pageable pageable);

    Campaign createCampaign(CreateCampaignRequest request);

    Page<Campaign> getCampaigns(Pageable pageable);

    Campaign getCampaignById(UUID id);

    Campaign updateCampaign(UUID id, UpdateCampaignRequest request);

    void deleteCampaign(UUID id);

    void updateCampaignStatuses();

    Page<CampaignDTO> getConvertedCampaigns(Pageable pageable);

    CampaignDTO convertToDto(Campaign campaign);
}

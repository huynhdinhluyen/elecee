package com.example.electrical_preorder_system_backend.service.campaign;

import com.example.electrical_preorder_system_backend.dto.request.CreateCampaignRequest;
import com.example.electrical_preorder_system_backend.dto.request.UpdateCampaignRequest;
import com.example.electrical_preorder_system_backend.dto.response.CampaignDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface ICampaignService {
    CampaignDTO createCampaign(CreateCampaignRequest request);

    Page<CampaignDTO> getCampaigns(Pageable pageable);

    CampaignDTO getCampaignById(UUID id);

    CampaignDTO updateCampaign(UUID id, UpdateCampaignRequest request);

    void deleteCampaign(UUID id);
}

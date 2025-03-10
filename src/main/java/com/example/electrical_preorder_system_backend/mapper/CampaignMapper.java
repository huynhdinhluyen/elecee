package com.example.electrical_preorder_system_backend.mapper;

import com.example.electrical_preorder_system_backend.dto.response.CampaignDTO;
import com.example.electrical_preorder_system_backend.entity.Campaign;

public class CampaignMapper {

    public static CampaignDTO toCampaignDTO(Campaign campaign) {
        CampaignDTO DTO = new CampaignDTO();
        DTO.setId(campaign.getId());
        DTO.setName(campaign.getName());
        DTO.setStartDate(campaign.getStartDate());
        DTO.setEndDate(campaign.getEndDate());
        DTO.setMinQuantity(campaign.getMinQuantity());
        DTO.setMaxQuantity(campaign.getMaxQuantity());
        DTO.setTotalAmount(campaign.getTotalAmount());
        DTO.setStatus(campaign.getStatus());
        DTO.setProduct(ProductMapper.toProductDTO(campaign.getProduct()));
        DTO.setCreatedAt(campaign.getCreatedAt());
        DTO.setUpdatedAt(campaign.getUpdatedAt());
        return DTO;

    }
}

package com.example.electrical_preorder_system_backend.dto.response.product;

import com.example.electrical_preorder_system_backend.dto.response.campaign.SimplifiedCampaignDTO;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class ProductDetailDTO {
    private ProductDTO product;
    private List<SimplifiedCampaignDTO> campaigns;
}
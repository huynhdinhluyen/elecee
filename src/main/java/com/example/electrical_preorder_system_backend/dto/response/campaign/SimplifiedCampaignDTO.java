package com.example.electrical_preorder_system_backend.dto.response.campaign;

import com.example.electrical_preorder_system_backend.dto.response.campaign_stage.CampaignStageDTO;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
public class SimplifiedCampaignDTO {
    private UUID id;
    private String name;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Integer minQuantity;
    private Integer maxQuantity;
    private BigDecimal totalAmount;
    private String status;
    private List<CampaignStageDTO> stages;
}

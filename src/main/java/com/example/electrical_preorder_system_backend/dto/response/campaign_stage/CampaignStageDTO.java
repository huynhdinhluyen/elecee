package com.example.electrical_preorder_system_backend.dto.response.campaign_stage;

import com.example.electrical_preorder_system_backend.enums.CampaignStageStatus;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class CampaignStageDTO {
    private UUID id;
    private String name;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Integer quantitySold;
    private Integer targetQuantity;
    private CampaignStageStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

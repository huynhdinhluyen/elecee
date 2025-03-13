package com.example.electrical_preorder_system_backend.dto.response.stage_history;

import com.example.electrical_preorder_system_backend.enums.CampaignStageStatus;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class StageHistoryDTO {
    private UUID id;
    private CampaignStageStatus preStatus;
    private CampaignStageStatus curStatus;
    private LocalDateTime transitionTime;
    private UUID campaignStageId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

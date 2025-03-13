package com.example.electrical_preorder_system_backend.dto.request.campaign_stage;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UpdateCampaignStageRequest {
    @NotBlank
    private String name;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    @Min(0)
    private Integer targetQuantity;
    @NotBlank
    private String status;
}

package com.example.electrical_preorder_system_backend.dto.filter;

import com.example.electrical_preorder_system_backend.enums.CampaignStatus;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
public class CampaignFilterCriteria {
    private String name;
    private CampaignStatus status;
    private UUID productId;
    private LocalDateTime startDateFrom;
    private LocalDateTime startDateTo;
    private LocalDateTime endDateFrom;
    private LocalDateTime endDateTo;
}
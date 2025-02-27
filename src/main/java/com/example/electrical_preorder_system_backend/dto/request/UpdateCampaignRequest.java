package com.example.electrical_preorder_system_backend.dto.request;

import jakarta.validation.constraints.Min;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class UpdateCampaignRequest {
    private String name;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    @Min(0)
    private Integer minQuantity;
    @Min(0)
    private Integer maxQuantity;
    @Min(0)
    private BigDecimal totalAmount;
    private String status;
    private String productId;
}

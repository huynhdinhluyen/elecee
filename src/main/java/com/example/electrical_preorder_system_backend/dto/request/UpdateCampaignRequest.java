package com.example.electrical_preorder_system_backend.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class UpdateCampaignRequest {
    @NotBlank
    private String name;

    @NotNull
    private LocalDateTime startDate;

    @NotNull
    private LocalDateTime endDate;

    @Min(0)
    private Integer minQuantity;

    @Min(0)
    private Integer maxQuantity;

    @Min(0)
    private BigDecimal totalAmount;

    @NotBlank
    private String status;

    private String productId;
}

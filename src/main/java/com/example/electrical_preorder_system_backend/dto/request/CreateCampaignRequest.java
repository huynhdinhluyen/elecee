package com.example.electrical_preorder_system_backend.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CreateCampaignRequest {
    @NotBlank
    private String name;

    @NotNull
    private LocalDateTime startDate;

    @NotNull
    private LocalDateTime endDate;

    @NotNull
    @Min(0)
    private Integer minQuantity;

    @NotNull
    @Min(0)
    private Integer maxQuantity;

    @NotNull
    @Min(0)
    private BigDecimal totalAmount;

    @NotBlank
    private String status;

    @NotBlank
    private String productId;
}

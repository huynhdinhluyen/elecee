package com.example.electrical_preorder_system_backend.dto.response.campaign;

import com.example.electrical_preorder_system_backend.dto.response.product.ProductDTO;
import com.example.electrical_preorder_system_backend.enums.CampaignStatus;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
public class CampaignDTO {
    private UUID id;
    private String name;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Integer minQuantity;
    private Integer maxQuantity;
    private BigDecimal totalAmount;
    private CampaignStatus status;
    private ProductDTO product;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
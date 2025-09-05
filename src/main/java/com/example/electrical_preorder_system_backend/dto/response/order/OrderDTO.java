package com.example.electrical_preorder_system_backend.dto.response.order;

import com.example.electrical_preorder_system_backend.dto.response.campaign.CampaignDTO;
import com.example.electrical_preorder_system_backend.dto.response.user.UserDTO;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class OrderDTO {
    private UUID id;
    private Integer quantity;
    private String status;
    private BigDecimal totalAmount;
    private UserDTO user;
    private CampaignDTO campaign;
    private boolean isDeleted;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

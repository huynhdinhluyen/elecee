package com.example.electrical_preorder_system_backend.dto.response;

import com.example.electrical_preorder_system_backend.enums.PaymentMethod;
import com.example.electrical_preorder_system_backend.enums.PaymentStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class PaymentDTO {
    private Long id;
    private LocalDateTime date;
    private BigDecimal amount;
    private PaymentMethod method;
    private PaymentStatus status;
    private List<UUID> orderIds;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
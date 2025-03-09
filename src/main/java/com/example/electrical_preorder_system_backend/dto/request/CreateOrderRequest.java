package com.example.electrical_preorder_system_backend.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@RequiredArgsConstructor
public class CreateOrderRequest {
    @NotNull
    private Integer quantity;
    @NotNull
    private UUID campaignId;
}

package com.example.electrical_preorder_system_backend.dto.request;

import lombok.Data;

import java.util.UUID;

@Data
public class UpdateOrderRequest {
    private Integer quantity;
}

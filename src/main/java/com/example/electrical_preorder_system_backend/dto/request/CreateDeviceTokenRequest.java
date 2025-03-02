package com.example.electrical_preorder_system_backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateDeviceTokenRequest {
    @NotBlank
    String token;
}

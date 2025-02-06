package com.example.electrical_preorder_system_backend.dto.response;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.UUID;

@Data
public class CategoryDTO {
    private UUID id;
    @NotBlank
    private String name;
}

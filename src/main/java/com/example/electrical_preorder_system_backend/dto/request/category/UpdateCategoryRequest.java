package com.example.electrical_preorder_system_backend.dto.request.category;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateCategoryRequest {
    @NotBlank(message = "Category name is required")
    private String name;
}

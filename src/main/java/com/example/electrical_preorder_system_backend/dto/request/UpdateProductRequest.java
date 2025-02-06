package com.example.electrical_preorder_system_backend.dto.request;

import com.example.electrical_preorder_system_backend.dto.response.CategoryDTO;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class UpdateProductRequest {
    private String productCode;
    private String name;
    @Min(0)
    private Integer quantity;
    private String description;
    @Positive
    private BigDecimal price;
    @PositiveOrZero
    private Integer position;
    private CategoryDTO category;
}

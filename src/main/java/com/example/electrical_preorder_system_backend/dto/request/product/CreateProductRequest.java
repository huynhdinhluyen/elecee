package com.example.electrical_preorder_system_backend.dto.request.product;

import com.example.electrical_preorder_system_backend.dto.response.category.CategoryDTO;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateProductRequest {
    @NotBlank
    private String productCode;
    @NotBlank
    private String name;
    @Min(0)
    private Integer quantity;
    @NotBlank
    private String description;
    @Positive
    private BigDecimal price;
    @PositiveOrZero
    private Integer position;
    @NotNull
    private CategoryDTO category;
}

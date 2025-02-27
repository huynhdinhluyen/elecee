package com.example.electrical_preorder_system_backend.dto.request;

import com.example.electrical_preorder_system_backend.dto.response.CategoryDTO;
import com.example.electrical_preorder_system_backend.dto.response.ImageProductDTO;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

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
    private List<ImageProductDTO> oldImageProducts = new ArrayList<>();
}

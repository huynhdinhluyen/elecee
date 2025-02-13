package com.example.electrical_preorder_system_backend.dto.response;

import com.example.electrical_preorder_system_backend.enums.ProductStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
public class ProductDTO {
    private UUID id;
    private String productCode;
    private String name;
    private String slug;
    private Integer quantity;
    private String description;
    private BigDecimal price;
    private Integer position;
    private ProductStatus status;
    private boolean isDeleted;
    private CategoryDTO category;
    private List<ImageProductDTO> imageProducts = new ArrayList<>();
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

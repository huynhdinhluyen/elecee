package com.example.electrical_preorder_system_backend.dto.response.product;

import com.example.electrical_preorder_system_backend.dto.response.category.CategoryDTO;
import com.example.electrical_preorder_system_backend.dto.response.product_images.ImageProductDTO;
import com.example.electrical_preorder_system_backend.enums.ProductStatus;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
public class ProductDTO {
    UUID id;
    String productCode;
    String name;
    String slug;
    Integer quantity;
    String description;
    BigDecimal price;
    Integer position;
    ProductStatus status;
    boolean isDeleted;
    CategoryDTO category;
    List<ImageProductDTO> imageProducts = new ArrayList<>();
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}

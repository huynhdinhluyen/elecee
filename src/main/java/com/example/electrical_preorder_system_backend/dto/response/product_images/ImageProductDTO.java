package com.example.electrical_preorder_system_backend.dto.response.product_images;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ImageProductDTO {
    @NotBlank
    private String altText;
    @NotBlank
    private String imageUrl;
}

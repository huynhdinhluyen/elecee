package com.example.electrical_preorder_system_backend.mapper;

import com.example.electrical_preorder_system_backend.dto.response.ProductDTO;
import com.example.electrical_preorder_system_backend.entity.Product;


public class ProductMapper {

    public static ProductDTO toProductDTO(Product product) {
        ProductDTO productDTO = new ProductDTO();
        productDTO.setId(product.getId());
        productDTO.setProductCode(product.getProductCode());
        productDTO.setName(product.getName());
        productDTO.setSlug(product.getSlug());
        productDTO.setQuantity(product.getQuantity());
        productDTO.setDescription(product.getDescription());
        productDTO.setPrice(product.getPrice());
        productDTO.setPosition(product.getPosition());
        productDTO.setStatus(product.getStatus());
        productDTO.setDeleted(product.isDeleted());
        productDTO.setCategory(CategoryMapper.toCategoryDTO(product.getCategory()));
        productDTO.setCreatedAt(product.getCreatedAt());
        productDTO.setUpdatedAt(product.getUpdatedAt());
        return productDTO;
    }
}

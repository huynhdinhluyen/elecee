package com.example.electrical_preorder_system_backend.service.product;

import com.example.electrical_preorder_system_backend.dto.request.CreateProductRequest;
import com.example.electrical_preorder_system_backend.dto.request.UpdateProductRequest;
import com.example.electrical_preorder_system_backend.dto.response.ProductDTO;
import com.example.electrical_preorder_system_backend.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface IProductService {
    Product addProduct(CreateProductRequest request);

    Page<Product> getProducts(Pageable pageable);

    Product getProductByProductCode(String productCode);

    Page<Product> getProductsByCategory(String category, Pageable pageable);

    Page<Product> getProductsByName(String name, Pageable pageable);

    Product getProductById(UUID id);

    Product updateProduct(UpdateProductRequest request, UUID id);

    void deleteProductById(UUID id);

    Long countProducts();

    Page<ProductDTO> getConvertedProducts(Pageable pageable);

    ProductDTO convertToDto(Product product);
}

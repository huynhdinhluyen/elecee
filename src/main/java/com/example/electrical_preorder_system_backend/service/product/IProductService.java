package com.example.electrical_preorder_system_backend.service.product;

import com.example.electrical_preorder_system_backend.dto.filter.ProductFilterCriteria;
import com.example.electrical_preorder_system_backend.dto.request.CreateProductRequest;
import com.example.electrical_preorder_system_backend.dto.request.UpdateProductRequest;
import com.example.electrical_preorder_system_backend.dto.response.ProductDTO;
import com.example.electrical_preorder_system_backend.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

public interface IProductService {
    Page<ProductDTO> getFilteredProducts(ProductFilterCriteria criteria, Pageable pageable);

    Product addProduct(CreateProductRequest request, List<MultipartFile> files);

    Page<Product> getProducts(Pageable pageable);

    Page<Product> getProductsByCategory(String categoryName, Pageable pageable);

    Page<Product> getProductsByName(String name, Pageable pageable);

    Product getProductById(UUID id);

    Product updateProduct(UpdateProductRequest request, UUID id, List<MultipartFile> files);

    void deleteProductById(UUID id);

    Long countProducts();

    Page<ProductDTO> getConvertedProducts(Pageable pageable);

    ProductDTO convertToDto(Product product);

    Page<Product> searchProducts(String query, Pageable pageable);

    Page<Product> searchProducts(String query, String category, Pageable pageable);

    Product getProductBySlug(String slug);

    Product getProductByProductCode(String productCode);

    void deleteProducts(List<UUID> ids);
}

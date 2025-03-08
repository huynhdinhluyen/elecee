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
    Page<ProductDTO> getProducts(ProductFilterCriteria criteria, Pageable pageable);

    Product addProduct(CreateProductRequest request, List<MultipartFile> files);

    Product updateProduct(UpdateProductRequest request, UUID id, List<MultipartFile> files);

    void deleteProductById(UUID id);

    Long countProducts();

    ProductDTO convertToDto(Product product);

    void clearProductCache();

    Product getProductBySlug(String slug);

    void deleteProducts(List<UUID> ids);
}

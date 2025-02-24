package com.example.electrical_preorder_system_backend.controller;

import com.example.electrical_preorder_system_backend.dto.request.CreateProductRequest;
import com.example.electrical_preorder_system_backend.dto.request.UpdateProductRequest;
import com.example.electrical_preorder_system_backend.dto.response.ApiResponse;
import com.example.electrical_preorder_system_backend.dto.response.ProductDTO;
import com.example.electrical_preorder_system_backend.entity.Product;
import com.example.electrical_preorder_system_backend.service.product.IProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("${api.prefix}/products")
public class ProductController {

    private final IProductService productService;

    @GetMapping
    public ResponseEntity<ApiResponse> getProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String category) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ProductDTO> productPage;

        if (category != null && !category.trim().isEmpty()) {
            productPage = productService.getProductsByCategory(category.trim(), pageable)
                    .map(productService::convertToDto);
        } else {
            productPage = productService.getConvertedProducts(pageable);
        }

        return ResponseEntity.ok(new ApiResponse("Products retrieved successfully", productPage));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> getProductById(@PathVariable UUID id) {
        Product product = productService.getProductById(id);
        return ResponseEntity.ok(new ApiResponse("Product retrieved successfully", productService.convertToDto(product)));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse> searchProducts(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ProductDTO> productPage = productService.searchProducts(query, pageable)
                .map(productService::convertToDto);
        return ResponseEntity.ok(new ApiResponse("Product search successful", productPage));
    }

    @GetMapping("/count")
    public ResponseEntity<ApiResponse> countProducts() {
        Long count = productService.countProducts();
        return ResponseEntity.ok(new ApiResponse("Product count retrieved successfully", count));
    }

    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<ApiResponse> createProduct(
            @RequestPart("product") @Valid CreateProductRequest productRequest,
            @RequestPart(value = "imageFiles", required = false) List<MultipartFile> imageFiles) {
        Product product = productService.addProduct(productRequest, imageFiles);
        return ResponseEntity.ok(new ApiResponse("Product created successfully", productService.convertToDto(product)));
    }

    @PatchMapping(value = "/{id}", consumes = "multipart/form-data")
    public ResponseEntity<ApiResponse> updateProduct(
            @PathVariable UUID id,
            @RequestPart("product") UpdateProductRequest request,
            @RequestPart(value = "files", required = false) List<MultipartFile> imageFiles) {
        Product updatedProduct = productService.updateProduct(request, id, imageFiles);
        return ResponseEntity.ok(new ApiResponse("Product updated successfully", productService.convertToDto(updatedProduct)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> deleteProduct(@PathVariable UUID id) {
        productService.deleteProductById(id);
        return ResponseEntity.ok(new ApiResponse("Product deleted successfully", null));
    }
}

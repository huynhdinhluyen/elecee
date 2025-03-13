package com.example.electrical_preorder_system_backend.controller;

import com.example.electrical_preorder_system_backend.dto.filter.ProductFilterCriteria;
import com.example.electrical_preorder_system_backend.dto.request.product.CreateProductRequest;
import com.example.electrical_preorder_system_backend.dto.request.product.UpdateProductRequest;
import com.example.electrical_preorder_system_backend.dto.response.ApiResponse;
import com.example.electrical_preorder_system_backend.dto.response.product.ProductDTO;
import com.example.electrical_preorder_system_backend.dto.response.product.ProductDetailDTO;
import com.example.electrical_preorder_system_backend.entity.Product;
import com.example.electrical_preorder_system_backend.service.product.IProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("${api.prefix}/products")
@Tag(name = "Product API", description = "APIs for Product management")
public class ProductController {

    private final IProductService productService;

    @Operation(
            summary = "Get products with searching, filtering, sorting and pagination",
            description = "Returns a paginated list of products that can be filtered by category, search query, " +
                    "and price range. Results can be sorted by any product field."
    )
    @GetMapping
    public ResponseEntity<ApiResponse> getProducts(
            @Parameter(description = "Page number (zero-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Number of items per page") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Filter by category name") @RequestParam(required = false) String category,
            @Parameter(description = "Search query for name or product code") @RequestParam(required = false) String query,
            @Parameter(description = "Minimum price filter") @RequestParam(required = false) BigDecimal minPrice,
            @Parameter(description = "Maximum price filter") @RequestParam(required = false) BigDecimal maxPrice,
            @Parameter(description = "Field to sort by") @RequestParam(defaultValue = "position") String sortBy,
            @Parameter(description = "Sort direction (asc or desc)") @RequestParam(defaultValue = "asc") String sortDirection) {

        Sort sort = sortDirection.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);

        ProductFilterCriteria criteria = new ProductFilterCriteria();
        criteria.setCategory(category);
        criteria.setQuery(query);
        criteria.setMinPrice(minPrice);
        criteria.setMaxPrice(maxPrice);

        Page<ProductDTO> productPage = productService.getProducts(criteria, pageable);

        return ResponseEntity.ok(new ApiResponse("Products retrieved successfully", productPage));
    }

    @Operation(
            summary = "Get product by slug",
            description = "Returns a single product by its slug identifier, including all associated campaigns and stages"
    )
    @GetMapping("/{slug}")
    public ResponseEntity<ApiResponse> getProductBySlug(
            @Parameter(description = "Product slug", required = true) @PathVariable String slug
    ) {
        ProductDetailDTO productDetail = productService.getProductDetailWithCampaigns(slug);
        return ResponseEntity.ok(new ApiResponse("Product retrieved successfully", productDetail));
    }

    @Operation(
            summary = "Get total product count",
            description = "Returns the total count of active products"
    )
    @GetMapping("/count")
    public ResponseEntity<ApiResponse> countProducts() {
        Long count = productService.countProducts();
        return ResponseEntity.ok(new ApiResponse("Product count retrieved successfully", count));
    }

    @Operation(
            summary = "Create new product",
            description = "Create a new product with optional image uploads. Requires admin role."
    )
    @SecurityRequirement(name = "Bearer Authentication")
    @PostMapping(consumes = "multipart/form-data")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse> createProduct(
            @Parameter(description = "Product data", required = true)
            @RequestPart("product") @Valid CreateProductRequest productRequest,
            @Parameter(description = "Product images (optional)")
            @RequestPart(value = "imageFiles", required = false) List<MultipartFile> imageFiles) {
        Product product = productService.addProduct(productRequest, imageFiles);
        return ResponseEntity.ok(new ApiResponse("Product created successfully", productService.convertToDto(product)));
    }

    @Operation(
            summary = "Update an existing product",
            description = "Update a product by ID with option to upload new images. Requires admin role."
    )
    @SecurityRequirement(name = "Bearer Authentication")
    @PatchMapping(value = "/{id}", consumes = "multipart/form-data")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse> updateProduct(
            @Parameter(description = "Product ID", required = true) @PathVariable UUID id,
            @Parameter(description = "Updated product data", required = true)
            @RequestPart("product") UpdateProductRequest request,
            @Parameter(description = "New product images (optional)")
            @RequestPart(value = "imageFiles", required = false) List<MultipartFile> imageFiles) {
        Product updatedProduct = productService.updateProduct(request, id, imageFiles);
        return ResponseEntity.ok(new ApiResponse("Product updated successfully", productService.convertToDto(updatedProduct)));
    }

    @Operation(
            summary = "Delete a product",
            description = "Soft delete a product by ID (marks as deleted). Requires admin role."
    )
    @SecurityRequirement(name = "Bearer Authentication")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse> deleteProduct(
            @Parameter(description = "Product ID", required = true) @PathVariable UUID id
    ) {
        productService.deleteProductById(id);
        return ResponseEntity.ok(new ApiResponse("Product deleted successfully", id));
    }

    @Operation(
            summary = "Delete multiple products",
            description = "Soft delete multiple products by their IDs. Requires admin role."
    )
    @SecurityRequirement(name = "Bearer Authentication")
    @DeleteMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse> deleteProducts(
            @Parameter(description = "List of product IDs", required = true) @RequestParam List<UUID> ids
    ) {
        productService.deleteProducts(ids);
        return ResponseEntity.ok(new ApiResponse("Products deleted successfully", ids));
    }
}
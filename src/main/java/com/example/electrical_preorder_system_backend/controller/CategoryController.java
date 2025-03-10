package com.example.electrical_preorder_system_backend.controller;

import com.example.electrical_preorder_system_backend.dto.request.CreateCategoryRequest;
import com.example.electrical_preorder_system_backend.dto.request.UpdateCategoryRequest;
import com.example.electrical_preorder_system_backend.dto.response.ApiResponse;
import com.example.electrical_preorder_system_backend.dto.response.CategoryDTO;
import com.example.electrical_preorder_system_backend.exception.AlreadyExistsException;
import com.example.electrical_preorder_system_backend.service.category.ICategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

import static org.springframework.http.HttpStatus.CONFLICT;

@RestController
@RequiredArgsConstructor
@RequestMapping("${api.prefix}/categories")
@Tag(name = "Category API", description = "APIs for managing product categories")
public class CategoryController {

    private final ICategoryService categoryService;

    @Operation(
            summary = "Get all categories",
            description = "Returns a list of all active (non-deleted) product categories"
    )
    @GetMapping
    public ResponseEntity<ApiResponse> getAllCategories() {
        List<CategoryDTO> categories = categoryService.getAllCategories();
        return ResponseEntity.ok(new ApiResponse("Categories retrieved successfully", categories));
    }

    @Operation(
            summary = "Get category by ID",
            description = "Returns a single category by its UUID identifier"
    )
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> getCategoryById(
            @Parameter(description = "Category UUID", required = true) @PathVariable UUID id
    ) {
        CategoryDTO category = categoryService.getCategoryById(id);
        return ResponseEntity.ok(new ApiResponse("Category retrieved successfully", category));
    }

    @Operation(
            summary = "Create new category",
            description = "Create a new product category. Requires admin role."
    )
    @SecurityRequirement(name = "Bearer Authentication")
    @PostMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse> addCategory(
            @Parameter(description = "New category data", required = true)
            @RequestBody @Valid CreateCategoryRequest categoryRequest
    ) {
        try {
            CategoryDTO newCategory = categoryService.createCategory(categoryRequest);
            return ResponseEntity.ok(new ApiResponse("success", newCategory));
        } catch (AlreadyExistsException e) {
            return ResponseEntity.status(CONFLICT).body(new ApiResponse(e.getMessage(), null));
        }
    }

    @Operation(
            summary = "Update a category",
            description = "Update an existing category by ID. Requires admin role."
    )
    @SecurityRequirement(name = "Bearer Authentication")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse> updateCategory(
            @Parameter(description = "Category UUID", required = true) @PathVariable UUID id,
            @Parameter(description = "Updated category data", required = true)
            @RequestBody @Valid UpdateCategoryRequest request
    ) {
        CategoryDTO updatedCategory = categoryService.updateCategory(id, request);
        return ResponseEntity.ok(new ApiResponse("Category updated successfully", updatedCategory));
    }

    @Operation(
            summary = "Delete a category",
            description = "Soft delete a category by ID (marks as deleted). Requires admin role."
    )
    @SecurityRequirement(name = "Bearer Authentication")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse> deleteCategoryById(
            @Parameter(description = "Category UUID", required = true) @PathVariable UUID id
    ) {
        categoryService.deleteCategoryById(id);
        return ResponseEntity.ok(new ApiResponse("success", null));
    }
}

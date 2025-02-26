package com.example.electrical_preorder_system_backend.controller;

import com.example.electrical_preorder_system_backend.dto.request.CreateCategoryRequest;
import com.example.electrical_preorder_system_backend.dto.request.UpdateCategoryRequest;
import com.example.electrical_preorder_system_backend.dto.response.ApiResponse;
import com.example.electrical_preorder_system_backend.dto.response.CategoryDTO;
import com.example.electrical_preorder_system_backend.exception.AlreadyExistsException;
import com.example.electrical_preorder_system_backend.service.category.ICategoryService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
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
public class CategoryController {

    private final ICategoryService categoryService;

    @GetMapping
    public ResponseEntity<ApiResponse> getAllCategories() {
        List<CategoryDTO> categories = categoryService.getAllCategories();
        return ResponseEntity.ok(new ApiResponse("Categories retrieved successfully", categories));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> getCategoryById(@PathVariable UUID id) {
        CategoryDTO category = categoryService.getCategoryById(id);
        return ResponseEntity.ok(new ApiResponse("Category retrieved successfully", category));
    }

    @PostMapping
    @SecurityRequirement(name = "Bearer Authentication")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse> addCategory(@RequestBody CreateCategoryRequest categoryRequest) {
        try {
            CategoryDTO newCategory = categoryService.createCategory(categoryRequest);
            return ResponseEntity.ok(new ApiResponse("success", newCategory));
        } catch (AlreadyExistsException e) {
            return ResponseEntity.status(CONFLICT).body(new ApiResponse(e.getMessage(), null));
        }
    }

    @PutMapping("/{id}")
    @SecurityRequirement(name = "Bearer Authentication")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse> updateCategory(@PathVariable UUID id,
                                                      @RequestBody @Valid UpdateCategoryRequest request) {
        CategoryDTO updatedCategory = categoryService.updateCategory(id, request);
        return ResponseEntity.ok(new ApiResponse("Category updated successfully", updatedCategory));
    }

    @DeleteMapping("/{id}")
    @SecurityRequirement(name = "Bearer Authentication")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse> deleteCategoryById(@PathVariable UUID id) {
        categoryService.deleteCategoryById(id);
        return ResponseEntity.ok(new ApiResponse("success", null));
    }
}
package com.example.electrical_preorder_system_backend.controller;

import com.example.electrical_preorder_system_backend.dto.request.CreateCategoryRequest;
import com.example.electrical_preorder_system_backend.dto.request.UpdateCategoryRequest;
import com.example.electrical_preorder_system_backend.dto.response.ApiResponse;
import com.example.electrical_preorder_system_backend.dto.response.CategoryDTO;
import com.example.electrical_preorder_system_backend.service.category.ICategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

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
    public ResponseEntity<ApiResponse> createCategory(@RequestBody @Valid CreateCategoryRequest request) {
        CategoryDTO category = categoryService.createCategory(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse("Category created successfully", category));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse> updateCategory(@PathVariable UUID id,
                                                      @RequestBody @Valid UpdateCategoryRequest request) {
        CategoryDTO updatedCategory = categoryService.updateCategory(id, request);
        return ResponseEntity.ok(new ApiResponse("Category updated successfully", updatedCategory));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> deleteCategory(@PathVariable UUID id) {
        categoryService.deleteCategoryById(id);
        return ResponseEntity.ok(new ApiResponse("Category deleted successfully", id));
    }
}
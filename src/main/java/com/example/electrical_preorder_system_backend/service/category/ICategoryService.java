package com.example.electrical_preorder_system_backend.service.category;

import com.example.electrical_preorder_system_backend.dto.request.CreateCategoryRequest;
import com.example.electrical_preorder_system_backend.dto.request.UpdateCategoryRequest;
import com.example.electrical_preorder_system_backend.dto.response.CategoryDTO;

import java.util.List;
import java.util.UUID;

public interface ICategoryService {
    List<CategoryDTO> getAllCategories();

    CategoryDTO getCategoryById(UUID id);

    CategoryDTO createCategory(CreateCategoryRequest request);

    CategoryDTO updateCategory(UUID id, UpdateCategoryRequest request);

    void deleteCategoryById(UUID id);
}

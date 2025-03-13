package com.example.electrical_preorder_system_backend.service.category;

import com.example.electrical_preorder_system_backend.dto.request.category.CreateCategoryRequest;
import com.example.electrical_preorder_system_backend.dto.request.category.UpdateCategoryRequest;
import com.example.electrical_preorder_system_backend.dto.response.category.CategoryDTO;

import java.util.List;
import java.util.UUID;

public interface ICategoryService {
    List<CategoryDTO> getAllCategories();

    List<CategoryDTO> searchCategories(String searchTerm);

    CategoryDTO getCategoryById(UUID id);

    CategoryDTO createCategory(CreateCategoryRequest request);

    CategoryDTO updateCategory(UUID id, UpdateCategoryRequest request);

    void deleteCategoryById(UUID id);
}

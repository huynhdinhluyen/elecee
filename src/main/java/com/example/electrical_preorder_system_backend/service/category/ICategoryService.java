package com.example.electrical_preorder_system_backend.service.category;

import com.example.electrical_preorder_system_backend.entity.Category;

import java.util.List;
import java.util.UUID;

public interface ICategoryService {
    Category getCategoryById(UUID id);

    Category getCategoryByName(String name);

    List<Category> getAllCategories();

    Category addCategory(Category category);

    Category updateCategory(Category category, UUID id);

    void deleteCategoryById(UUID id);
}

package com.example.electrical_preorder_system_backend.service.category;

import com.example.electrical_preorder_system_backend.dto.request.CreateCategoryRequest;
import com.example.electrical_preorder_system_backend.dto.request.UpdateCategoryRequest;
import com.example.electrical_preorder_system_backend.dto.response.CategoryDTO;
import com.example.electrical_preorder_system_backend.entity.Category;
import com.example.electrical_preorder_system_backend.exception.AlreadyExistsException;
import com.example.electrical_preorder_system_backend.exception.ResourceNotFoundException;
import com.example.electrical_preorder_system_backend.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryService implements ICategoryService {

    private final CategoryRepository categoryRepository;

    @Override
    public List<CategoryDTO> getAllCategories() {
        log.info("Fetching active categories from the database.");
        List<Category> categories = categoryRepository.findByIsDeletedFalseOrderByNameAsc();
        return categories.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public CategoryDTO getCategoryById(UUID id) {
        Category category = categoryRepository.findById(id)
                .filter(cat -> !cat.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found!"));
        return convertToDto(category);
    }

    @Override
    public CategoryDTO createCategory(CreateCategoryRequest request) {
        String trimmedName = request.getName().trim();
        Category category = categoryRepository.findByName(trimmedName);
        if (category != null) {
            if (category.isDeleted()) {
                category.setDeleted(false);
                category = categoryRepository.save(category);
                return convertToDto(category);
            } else {
                throw new AlreadyExistsException("Category '" + trimmedName + "' already exists.");
            }
        }
        Category newCategory = new Category(trimmedName);
        newCategory = categoryRepository.save(newCategory);
        return convertToDto(newCategory);
    }

    @Override
    @Transactional
    public CategoryDTO updateCategory(UUID id, UpdateCategoryRequest request) {
        Category existingCategory = categoryRepository.findById(id)
                .filter(cat -> !cat.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found!"));

        String newName = request.getName().trim();
        Category duplicate = categoryRepository.findByName(newName);
        if (duplicate != null && !duplicate.getId().equals(existingCategory.getId())) {
            if (duplicate.isDeleted()) {
                duplicate.setDeleted(false);
                existingCategory.setDeleted(true);
                categoryRepository.save(existingCategory);
                return convertToDto(categoryRepository.save(duplicate));
            } else {
                throw new AlreadyExistsException("Category '" + newName + "' already exists.");
            }
        }
        existingCategory.setName(newName);
        log.info("Updated category with ID {}: new name {}", id, newName);
        existingCategory = categoryRepository.save(existingCategory);
        return convertToDto(existingCategory);
    }

    @Override
    public void deleteCategoryById(UUID id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found!"));
        category.setDeleted(true);
        categoryRepository.save(category);
        log.info("Category with ID {} marked as deleted.", id);
    }

    private CategoryDTO convertToDto(Category category) {
        CategoryDTO dto = new CategoryDTO();
        dto.setId(category.getId());
        dto.setName(category.getName());
        return dto;
    }
}
package com.example.electrical_preorder_system_backend.service.category;

import com.example.electrical_preorder_system_backend.dto.request.CreateCategoryRequest;
import com.example.electrical_preorder_system_backend.dto.request.UpdateCategoryRequest;
import com.example.electrical_preorder_system_backend.dto.response.CategoryDTO;
import com.example.electrical_preorder_system_backend.entity.Category;
import com.example.electrical_preorder_system_backend.exception.AlreadyExistsException;
import com.example.electrical_preorder_system_backend.exception.ResourceNotFoundException;
import com.example.electrical_preorder_system_backend.mapper.CategoryMapper;
import com.example.electrical_preorder_system_backend.repository.CategoryRepository;
import com.example.electrical_preorder_system_backend.service.product.IProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
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
    private final IProductService productService;

    @Override
    @Cacheable(value = "categories")
    public List<CategoryDTO> getAllCategories() {
        log.info("Fetching active categories from the database.");
        List<Category> categories = categoryRepository.findByIsDeletedFalseOrderByNameAsc();
        return categories.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Cacheable(value = "categories", key = "'search_' + #searchTerm")
    public List<CategoryDTO> searchCategories(String searchTerm) {
        log.info("Searching categories with term: {}", searchTerm);

        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return getAllCategories();
        }

        List<Category> categories = categoryRepository.searchCategoriesByName(searchTerm.trim());
        return categories.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Cacheable(value = "categories", key = "'category-' + #id")
    public CategoryDTO getCategoryById(UUID id) {
        log.info("Fetching category from database with ID: {}", id);
        Category category = categoryRepository.findById(id)
                .filter(cat -> !cat.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found!"));

        return CategoryMapper.toCategoryDTO(category);
    }

    @Override
    @Transactional
    @CacheEvict(value = "categories", allEntries = true)
    public CategoryDTO createCategory(CreateCategoryRequest request) {
        String trimmedName = request.getName().trim();

        Category existingCategory = categoryRepository.findByNameIgnoreCase(trimmedName);

        if (existingCategory != null) {
            if (existingCategory.isDeleted()) {
                existingCategory.setDeleted(false);
                return convertToDto(categoryRepository.save(existingCategory));
            } else {
                throw new AlreadyExistsException("Category '" + trimmedName + "' already exists.");
            }
        }

        Category newCategory = new Category(trimmedName);
        return convertToDto(categoryRepository.save(newCategory));
    }

    @Override
    @Transactional
    @CacheEvict(value = "categories", allEntries = true)
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
        productService.clearProductCache();
        log.info("Category with ID {} updated and product cache cleared.", id);
        return convertToDto(existingCategory);
    }

    @Override
    @CacheEvict(value = "categories", allEntries = true)
    public void deleteCategoryById(UUID id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found!"));
        category.setDeleted(true);
        categoryRepository.save(category);
        productService.clearProductCache();
        log.info("Category with ID {} marked as deleted and product cache cleared.", id);
    }

    private CategoryDTO convertToDto(Category category) {
        CategoryDTO dto = new CategoryDTO();
        dto.setId(category.getId());
        dto.setName(category.getName());
        return dto;
    }
}
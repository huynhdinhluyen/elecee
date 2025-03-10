package com.example.electrical_preorder_system_backend.repository;

import com.example.electrical_preorder_system_backend.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CategoryRepository extends JpaRepository<Category, UUID> {
    Category findByName(String name);

    List<Category> findByIsDeletedFalseOrderByNameAsc();

    Category findByNameIgnoreCase(String trimmedName);
}

package com.example.electrical_preorder_system_backend.repository;

import com.example.electrical_preorder_system_backend.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ProductRepository extends JpaRepository<Product, UUID> {
    boolean existsByProductCode(String productCode);

    Product findByProductCode(String productCode);

    Page<Product> findByIsDeletedFalse(Pageable pageable);

    Page<Product> findByCategory_NameAndIsDeletedFalse(String categoryName, Pageable pageable);

    Page<Product> findByNameContainingIgnoreCaseAndIsDeletedFalse(String name, Pageable pageable);
}

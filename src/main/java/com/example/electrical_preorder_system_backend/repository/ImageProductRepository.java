package com.example.electrical_preorder_system_backend.repository;

import com.example.electrical_preorder_system_backend.entity.ImageProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ImageProductRepository extends JpaRepository<ImageProduct, UUID> {
}

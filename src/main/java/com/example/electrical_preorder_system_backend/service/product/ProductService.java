package com.example.electrical_preorder_system_backend.service.product;

import com.example.electrical_preorder_system_backend.dto.request.CreateProductRequest;
import com.example.electrical_preorder_system_backend.dto.request.UpdateProductRequest;
import com.example.electrical_preorder_system_backend.dto.response.CategoryDTO;
import com.example.electrical_preorder_system_backend.dto.response.ImageProductDTO;
import com.example.electrical_preorder_system_backend.dto.response.ProductDTO;
import com.example.electrical_preorder_system_backend.entity.Category;
import com.example.electrical_preorder_system_backend.entity.Product;
import com.example.electrical_preorder_system_backend.enums.ProductStatus;
import com.example.electrical_preorder_system_backend.exception.AlreadyExistsException;
import com.example.electrical_preorder_system_backend.exception.ResourceNotFoundException;
import com.example.electrical_preorder_system_backend.repository.CategoryRepository;
import com.example.electrical_preorder_system_backend.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService implements IProductService {
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ModelMapper modelMapper;

    @Override
    @CacheEvict(value = "products", allEntries = true)
    public Product addProduct(CreateProductRequest request) {
        if (productRepository.existsByProductCode(request.getProductCode())) {
            throw new AlreadyExistsException("Product code '" + request.getProductCode() + "' already exists.");
        }

        String categoryName = request.getCategory().getName();

        Category category = categoryRepository.findByName(categoryName);

        if (category == null) {
            category = new Category(categoryName);
            category = categoryRepository.save(category);
        }

        CategoryDTO categoryDto = new CategoryDTO();
        categoryDto.setId(category.getId());
        categoryDto.setName(category.getName());
        request.setCategory(categoryDto);

        Product product = createProduct(request, category);

        return productRepository.save(product);
    }

    private Product createProduct(CreateProductRequest request, Category category) {
        Product product = new Product(
                request.getProductCode(),
                request.getName(),
                request.getQuantity(),
                request.getDescription(),
                request.getPrice(),
                request.getPosition(),
                category
        );
        product.setStatus(ProductStatus.AVAILABLE);
        return product;
    }

    @Override
    @Cacheable(value = "products", key = "#pageable.pageNumber + '-' + #pageable.pageSize")
    public Page<Product> getProducts(Pageable pageable) {
        return productRepository.findByIsDeletedFalse(pageable);
    }

    @Override
    public Page<Product> getProductsByCategory(String categoryName, Pageable pageable) {
        return productRepository.findByCategory_NameAndIsDeletedFalse(categoryName, pageable);
    }

    @Override
    public Page<Product> getProductsByName(String name, Pageable pageable) {
        return productRepository.findByNameContainingIgnoreCaseAndIsDeletedFalse(name, pageable);
    }

    @Override
    public Product getProductByProductCode(String productCode) {
        Product product = productRepository.findByProductCode(productCode);
        if (product == null || product.isDeleted()) {
            throw new ResourceNotFoundException("Product not found with code: " + productCode);
        }
        return product;
    }

    @Override
    public Product getProductById(UUID id) {
        return productRepository.findById(id)
                .filter(p -> !p.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found!"));
    }

    @Override
    @CacheEvict(value = "products", allEntries = true)
    public Product updateProduct(UpdateProductRequest request, UUID id) {
        return productRepository.findById(id)
                .map(existingProduct -> {
                    if (existingProduct.isDeleted()) {
                        throw new IllegalStateException("Product has been deleted and cannot be updated.");
                    }
                    return updateExistingProduct(existingProduct, request);
                })
                .map(productRepository::save)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found!"));
    }

    private Product updateExistingProduct(Product existingProduct, UpdateProductRequest request) {
        if (request.getProductCode() != null && !request.getProductCode().isBlank()) {
            existingProduct.setProductCode(request.getProductCode());
        }

        if (request.getName() != null && !request.getName().isBlank()) {
            existingProduct.setName(request.getName());
        }

        if (request.getQuantity() != null) {
            existingProduct.setQuantity(request.getQuantity());
        }

        if (request.getDescription() != null && !request.getDescription().isBlank()) {
            existingProduct.setDescription(request.getDescription());
        }

        if (request.getPrice() != null) {
            existingProduct.setPrice(request.getPrice());
        }

        if (request.getPosition() != null) {
            existingProduct.setPosition(request.getPosition());
        }

        if (request.getCategory() != null &&
                request.getCategory().getName() != null &&
                !request.getCategory().getName().isBlank()) {
            Category category = categoryRepository.findByName(request.getCategory().getName());
            if (category == null) {
                throw new ResourceNotFoundException("Category not found!");
            }
            existingProduct.setCategory(category);
        }
        return existingProduct;
    }

    @Override
    @CacheEvict(value = "products", allEntries = true)
    public void deleteProductById(UUID id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found!"));
        product.setDeleted(true);
        productRepository.save(product);
    }

    @Override
    public Long countProducts() {
        return productRepository.count();
    }

    @Override
    public Page<ProductDTO> getConvertedProducts(Pageable pageable) {
        return getProducts(pageable).map(this::convertToDto);
    }

    @Override
    public ProductDTO convertToDto(Product product) {
        ProductDTO dto = modelMapper.map(product, ProductDTO.class);

        CategoryDTO categoryDto = new CategoryDTO();
        categoryDto.setId(product.getCategory().getId());
        categoryDto.setName(product.getCategory().getName());
        dto.setCategory(categoryDto);

        List<ImageProductDTO> imageDtos = product.getImageProducts().stream().map(ip -> {
            ImageProductDTO imgDto = new ImageProductDTO();
            imgDto.setAltText(ip.getAltText());
            imgDto.setImageUrl(ip.getImageUrl());
            return imgDto;
        }).collect(Collectors.toList());
        dto.setImageProducts(imageDtos);

        return dto;
    }
}

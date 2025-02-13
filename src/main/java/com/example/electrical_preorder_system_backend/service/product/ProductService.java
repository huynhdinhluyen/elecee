package com.example.electrical_preorder_system_backend.service.product;

import com.example.electrical_preorder_system_backend.dto.request.CreateProductRequest;
import com.example.electrical_preorder_system_backend.dto.request.UpdateProductRequest;
import com.example.electrical_preorder_system_backend.dto.response.CategoryDTO;
import com.example.electrical_preorder_system_backend.dto.response.ImageProductDTO;
import com.example.electrical_preorder_system_backend.dto.response.ProductDTO;
import com.example.electrical_preorder_system_backend.entity.Category;
import com.example.electrical_preorder_system_backend.entity.ImageProduct;
import com.example.electrical_preorder_system_backend.entity.Product;
import com.example.electrical_preorder_system_backend.enums.ProductStatus;
import com.example.electrical_preorder_system_backend.exception.AlreadyExistsException;
import com.example.electrical_preorder_system_backend.exception.ResourceNotFoundException;
import com.example.electrical_preorder_system_backend.repository.CategoryRepository;
import com.example.electrical_preorder_system_backend.repository.ProductRepository;
import com.example.electrical_preorder_system_backend.util.SlugUtil;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.example.electrical_preorder_system_backend.util.SlugUtil.generateSlug;

@Service
@RequiredArgsConstructor
public class ProductService implements IProductService {
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ModelMapper modelMapper;

    @Override
    @CacheEvict(value = "products", allEntries = true)
    public Product addProduct(CreateProductRequest request) {
        if (productRepository.existsByProductCode(request.getProductCode().trim())) {
            throw new AlreadyExistsException("Product code '" + request.getProductCode() + "' already exists.");
        }

        String categoryName = request.getCategory().getName().trim();
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
        product = productRepository.save(product);

        if (request.getPosition() != null) {
            adjustPositions(product, request.getPosition());
        }
        return product;
    }

    private Product createProduct(CreateProductRequest request, Category category) {
        Product product = new Product(
                request.getProductCode().trim(),
                request.getName().trim(),
                request.getQuantity(),
                request.getDescription(),
                request.getPrice(),
                request.getPosition(),
                category
        );
        product.setStatus(ProductStatus.AVAILABLE);
        product.setSlug(generateUniqueSlug(product.getName()));

        if (request.getImageProducts() != null && !request.getImageProducts().isEmpty()) {
            List<ImageProduct> imageProducts = request.getImageProducts().stream().map(dto -> {
                ImageProduct imageProduct = new ImageProduct();
                imageProduct.setAltText(dto.getAltText());
                imageProduct.setImageUrl(dto.getImageUrl());
                imageProduct.setProduct(product);
                return imageProduct;
            }).collect(Collectors.toList());
            product.setImageProducts(imageProducts);
        }
        return product;
    }

    @Override
    @Cacheable(value = "products", key = "#pageable.pageNumber + '-' + #pageable.pageSize")
    public Page<Product> getProducts(Pageable pageable) {
        return productRepository.findActiveProductsSorted(pageable);
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
        Product product = productRepository.findByProductCode(productCode.trim());
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
    @Transactional
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
            existingProduct.setProductCode(request.getProductCode().trim());
        }
        if (request.getName() != null && !request.getName().isBlank()) {
            String newName = request.getName().trim();
            existingProduct.setName(newName);
            existingProduct.setSlug(generateUniqueSlug(newName));
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
            int newPosition = request.getPosition();
            if (!Integer.valueOf(newPosition).equals(existingProduct.getPosition())) {
                adjustPositions(existingProduct, newPosition);
            }
        }

        // Update category if provided
        if (request.getCategory() != null &&
                request.getCategory().getName() != null &&
                !request.getCategory().getName().isBlank()) {
            String catName = request.getCategory().getName().trim();
            Category category = categoryRepository.findByName(catName);
            if (category == null) {
                throw new ResourceNotFoundException("Category not found!");
            }
            existingProduct.setCategory(category);
        }

        // Process image products: Soft-delete removed images and add new images if provided
        if (request.getImageProducts() != null) {
            Set<String> newImageUrls = request.getImageProducts().stream()
                    .map(ImageProductDTO::getImageUrl)
                    .collect(Collectors.toSet());

            // Mark existing images as deleted if not present in new request
            existingProduct.getImageProducts().forEach(img -> {
                if (!newImageUrls.contains(img.getImageUrl())) {
                    img.setDeleted(true);
                }
            });

            // Collect active (non-deleted) image URLs from existing product
            Set<String> existingActiveImageUrls = existingProduct.getImageProducts().stream()
                    .filter(img -> !img.isDeleted())
                    .map(ImageProduct::getImageUrl)
                    .collect(Collectors.toSet());

            // Add new images from the request if not already present
            request.getImageProducts().forEach(dto -> {
                if (!existingActiveImageUrls.contains(dto.getImageUrl())) {
                    ImageProduct newImage = new ImageProduct();
                    newImage.setAltText(dto.getAltText());
                    newImage.setImageUrl(dto.getImageUrl());
                    newImage.setDeleted(false);
                    newImage.setProduct(existingProduct);
                    existingProduct.getImageProducts().add(newImage);
                }
            });
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

        List<ImageProductDTO> imageDtos = product.getImageProducts().stream()
                .filter(ip -> !ip.isDeleted())
                .map(ip -> {
                    ImageProductDTO imgDto = new ImageProductDTO();
                    imgDto.setAltText(ip.getAltText());
                    imgDto.setImageUrl(ip.getImageUrl());
                    return imgDto;
                }).collect(Collectors.toList());
        dto.setImageProducts(imageDtos);
        return dto;
    }

    private String generateUniqueSlug(String name) {
        String baseSlug = SlugUtil.generateSlug(name);
        String uniqueSlug = baseSlug;
        int count = 1;
        while (productRepository.existsBySlug(uniqueSlug)) {
            uniqueSlug = baseSlug + "-" + count;
            count++;
        }
        return uniqueSlug;
    }

    // Utility method to adjust positions for active products when a product is inserted or updated with a new position.
    private void adjustPositions(Product product, int newPosition) {
        // Retrieve all active products sorted by position (and created_at as tie-breaker)
        List<Product> activeProducts = productRepository.findActiveProductsSorted(Pageable.unpaged()).getContent();
        // Remove the current product if present
        activeProducts.removeIf(p -> p.getId().equals(product.getId()));
        // Clamp newPosition between 1 and activeProducts.size() + 1 (1-indexed)
        if (newPosition < 1) {
            newPosition = 1;
        } else if (newPosition > activeProducts.size() + 1) {
            newPosition = activeProducts.size() + 1;
        }
        // Insert product at new position (0-indexed insertion)
        activeProducts.add(newPosition - 1, product);
        // Reassign positions for all products in the list
        int pos = 1;
        for (Product p : activeProducts) {
            if (p.getPosition() == null || !p.getPosition().equals(pos)) {
                p.setPosition(pos);
                productRepository.save(p);
            }
            pos++;
        }
    }
}
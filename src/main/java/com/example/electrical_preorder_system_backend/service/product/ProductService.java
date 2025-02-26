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
import com.example.electrical_preorder_system_backend.service.CloudinaryService;
import com.example.electrical_preorder_system_backend.util.SlugUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService implements IProductService {
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final CloudinaryService cloudinaryService;

    @Override
    @CacheEvict(value = "products", allEntries = true)
    public Product addProduct(CreateProductRequest request, List<MultipartFile> files) {
        String productCode = request.getProductCode().trim();
        if (productRepository.existsByProductCode(productCode)) {
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

        Product product = new Product(
                productCode,
                request.getName().trim(),
                request.getQuantity(),
                request.getDescription(),
                request.getPrice(),
                request.getPosition(),
                category
        );
        product.setStatus(ProductStatus.AVAILABLE);
        product.setSlug(generateUniqueSlug(product.getName()));

        if (files != null && !files.isEmpty()) {
            List<CompletableFuture<String>> futures = files.stream()
                    .map(cloudinaryService::uploadFileAsync)
                    .toList();
            List<String> imageUrls = futures.stream()
                    .map(CompletableFuture::join)
                    .toList();
            Product finalProduct = product;
            List<ImageProduct> imageProducts = imageUrls.stream().map(url -> {
                ImageProduct image = new ImageProduct();
                image.setAltText(request.getName());
                image.setImageUrl(url);
                image.setProduct(finalProduct);
                image.setDeleted(false);
                return image;
            }).collect(Collectors.toList());
            product.setImageProducts(imageProducts);
        }

        product = productRepository.save(product);

        if (request.getPosition() != null) {
            adjustPositions(product, request.getPosition());
        }
        return product;
    }

    @Override
    @Cacheable(value = "products", key = "#pageable.pageNumber + '-' + #pageable.pageSize + '-' + #pageable.sort.toString()")
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
    public Product getProductById(UUID id) {
        return productRepository.findById(id)
                .filter(p -> !p.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found!"));
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
    @Transactional
    @CacheEvict(value = "products", allEntries = true)
    public Product updateProduct(UpdateProductRequest request, UUID id, List<MultipartFile> files) {
        return productRepository.findById(id)
                .map(existingProduct -> {
                    if (existingProduct.isDeleted()) {
                        throw new IllegalStateException("Product has been deleted and cannot be updated.");
                    }
                    return updateExistingProduct(existingProduct, request, files);
                })
                .map(productRepository::save)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found!"));
    }

    private Product updateExistingProduct(Product existingProduct, UpdateProductRequest request, List<MultipartFile> files) {
        updateBasicFields(existingProduct, request);
        updateCategory(existingProduct, request);
        updateImageProducts(existingProduct, request, files);
        return existingProduct;
    }

    private void updateBasicFields(Product product, UpdateProductRequest request) {
        if (request.getProductCode() != null && !request.getProductCode().isBlank()) {
            product.setProductCode(request.getProductCode().trim());
        }
        if (request.getName() != null && !request.getName().isBlank()) {
            String newName = request.getName().trim();
            product.setName(newName);
            product.setSlug(generateUniqueSlug(newName));
        }
        if (request.getQuantity() != null) {
            product.setQuantity(request.getQuantity());
        }
        if (request.getDescription() != null && !request.getDescription().isBlank()) {
            product.setDescription(request.getDescription());
        }
        if (request.getPrice() != null) {
            product.setPrice(request.getPrice());
        }
        if (request.getPosition() != null && !request.getPosition().equals(product.getPosition())) {
            adjustPositions(product, request.getPosition());
        }
    }

    private void updateCategory(Product product, UpdateProductRequest request) {
        if (request.getCategory() != null &&
                request.getCategory().getName() != null &&
                !request.getCategory().getName().isBlank()) {
            String catName = request.getCategory().getName().trim();
            Category category = categoryRepository.findByName(catName);
            if (category == null) {
                throw new ResourceNotFoundException("Category not found!");
            }
            product.setCategory(category);
        }
    }

    private void updateImageProducts(Product product, UpdateProductRequest request, List<MultipartFile> files) {
        if (files != null && !files.isEmpty()) {
            // Soft-delete all existing images
            if (product.getImageProducts() != null) {
                product.getImageProducts().forEach(img -> img.setDeleted(true));
            }
            // Asynchronously upload each new image and wait for the URLs
            List<CompletableFuture<String>> futures = files.stream()
                    .map(cloudinaryService::uploadFileAsync)
                    .toList();
            List<String> imageUrls = futures.stream()
                    .map(CompletableFuture::join)
                    .toList();
            // Map each URL to a new ImageProduct entity
            List<ImageProduct> newImages = imageUrls.stream().map(url -> {
                ImageProduct ip = new ImageProduct();
                // Here we use the product name as alt text; adjust as needed.
                ip.setAltText(request.getName());
                ip.setImageUrl(url);
                ip.setDeleted(false);
                ip.setProduct(product);
                return ip;
            }).collect(Collectors.toList());
            product.setImageProducts(newImages);
        }
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
    @CacheEvict(value = "products", allEntries = true)
    public void deleteProducts(List<UUID> ids) {
        List<Product> products = productRepository.findAllById(ids)
                .stream()
                .filter(p -> !p.isDeleted())
                .collect(Collectors.toList());

        if (products.size() != ids.size()) {
            throw new ResourceNotFoundException("Some products were not found or already deleted.");
        }

        products.forEach(p -> p.setDeleted(true));
        productRepository.saveAll(products);
    }

    @Override
    public Long countProducts() {
        return productRepository.countActiveProducts();
    }

    @Override
    public Page<ProductDTO> getConvertedProducts(Pageable pageable) {
        return getProducts(pageable).map(this::convertToDto);
    }

    @Override
    public ProductDTO convertToDto(Product product) {
        if (product == null) {
            return null;
        }

        ProductDTO dto = new ProductDTO();
        dto.setId(product.getId());
        dto.setProductCode(product.getProductCode());
        dto.setName(product.getName());
        dto.setSlug(product.getSlug());
        dto.setQuantity(product.getQuantity());
        dto.setDescription(product.getDescription());
        dto.setPrice(product.getPrice());
        dto.setPosition(product.getPosition());
        dto.setStatus(product.getStatus());
        dto.setDeleted(product.isDeleted());
        dto.setCreatedAt(product.getCreatedAt());
        dto.setUpdatedAt(product.getUpdatedAt());

        if (product.getCategory() != null) {
            CategoryDTO categoryDto = new CategoryDTO();
            categoryDto.setId(product.getCategory().getId());
            categoryDto.setName(product.getCategory().getName());
            dto.setCategory(categoryDto);
        }

        List<ImageProductDTO> imageDtos = product.getImageProducts().stream()
                .filter(ip -> !ip.isDeleted())
                .map(ip -> {
                    ImageProductDTO imgDto = new ImageProductDTO();
                    imgDto.setAltText(ip.getAltText());
                    imgDto.setImageUrl(ip.getImageUrl());
                    return imgDto;
                })
                .collect(Collectors.toList());
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

    private void adjustPositions(Product product, int newPosition) {
        try {
            List<Product> activeProducts = productRepository.findActiveProductsSorted(Pageable.unpaged()).getContent();
            activeProducts.removeIf(p -> p.getId().equals(product.getId()));

            if (newPosition < 1) {
                newPosition = 1;
            } else if (newPosition > activeProducts.size() + 1) {
                newPosition = activeProducts.size() + 1;
            }
            activeProducts.add(newPosition - 1, product);

            int pos = 1;
            for (Product p : activeProducts) {
                p.setPosition(pos++);
            }

            productRepository.saveAll(activeProducts);
        } catch (Exception ex) {
            log.error("Error adjusting product positions for product ID {}: {}", product.getId(), ex.getMessage(), ex);
        }
    }

    @Override
    public Page<Product> searchProducts(String query, Pageable pageable) {
        if (query == null || query.isBlank()) {
            return getProducts(pageable);
        }
        return productRepository.searchProducts(query, pageable);
    }

    @Override
    public Page<Product> searchProducts(String query, String category, Pageable pageable) {
        if (query == null || query.isBlank()) {
            return productRepository.findByCategory_NameAndIsDeletedFalse(category, pageable);
        }
        return productRepository.searchProductsByCategory(query, category, pageable);
    }
}
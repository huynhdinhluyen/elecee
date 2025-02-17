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
        if (productRepository.existsByProductCode(request.getProductCode().trim())) {
            throw new AlreadyExistsException("Product code '" + request.getProductCode() + "' already exists.");
        }

        // Lấy hoặc tạo mới Category
        String categoryName = request.getCategory().getName().trim();
        Category category = categoryRepository.findByName(categoryName);
        if (category == null) {
            category = new Category(categoryName);
            category = categoryRepository.save(category);
        }

        // Tạo đối tượng Product mới
        Product product = new Product(
                request.getProductCode(),
                request.getName().trim(),
                request.getQuantity(),
                request.getDescription(),
                request.getPrice(),
                request.getPosition(),
                category
        );
        product.setStatus(ProductStatus.AVAILABLE);
        product.setSlug(generateUniqueSlug(product.getName()));

        // Xử lý file upload: Upload từng file qua Cloudinary trong tầng service
        if (files != null && !files.isEmpty()) {
            Product finalProduct = product;
            List<ImageProduct> imageProducts = files.stream().map(file -> {
                // Thực hiện upload file qua CloudinaryService
                String imageUrl = cloudinaryService.uploadFile(file);
                ImageProduct img = new ImageProduct();
                img.setAltText(file.getOriginalFilename());
                img.setImageUrl(imageUrl);
                img.setProduct(finalProduct);
                return img;
            }).collect(Collectors.toList());
            product.setImageProducts(imageProducts);
        }

        product = productRepository.save(product);

        // Điều chỉnh vị trí nếu cần
        if (request.getPosition() != null) {
            adjustPositions(product, request.getPosition());
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
    public Product getProductById(UUID id) {
        return productRepository.findById(id)
                .filter(p -> !p.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found!"));
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

        if (files != null && !files.isEmpty()) {
            if (existingProduct.getImageProducts() != null) {
                existingProduct.getImageProducts().forEach(img -> img.setDeleted(true));
            }
            List<ImageProduct> newImages = files.stream().map(file -> {
                String imageUrl = cloudinaryService.uploadFile(file);
                ImageProduct newImage = new ImageProduct();
                newImage.setAltText(file.getOriginalFilename());
                newImage.setImageUrl(imageUrl);
                newImage.setDeleted(false);
                newImage.setProduct(existingProduct);
                return newImage;
            }).collect(Collectors.toList());
            existingProduct.setImageProducts(newImages);
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

            // Cập nhật lại thứ tự vị trí
            int pos = 1;
            for (Product p : activeProducts) {
                p.setPosition(pos++);
            }

            // Batch update để giảm số lần gọi cơ sở dữ liệu
            productRepository.saveAll(activeProducts);
        } catch (Exception ex) {
            log.error("Error adjusting product positions for product ID {}: {}", product.getId(), ex.getMessage(), ex);
        }
    }
}
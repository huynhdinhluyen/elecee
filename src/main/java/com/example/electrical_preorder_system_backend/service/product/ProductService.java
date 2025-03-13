package com.example.electrical_preorder_system_backend.service.product;

import com.example.electrical_preorder_system_backend.dto.cache.CachedProductPage;
import com.example.electrical_preorder_system_backend.dto.filter.ProductFilterCriteria;
import com.example.electrical_preorder_system_backend.dto.request.product.CreateProductRequest;
import com.example.electrical_preorder_system_backend.dto.request.product.UpdateProductRequest;
import com.example.electrical_preorder_system_backend.dto.response.campaign.SimplifiedCampaignDTO;
import com.example.electrical_preorder_system_backend.dto.response.campaign_stage.CampaignStageDTO;
import com.example.electrical_preorder_system_backend.dto.response.category.CategoryDTO;
import com.example.electrical_preorder_system_backend.dto.response.product.ProductDTO;
import com.example.electrical_preorder_system_backend.dto.response.product.ProductDetailDTO;
import com.example.electrical_preorder_system_backend.dto.response.product_images.ImageProductDTO;
import com.example.electrical_preorder_system_backend.entity.Campaign;
import com.example.electrical_preorder_system_backend.entity.Category;
import com.example.electrical_preorder_system_backend.entity.ImageProduct;
import com.example.electrical_preorder_system_backend.entity.Product;
import com.example.electrical_preorder_system_backend.enums.CampaignStatus;
import com.example.electrical_preorder_system_backend.enums.ProductStatus;
import com.example.electrical_preorder_system_backend.exception.AlreadyExistsException;
import com.example.electrical_preorder_system_backend.exception.ResourceNotFoundException;
import com.example.electrical_preorder_system_backend.repository.CampaignRepository;
import com.example.electrical_preorder_system_backend.repository.CategoryRepository;
import com.example.electrical_preorder_system_backend.repository.ProductRepository;
import com.example.electrical_preorder_system_backend.repository.specification.ProductSpecifications;
import com.example.electrical_preorder_system_backend.service.campaign_stage.ICampaignStageService;
import com.example.electrical_preorder_system_backend.service.cloudinary.CloudinaryService;
import com.example.electrical_preorder_system_backend.util.SlugUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService implements IProductService {
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final CloudinaryService cloudinaryService;
    private final RedisTemplate<String, Object> redisTemplate;
    private final CampaignRepository campaignRepository;
    private final ICampaignStageService campaignStageService;

    @Override
    public Page<ProductDTO> getProducts(ProductFilterCriteria criteria, Pageable pageable) {
        String cacheKey = generateCacheKey(criteria, pageable);

        Page<ProductDTO> cachedResult = getCachedProductPage(cacheKey);
        if (cachedResult != null) {
            return cachedResult;
        }

        Specification<Product> spec = Specification.where(ProductSpecifications.isNotDeleted());

        if (criteria.getCategory() != null && !criteria.getCategory().isBlank()) {
            spec = spec.and(ProductSpecifications.hasCategory(criteria.getCategory().trim()));
        }

        if (criteria.getQuery() != null && !criteria.getQuery().isBlank()) {
            spec = spec.and(ProductSpecifications.matchesQuery(criteria.getQuery().trim()));
        }

        if (criteria.getMinPrice() != null) {
            spec = spec.and(ProductSpecifications.priceGreaterThanOrEqual(criteria.getMinPrice()));
        }

        if (criteria.getMaxPrice() != null) {
            spec = spec.and(ProductSpecifications.priceLessThanOrEqual(criteria.getMaxPrice()));
        }

        Page<ProductDTO> result = productRepository.findAll(spec, pageable)
                .map(this::convertToDto);

        cacheProductPage(cacheKey, result);

        return result;
    }

    private String generateCacheKey(ProductFilterCriteria criteria, Pageable pageable) {
        StringBuilder sb = new StringBuilder("products-filtered-");
        sb.append(criteria.hashCode())
                .append('-')
                .append(pageable.getPageNumber())
                .append('-')
                .append(pageable.getPageSize())
                .append('-')
                .append(pageable.getSort());
        return sb.toString();
    }

    private void cacheProductPage(String key, Page<ProductDTO> productPage) {
        try {
            CachedProductPage cachedPage = CachedProductPage.from(productPage);
            redisTemplate.opsForValue().set(key, cachedPage, 1, TimeUnit.HOURS);
            log.debug("Successfully cached product page with key: {}", key);
        } catch (Exception ex) {
            log.error("Failed to cache product page: {}", ex.getMessage());
        }
    }

    private Page<ProductDTO> getCachedProductPage(String key) {
        try {
            Object cachedObject = redisTemplate.opsForValue().get(key);
            if (cachedObject instanceof CachedProductPage) {
                log.debug("Cache hit for key: {}", key);
                return ((CachedProductPage) cachedObject).toPage();
            }
            log.debug("Cache miss for key: {}", key);
        } catch (Exception ex) {
            log.error("Error retrieving cached product page: {}", ex.getMessage());
        }
        return null;
    }

    @Override
    @CacheEvict(value = {"products"}, allEntries = true)
    public void clearProductCache() {
        try {
            Set<String> keys = redisTemplate.keys("products-filtered-*");
            if (!keys.isEmpty()) {
                redisTemplate.delete(keys);
                log.debug("Cleared {} filtered product cache entries", keys.size());
            }

            Set<String> detailKeys = redisTemplate.keys("product-detail-*");
            if (!detailKeys.isEmpty()) {
                redisTemplate.delete(detailKeys);
                log.debug("Cleared {} product detail cache entries", detailKeys.size());
            }
        } catch (Exception ex) {
            log.error("Error clearing product cache: {}", ex.getMessage());
        }
    }

    @Override
    public Product getProductBySlug(String slug) {
        Product product = productRepository.findBySlug(slug.trim());
        if (product == null || product.isDeleted()) {
            throw new ResourceNotFoundException("Product not found with slug: " + slug);
        }
        return product;
    }

    @Override
    @Cacheable(value = "products", key = "'product-detail-' + #slug")
    public ProductDetailDTO getProductDetailWithCampaigns(String slug) {
        log.info("Fetching product detail with campaigns for slug: {}", slug);

        // Get the product
        Product product = getProductBySlug(slug);
        ProductDTO productDTO = convertToDto(product);

        // Get all campaigns for this product
        List<Campaign> allCampaigns = campaignRepository.findActiveCampaignsByProductId(product.getId())
                .stream()
                .filter(c -> !c.isDeleted())
                .toList();

        // Find ACTIVE or SCHEDULED campaigns first (priority)
        Optional<Campaign> activeCampaign = allCampaigns.stream()
                .filter(c -> c.getStatus() == CampaignStatus.ACTIVE || c.getStatus() == CampaignStatus.SCHEDULED)
                .max(Comparator.comparing(Campaign::getCreatedAt));

        // If no active/scheduled campaign, get the most recent completed one
        Campaign campaignToShow = activeCampaign.orElse(
                allCampaigns.stream()
                        .max(Comparator.comparing(Campaign::getCreatedAt))
                        .orElse(null)
        );

        // Create response with the single campaign
        List<SimplifiedCampaignDTO> campaignDTOs = new ArrayList<>();
        if (campaignToShow != null) {
            SimplifiedCampaignDTO campaignDTO = new SimplifiedCampaignDTO();
            campaignDTO.setId(campaignToShow.getId());
            campaignDTO.setName(campaignToShow.getName());
            campaignDTO.setStartDate(campaignToShow.getStartDate());
            campaignDTO.setEndDate(campaignToShow.getEndDate());
            campaignDTO.setMinQuantity(campaignToShow.getMinQuantity());
            campaignDTO.setMaxQuantity(campaignToShow.getMaxQuantity());
            campaignDTO.setTotalAmount(campaignToShow.getTotalAmount());
            campaignDTO.setStatus(campaignToShow.getStatus().name());

            // Get stages for this campaign
            List<CampaignStageDTO> stages = campaignStageService.getConvertedCampaignStages(campaignToShow.getId());
            campaignDTO.setStages(stages);

            campaignDTOs.add(campaignDTO);
        }

        // Create final response
        ProductDetailDTO detailDTO = new ProductDetailDTO();
        detailDTO.setProduct(productDTO);
        detailDTO.setCampaigns(campaignDTOs);

        return detailDTO;
    }

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
        clearProductCache();
        log.info("Product added and cache cleared for id: {}", product.getId());
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
        clearProductCache();
        log.info("Product updated and cache cleared");
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
            Set<String> oldImageUrls = request.getOldImageProducts().stream()
                    .map(dto -> dto.getImageUrl().trim())
                    .collect(Collectors.toSet());

            if (product.getImageProducts() != null) {
                // Find images to delete
                List<String> imagesToDelete = new ArrayList<>();
                product.getImageProducts().forEach(img -> {
                    if (!oldImageUrls.contains(img.getImageUrl())) {
                        img.setDeleted(true);
                        imagesToDelete.add(img.getImageUrl());
                    }
                });

                // Delete from Cloudinary asynchronously
                if (!imagesToDelete.isEmpty()) {
                    deleteImagesFromCloudinary(imagesToDelete);
                }
            }

            List<CompletableFuture<String>> futures = files.stream()
                    .map(cloudinaryService::uploadFileAsync)
                    .toList();

            List<String> newImageUrls = futures.stream()
                    .map(CompletableFuture::join)
                    .toList();

            List<ImageProduct> newImages = newImageUrls.stream().map(url -> {
                ImageProduct ip = new ImageProduct();
                ip.setAltText(request.getName());
                ip.setImageUrl(url);
                ip.setDeleted(false);
                ip.setProduct(product);
                return ip;
            }).toList();

            product.getImageProducts().addAll(newImages);
        }
    }

    @Override
    @Transactional
    @CacheEvict(value = "products", allEntries = true)
    public void deleteProductById(UUID id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found!"));
        List<String> imageUrls = product.getImageProducts().stream()
                .filter(img -> !img.isDeleted())
                .map(ImageProduct::getImageUrl)
                .collect(Collectors.toList());

        deleteImagesFromCloudinary(imageUrls);
        product.setDeleted(true);
        productRepository.save(product);
        clearProductCache();
        log.info("Product marked deleted and cache cleared for id: {}", id);
    }

    @Override
    @Transactional
    @CacheEvict(value = "products", allEntries = true)
    public void deleteProducts(List<UUID> ids) {
        List<Product> products = productRepository.findAllById(ids)
                .stream()
                .filter(p -> !p.isDeleted())
                .collect(Collectors.toList());

        if (products.size() != ids.size()) {
            throw new ResourceNotFoundException("Some products were not found or already deleted.");
        }

        List<String> allImageUrls = products.stream()
                .flatMap(p -> p.getImageProducts().stream())
                .filter(img -> !img.isDeleted())
                .map(ImageProduct::getImageUrl)
                .collect(Collectors.toList());

        deleteImagesFromCloudinary(allImageUrls);

        products.forEach(p -> p.setDeleted(true));
        productRepository.saveAll(products);
        clearProductCache();
        log.info("Multiple products marked deleted and cache cleared for ids: {}", ids);
    }

    @Override
    public Long countProducts() {
        return productRepository.countActiveProducts();
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

    private void deleteImagesFromCloudinary(List<String> imageUrls) {
        if (imageUrls == null || imageUrls.isEmpty()) return;

        List<CompletableFuture<Boolean>> deleteFutures = imageUrls.stream()
                .map(cloudinaryService::deleteImageAsync)
                .toList();

        CompletableFuture.allOf(deleteFutures.toArray(new CompletableFuture[0]))
                .thenAccept(v -> {
                    long successCount = deleteFutures.stream()
                            .map(CompletableFuture::join)
                            .filter(success -> success)
                            .count();
                    log.info("Deleted {}/{} images from Cloudinary", successCount, imageUrls.size());
                })
                .exceptionally(ex -> {
                    log.error("Error deleting images from Cloudinary", ex);
                    return null;
                });
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
}
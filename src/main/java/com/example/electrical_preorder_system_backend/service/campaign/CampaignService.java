package com.example.electrical_preorder_system_backend.service.campaign;

import com.example.electrical_preorder_system_backend.dto.cache.CachedCampaignPage;
import com.example.electrical_preorder_system_backend.dto.filter.CampaignFilterCriteria;
import com.example.electrical_preorder_system_backend.dto.request.CreateCampaignRequest;
import com.example.electrical_preorder_system_backend.dto.request.UpdateCampaignRequest;
import com.example.electrical_preorder_system_backend.dto.response.CampaignDTO;
import com.example.electrical_preorder_system_backend.entity.Campaign;
import com.example.electrical_preorder_system_backend.entity.Product;
import com.example.electrical_preorder_system_backend.enums.CampaignStatus;
import com.example.electrical_preorder_system_backend.exception.AlreadyExistsException;
import com.example.electrical_preorder_system_backend.exception.CampaignStatusException;
import com.example.electrical_preorder_system_backend.exception.ResourceNotFoundException;
import com.example.electrical_preorder_system_backend.mapper.CampaignMapper;
import com.example.electrical_preorder_system_backend.repository.CampaignRepository;
import com.example.electrical_preorder_system_backend.repository.ProductRepository;
import com.example.electrical_preorder_system_backend.repository.specification.CampaignSpecifications;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class CampaignService implements ICampaignService {

    private final CampaignRepository campaignRepository;
    private final ProductRepository productRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public Page<CampaignDTO> getFilteredCampaigns(CampaignFilterCriteria criteria, Pageable pageable) {
        log.info("Searching campaigns with filters: name={}, status={}, productId={}",
                criteria.getName(), criteria.getStatus(), criteria.getProductId());

        String cacheKey = generateCacheKey(criteria, pageable);

        CachedCampaignPage cachedResult = getCachedCampaignPage(cacheKey);
        if (cachedResult != null) {
            log.info("Cache hit for campaigns with key: {}", cacheKey);
            return cachedResult.toPage();
        }

        log.info("Cache miss for campaigns, fetching from database");
        Specification<Campaign> spec = Specification.where(CampaignSpecifications.isNotDeleted());

        if (criteria.getName() != null && !criteria.getName().trim().isEmpty()) {
            spec = spec.and(CampaignSpecifications.nameLike(criteria.getName()));
        }

        if (criteria.getStatus() != null) {
            spec = spec.and(CampaignSpecifications.hasStatus(criteria.getStatus()));
        }

        if (criteria.getProductId() != null) {
            spec = spec.and(CampaignSpecifications.hasProductId(criteria.getProductId()));
        }

        if (criteria.getStartDateFrom() != null) {
            spec = spec.and(CampaignSpecifications.startDateAfterOrEqual(criteria.getStartDateFrom()));
        }

        if (criteria.getStartDateTo() != null) {
            spec = spec.and(CampaignSpecifications.startDateBeforeOrEqual(criteria.getStartDateTo()));
        }

        if (criteria.getEndDateFrom() != null) {
            spec = spec.and(CampaignSpecifications.endDateAfterOrEqual(criteria.getEndDateFrom()));
        }

        if (criteria.getEndDateTo() != null) {
            spec = spec.and(CampaignSpecifications.endDateBeforeOrEqual(criteria.getEndDateTo()));
        }

        Page<CampaignDTO> resultPage = campaignRepository.findAll(spec, pageable)
                .map(CampaignMapper::toCampaignDTO);

        cacheCampaignPage(cacheKey, resultPage);

        return resultPage;
    }

    private String generateCacheKey(CampaignFilterCriteria criteria, Pageable pageable) {
        return "campaigns-filtered-" +
                (criteria.getName() != null ? criteria.getName() : "") + "-" +
                (criteria.getStatus() != null ? criteria.getStatus() : "") + "-" +
                (criteria.getProductId() != null ? criteria.getProductId() : "") + "-" +
                (criteria.getStartDateFrom() != null ? criteria.getStartDateFrom() : "") + "-" +
                (criteria.getStartDateTo() != null ? criteria.getStartDateTo() : "") + "-" +
                (criteria.getEndDateFrom() != null ? criteria.getEndDateFrom() : "") + "-" +
                (criteria.getEndDateTo() != null ? criteria.getEndDateTo() : "") + "-" +
                pageable.getPageNumber() + "-" +
                pageable.getPageSize() + "-" +
                pageable.getSort().toString();
    }

    private CachedCampaignPage getCachedCampaignPage(String key) {
        try {
            Object value = redisTemplate.opsForValue().get(key);
            if (value instanceof CachedCampaignPage) {
                return (CachedCampaignPage) value;
            }
        } catch (Exception e) {
            log.error("Error retrieving cached campaign page: {}", e.getMessage());
        }
        return null;
    }

    private void cacheCampaignPage(String key, Page<CampaignDTO> page) {
        try {
            CachedCampaignPage cachedPage = CachedCampaignPage.from(page);
            redisTemplate.opsForValue().set(key, cachedPage, 60, TimeUnit.MINUTES);
            log.info("Cached campaign page with key: {}", key);
        } catch (Exception e) {
            log.error("Error caching campaign page: {}", e.getMessage());
        }
    }

    @Override
    @Cacheable(value = "campaigns", key = "'campaign-' + #id")
    public CampaignDTO getCampaignById(UUID id) {
        log.info("Fetching campaign from database with ID: {}", id);
        Campaign campaign = campaignRepository.findById(id)
                .filter(c -> !c.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Campaign not found with id: " + id));

        return CampaignMapper.toCampaignDTO(campaign);
    }

//    @Override
//    public Map<String, Object> getCampaignPerformanceMetrics(UUID campaignId) {
//        Campaign campaign = getCampaignById(campaignId);
//        List<CampaignStage> stages = getCampaignStagesByCampaignId(campaignId);
//
//        int totalSold = stages.stream().mapToInt(CampaignStage::getQuantitySold).sum();
//        double progressPercentage = campaign.getMaxQuantity() > 0 ?
//                (totalSold * 100.0 / campaign.getMaxQuantity()) : 0;
//
//        Map<String, Object> metrics = new HashMap<>();
//        metrics.put("totalSold", totalSold);
//        metrics.put("totalTarget", campaign.getMaxQuantity());
//        metrics.put("progressPercentage", progressPercentage);
//        metrics.put("activeStages", stages.stream()
//                .filter(s -> s.getStatus() == CampaignStageStatus.ACTIVE)
//                .count());
//
//        return metrics;
//    }

    @Override
    @Transactional
    @CacheEvict(value = "campaigns", allEntries = true)
    public Campaign createCampaign(CreateCampaignRequest request) {
        String campaignName = request.getName().trim();
        Campaign oldCampaign = campaignRepository.findByName(campaignName);
        if (oldCampaign != null && !oldCampaign.isDeleted()) {
            throw new AlreadyExistsException("Campaign with name '" + campaignName + "' already exists.");
        }
        LocalDateTime startDate = request.getStartDate();
        LocalDateTime endDate = request.getEndDate();
        validateCampaignDates(startDate, endDate);

        Campaign newCampaign = new Campaign();
        newCampaign.setName(campaignName);
        newCampaign.setStartDate(startDate);
        newCampaign.setEndDate(endDate);
        newCampaign.setMinQuantity(request.getMinQuantity());
        newCampaign.setMaxQuantity(request.getMaxQuantity());
        newCampaign.setTotalAmount(BigDecimal.valueOf(0));
        newCampaign.setStatus(determineCampaignStatus(startDate, endDate));

        UUID productId = UUID.fromString(request.getProductId().trim());
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + request.getProductId()));
        newCampaign.setProduct(product);

        newCampaign = campaignRepository.save(newCampaign);
        log.info("Campaign created with id {}", newCampaign.getId());
        clearCampaignCache();
        return newCampaign;
    }

    @Override
    @Transactional
    @CacheEvict(value = "campaigns", allEntries = true)
    public Campaign updateCampaign(UUID id, UpdateCampaignRequest request) {
        Campaign campaign = campaignRepository.findById(id)
                .filter(c -> !c.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Campaign not found with id: " + id));
        String campaignName = request.getName().trim();
        if (!campaign.getName().equalsIgnoreCase(campaignName)) {
            Campaign oldCampaign = campaignRepository.findByName(campaignName);
            if (oldCampaign != null && !oldCampaign.isDeleted()
                    && !oldCampaign.getId().equals(campaign.getId())) {
                throw new AlreadyExistsException("Campaign with name '" + campaignName + "' already exists.");
            }
            campaign.setName(campaignName);
        }

        LocalDateTime startDate = request.getStartDate();
        LocalDateTime endDate = request.getEndDate();
        if (startDate != null && endDate != null) {
            validateCampaignDates(startDate, endDate);
            campaign.setStartDate(startDate);
            campaign.setEndDate(endDate);
            campaign.setStatus(determineCampaignStatus(startDate, endDate));
        }

        if (request.getMinQuantity() != null) {
            campaign.setMinQuantity(request.getMinQuantity());
        }
        if (request.getMaxQuantity() != null) {
            campaign.setMaxQuantity(request.getMaxQuantity());
        }
        if (request.getTotalAmount() != null) {
            campaign.setTotalAmount(request.getTotalAmount());
        }
        if (request.getProductId() != null && !request.getProductId().trim().isEmpty()) {
            if (campaign.getStatus().equals(CampaignStatus.ACTIVE)
                    || campaign.getStatus().equals(CampaignStatus.CANCELLED)
                    || campaign.getStatus().equals(CampaignStatus.COMPLETED)) {
                throw new CampaignStatusException("Cannot change product in this status");
            }
            UUID prodId = UUID.fromString(request.getProductId().trim());
            Product product = productRepository.findById(prodId)
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + request.getProductId()));
            campaign.setProduct(product);
        }

        campaign = campaignRepository.save(campaign);
        log.info("Campaign updated with id {}", campaign.getId());
        clearCampaignCache();
        return campaign;
    }

    @Override
    @Transactional
    @CacheEvict(value = "campaigns", allEntries = true)
    public void deleteCampaign(UUID id) {
        Campaign campaign = campaignRepository.findById(id)
                .filter(c -> !c.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Campaign not found with id: " + id));
        campaign.setDeleted(true);
        campaignRepository.save(campaign);
        clearCampaignCache();
        log.info("Campaign marked as deleted with id {}", id);
    }

    private CampaignStatus determineCampaignStatus(LocalDateTime startDate, LocalDateTime endDate) {
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(startDate)) {
            return CampaignStatus.SCHEDULED;
        } else if (now.isAfter(endDate)) {
            return CampaignStatus.COMPLETED;
        } else {
            return CampaignStatus.ACTIVE;
        }
    }

    @Override
    @Transactional
    public void updateCampaignStatuses() {
        List<Campaign> campaigns = campaignRepository.findActiveCampaigns();
        LocalDateTime now = LocalDateTime.now();
        for (Campaign campaign : campaigns) {
            CampaignStatus currentStatus = campaign.getStatus();
            if (campaign.getStartDate().isAfter(now)) {
                campaign.setStatus(CampaignStatus.SCHEDULED);
            } else if (!campaign.getStartDate().isAfter(now) && campaign.getEndDate().isAfter(now)) {
                campaign.setStatus(CampaignStatus.ACTIVE);
            } else if (campaign.getEndDate().isBefore(now)) {
                campaign.setStatus(CampaignStatus.COMPLETED);
            }
            if (!currentStatus.equals(campaign.getStatus())) {
                log.info("Campaign {} status updated from {} to {}", campaign.getId(), currentStatus, campaign.getStatus());
            }
        }
        campaignRepository.saveAll(campaigns);
        clearCampaignCache();
    }

    @Override
    public CampaignDTO convertToDto(Campaign campaign) {
        if (campaign == null) {
            return null;
        }
        return CampaignMapper.toCampaignDTO(campaign);
    }

    @Override
    @CacheEvict(value = "campaigns", allEntries = true)
    public void clearCampaignCache() {
        log.info("Cleared campaign cache");
    }

    private void validateCampaignDates(LocalDateTime startDate, LocalDateTime endDate) {
        LocalDateTime now = LocalDateTime.now();
        if (startDate.isBefore(now)) {
            throw new IllegalArgumentException("Campaign start date cannot be in the past.");
        }
        if (!startDate.isBefore(endDate)) {
            throw new IllegalArgumentException("Campaign start date must be before the end date.");
        }
    }
}
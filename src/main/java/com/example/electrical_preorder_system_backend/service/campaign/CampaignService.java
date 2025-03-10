package com.example.electrical_preorder_system_backend.service.campaign;

import com.example.electrical_preorder_system_backend.dto.request.CreateCampaignRequest;
import com.example.electrical_preorder_system_backend.dto.request.UpdateCampaignRequest;
import com.example.electrical_preorder_system_backend.dto.response.CampaignDTO;
import com.example.electrical_preorder_system_backend.dto.response.ProductDTO;
import com.example.electrical_preorder_system_backend.entity.Campaign;
import com.example.electrical_preorder_system_backend.entity.Product;
import com.example.electrical_preorder_system_backend.enums.CampaignStatus;
import com.example.electrical_preorder_system_backend.exception.AlreadyExistsException;
import com.example.electrical_preorder_system_backend.exception.CampaignStatusException;
import com.example.electrical_preorder_system_backend.exception.ResourceNotFoundException;
import com.example.electrical_preorder_system_backend.repository.CampaignRepository;
import com.example.electrical_preorder_system_backend.repository.ProductRepository;
import com.example.electrical_preorder_system_backend.service.product.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CampaignService implements ICampaignService {

    private final CampaignRepository campaignRepository;
    private final ProductRepository productRepository;
    private final ProductService productService;

//    @Override
//    @Cacheable(value = "campaigns",
//            key = "'filtered-' + #criteria.hashCode() + '-' + #pageable.pageNumber + '-' + #pageable.pageSize")
//    public Page<Campaign> getFilteredCampaigns(CampaignFilterCriteria criteria, Pageable pageable) {
//        Specification<Campaign> spec = Specification.where((root, query, cb) -> cb.equal(root.get("deleted"), false));
//
//        if (criteria.getProductId() != null) {
//            spec = spec.and((root, query, cb) -> cb.equal(root.get("product").get("id"), criteria.getProductId()));
//        }
//
//        if (criteria.getStatus() != null) {
//            spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), criteria.getStatus()));
//        }
//
//        if (criteria.getStartDateFrom() != null) {
//            spec = spec.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("startDate"), criteria.getStartDateFrom()));
//        }
//
//        return campaignRepository.findAll(spec, pageable);
//    }

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
//    @CacheEvict(value = "campaigns", allEntries = true)
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
        newCampaign.setTotalAmount(request.getTotalAmount());
        newCampaign.setStatus(determineCampaignStatus(startDate, endDate));

        UUID productId = UUID.fromString(request.getProductId().trim());
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + request.getProductId()));
        newCampaign.setProduct(product);

        newCampaign = campaignRepository.save(newCampaign);
        log.info("Campaign created with id {}", newCampaign.getId());
        return newCampaign;
    }

    @Override
//    @Cacheable(value = "campaigns", key = "#pageable.pageNumber + '-' + #pageable.pageSize")
    public Page<Campaign> getCampaigns(Pageable pageable) {
        return campaignRepository.findByIsDeletedFalse(pageable);
    }

    @Override
//    @Cacheable(value = "campaigns", key = "'campaign-' + #id")
    public Campaign getCampaignById(UUID id) {
        return campaignRepository.findById(id)
                .filter(c -> !c.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Campaign not found with id: " + id));
    }

    @Override
    @Transactional
//    @CacheEvict(value = "campaigns", allEntries = true)
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
        return campaign;
    }

    @Override
    @Transactional
//    @CacheEvict(value = "campaigns", key = "'campaign-' + #id")
    public void deleteCampaign(UUID id) {
        Campaign campaign = campaignRepository.findById(id)
                .filter(c -> !c.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Campaign not found with id: " + id));
        campaign.setDeleted(true);
        campaignRepository.save(campaign);
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
    }

    @Override
    public Page<CampaignDTO> getConvertedCampaigns(Pageable pageable) {
        return getCampaigns(pageable).map(this::convertToDto);
    }

    @Override
    public CampaignDTO convertToDto(Campaign campaign) {
        CampaignDTO campaignDTO = new CampaignDTO();
        ProductDTO productDTO = productService.convertToDto(campaign.getProduct());
        campaignDTO.setId(campaign.getId());
        campaignDTO.setName(campaign.getName());
        campaignDTO.setStartDate(campaign.getStartDate());
        campaignDTO.setEndDate(campaign.getEndDate());
        campaignDTO.setMinQuantity(campaign.getMinQuantity());
        campaignDTO.setMaxQuantity(campaign.getMaxQuantity());
        campaignDTO.setTotalAmount(campaign.getTotalAmount());
        campaignDTO.setStatus(campaign.getStatus());
        campaignDTO.setProduct(productDTO);
        campaignDTO.setCreatedAt(campaign.getCreatedAt());
        campaignDTO.setUpdatedAt(campaign.getUpdatedAt());
        return campaignDTO;
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
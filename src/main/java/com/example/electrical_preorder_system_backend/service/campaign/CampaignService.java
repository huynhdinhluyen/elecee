package com.example.electrical_preorder_system_backend.service.campaign;

import com.example.electrical_preorder_system_backend.dto.request.CreateCampaignRequest;
import com.example.electrical_preorder_system_backend.dto.request.UpdateCampaignRequest;
import com.example.electrical_preorder_system_backend.dto.response.CampaignDTO;
import com.example.electrical_preorder_system_backend.dto.response.ProductDTO;
import com.example.electrical_preorder_system_backend.entity.Campaign;
import com.example.electrical_preorder_system_backend.entity.Product;
import com.example.electrical_preorder_system_backend.enums.CampaignStatus;
import com.example.electrical_preorder_system_backend.exception.AlreadyExistsException;
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

    @Override
    @Transactional
    @CacheEvict(value = "campaigns", allEntries = true)
    public Campaign createCampaign(CreateCampaignRequest request) {
        String trimmedName = request.getName().trim();
        if (campaignRepository.existsByName(trimmedName)) {
            throw new AlreadyExistsException("Campaign with name '" + trimmedName + "' already exists.");
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startDate = request.getStartDate();
        LocalDateTime endDate = request.getEndDate();
        if (startDate.isBefore(now)) {
            throw new IllegalArgumentException("Campaign start date cannot be in the past.");
        }
        if (!startDate.isBefore(endDate)) {
            throw new IllegalArgumentException("Campaign start date must be before the end date.");
        }

        Campaign campaign = new Campaign();
        campaign.setName(trimmedName);
        campaign.setStartDate(startDate);
        campaign.setEndDate(endDate);
        campaign.setMinQuantity(request.getMinQuantity());
        campaign.setMaxQuantity(request.getMaxQuantity());
        campaign.setTotalAmount(request.getTotalAmount());
        campaign.setStatus(determineCampaignStatus(startDate, endDate));

        UUID productId = UUID.fromString(request.getProductId().trim());
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + request.getProductId()));
        campaign.setProduct(product);

        campaign = campaignRepository.save(campaign);
        log.info("Campaign created with id {}", campaign.getId());
        return campaign;
    }

    @Override
    @Cacheable(value = "campaigns", key = "#pageable.pageNumber + '-' + #pageable.pageSize")
    public Page<Campaign> getCampaigns(Pageable pageable) {
        return campaignRepository.findByIsDeletedFalse(pageable);
    }

    @Override
    public Campaign getCampaignById(UUID id) {
        return campaignRepository.findById(id)
                .filter(c -> !c.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Campaign not found with id: " + id));
    }

    @Override
    @Transactional
    @CacheEvict(value = "campaigns", allEntries = true)
    public Campaign updateCampaign(UUID id, UpdateCampaignRequest request) {
        Campaign campaign = campaignRepository.findById(id)
                .filter(c -> !c.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Campaign not found with id: " + id));
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startDate = request.getStartDate();
        LocalDateTime endDate = request.getEndDate();
        if (startDate.isBefore(now)) {
            throw new IllegalArgumentException("Campaign start date cannot be in the past.");
        }
        if (!startDate.isBefore(endDate)) {
            throw new IllegalArgumentException("Campaign start date must be before the end date.");
        }

        campaign.setName(request.getName().trim());
        campaign.setStartDate(startDate);
        campaign.setEndDate(endDate);
        if (request.getMinQuantity() != null) {
            campaign.setMinQuantity(request.getMinQuantity());
        }
        if (request.getMaxQuantity() != null) {
            campaign.setMaxQuantity(request.getMaxQuantity());
        }
        if (request.getTotalAmount() != null) {
            campaign.setTotalAmount(request.getTotalAmount());
        }
        campaign.setStatus(determineCampaignStatus(startDate, endDate));

        if (request.getProductId() != null && !request.getProductId().trim().isEmpty()) {
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
    @CacheEvict(value = "campaigns", allEntries = true)
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
}

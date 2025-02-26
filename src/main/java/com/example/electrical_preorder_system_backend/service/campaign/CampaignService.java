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
    public CampaignDTO createCampaign(CreateCampaignRequest request) {
        String trimmedName = request.getName().trim();
        if (campaignRepository.existsByName(trimmedName)) {
            throw new AlreadyExistsException("Campaign with name '" + trimmedName + "' already exists.");
        }
        Campaign campaign = new Campaign();
        campaign.setName(trimmedName);
        campaign.setStartDate(request.getStartDate());
        campaign.setEndDate(request.getEndDate());
        campaign.setMinQuantity(request.getMinQuantity());
        campaign.setMaxQuantity(request.getMaxQuantity());
        campaign.setTotalAmount(request.getTotalAmount());
        campaign.setStatus(request.getStatus() != null ? CampaignStatus.valueOf(request.getStatus()) : CampaignStatus.DRAFT);
        Product product = productRepository.findById(UUID.fromString(request.getProductId()))
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        campaign.setProduct(product);
        campaign = campaignRepository.save(campaign);
        log.info("Campaign created with id {}", campaign.getId());
        return convertToDto(campaign);
    }

    @Override
    @Cacheable(value = "campaigns", key = "#pageable.pageNumber + '-' + #pageable.pageSize")
    public Page<CampaignDTO> getCampaigns(Pageable pageable) {
        Page<Campaign> campaignPage = campaignRepository.findByIsDeletedFalse(pageable);
        return campaignPage.map(this::convertToDto);
    }

    @Override
    public CampaignDTO getCampaignById(UUID id) {
        Campaign campaign = campaignRepository.findById(id)
                .filter(c -> !c.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Campaign not found with id: " + id));
        return convertToDto(campaign);
    }

    @Override
    @Transactional
    @CacheEvict(value = "campaigns", allEntries = true)
    public CampaignDTO updateCampaign(UUID id, UpdateCampaignRequest request) {
        Campaign campaign = campaignRepository.findById(id)
                .filter(c -> !c.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Campaign not found with id: " + id));

        if (request.getName() != null && !request.getName().trim().isEmpty()) {
            String newName = request.getName().trim();
            if (!campaign.getName().equalsIgnoreCase(newName) && campaignRepository.existsByName(newName)) {
                throw new AlreadyExistsException("Campaign with name '" + newName + "' already exists.");
            }
            campaign.setName(newName);
        }
        if (request.getStartDate() != null) {
            campaign.setStartDate(request.getStartDate());
        }
        if (request.getEndDate() != null) {
            campaign.setEndDate(request.getEndDate());
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
        if (request.getStatus() != null) {
            campaign.setStatus(CampaignStatus.valueOf(request.getStatus()));
        }
        if (request.getProductId() != null) {
            Product product = productRepository.findById(UUID.fromString(request.getProductId()))
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
            campaign.setProduct(product);
        }
        campaign = campaignRepository.save(campaign);
        log.info("Campaign updated with id {}", campaign.getId());
        return convertToDto(campaign);
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

    private CampaignDTO convertToDto(Campaign campaign) {
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

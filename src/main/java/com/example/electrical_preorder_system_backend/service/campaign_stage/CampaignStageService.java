package com.example.electrical_preorder_system_backend.service.campaign_stage;

import com.example.electrical_preorder_system_backend.dto.request.CreateCampaignStageRequest;
import com.example.electrical_preorder_system_backend.dto.request.UpdateCampaignStageRequest;
import com.example.electrical_preorder_system_backend.dto.response.CampaignStageDTO;
import com.example.electrical_preorder_system_backend.entity.Campaign;
import com.example.electrical_preorder_system_backend.entity.CampaignStage;
import com.example.electrical_preorder_system_backend.entity.StageHistory;
import com.example.electrical_preorder_system_backend.enums.CampaignStageStatus;
import com.example.electrical_preorder_system_backend.exception.AlreadyExistsException;
import com.example.electrical_preorder_system_backend.exception.ResourceNotFoundException;
import com.example.electrical_preorder_system_backend.repository.CampaignRepository;
import com.example.electrical_preorder_system_backend.repository.CampaignStageRepository;
import com.example.electrical_preorder_system_backend.repository.StageHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CampaignStageService implements ICampaignStageService {

    private final CampaignStageRepository campaignStageRepository;
    private final CampaignRepository campaignRepository;
    private final StageHistoryRepository stageHistoryRepository;

    @Override
    public CampaignStage createCampaignStage(CreateCampaignStageRequest request, UUID campaignId) {
        Campaign campaign = campaignRepository.findById(campaignId)
                .orElseThrow(() -> new ResourceNotFoundException("Campaign not found with id: " + campaignId));
        String stageName = request.getName().trim();
        CampaignStage oldStage = campaignStageRepository.findByName(stageName);
        if (!oldStage.isDeleted()) {
            throw new AlreadyExistsException("Stage with name '" + stageName + "' already exists.");
        }
        if (request.getStartDate().isBefore(campaign.getStartDate())) {
            throw new IllegalArgumentException("Stage start date cannot be before campaign start date (" + campaign.getStartDate() + ").");
        }
        if (request.getEndDate().isAfter(campaign.getEndDate())) {
            throw new IllegalArgumentException("Stage end date cannot be after campaign end date (" + campaign.getEndDate() + ").");
        }
        if (!request.getStartDate().isBefore(request.getEndDate())) {
            throw new IllegalArgumentException("Stage start date must be before end date.");
        }

        CampaignStage newStage = new CampaignStage();
        newStage.setName(stageName);
        newStage.setStartDate(request.getStartDate());
        newStage.setEndDate(request.getEndDate());
        newStage.setTargetQuantity(request.getTargetQuantity());
        newStage.setQuantitySold(0);
        newStage.setStatus(CampaignStageStatus.UPCOMING);
        newStage.setCampaign(campaign);
        newStage = campaignStageRepository.save(newStage);
        log.info("Created campaign stage with name: {}", stageName);
        return newStage;
    }

    @Override
    @Transactional
    public CampaignStage updateCampaignStage(UUID campaignId, UUID stageId, UpdateCampaignStageRequest request) {
        CampaignStage stage = campaignStageRepository.findById(stageId)
                .orElseThrow(() -> new ResourceNotFoundException("Campaign stage not found with id: " + stageId));
        String stageName = request.getName().trim();
        CampaignStage oldStage = campaignStageRepository.findByName(stageName);
        if (!oldStage.isDeleted()) {
            throw new AlreadyExistsException("Stage with name '" + stageName + "' already exists.");
        }
        Campaign campaign = stage.getCampaign();
        if (request.getStartDate().isBefore(campaign.getStartDate())) {
            throw new IllegalArgumentException("Stage start date cannot be before campaign start date (" + campaign.getStartDate() + ").");
        }
        if (request.getEndDate().isAfter(campaign.getEndDate())) {
            throw new IllegalArgumentException("Stage end date cannot be after campaign end date (" + campaign.getEndDate() + ").");
        }

        stage.setName(stageName);
        stage.setStartDate(request.getStartDate());
        stage.setEndDate(request.getEndDate());
        if (request.getTargetQuantity() != null) {
            stage.setTargetQuantity(request.getTargetQuantity());
        }
        updateStageStatus(stage);
        stage = campaignStageRepository.save(stage);
        log.info("Updated campaign stage with id {}", stage.getId());
        return stage;
    }

    @Override
    public List<CampaignStage> getCampaignStagesByCampaignId(UUID campaignId) {
        Campaign campaign = campaignRepository.findById(campaignId)
                .orElseThrow(() -> new ResourceNotFoundException("Campaign not found with id: " + campaignId));
        return campaignStageRepository.findCampaignStagesByCampaign(campaign);
    }

    @Override
    public CampaignStage getCampaignStageById(UUID id) {
        return campaignStageRepository.findById(id)
                .filter(s -> !s.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Campaign stage not found with id: " + id));
    }

    @Override
    @Transactional
    public void deleteCampaignStage(UUID campaignId, UUID stageId) {
        Campaign campaign = campaignRepository.findActiveCampaignById(campaignId);
        if (campaign == null) {
            throw new ResourceNotFoundException("Campaign not found with id: " + campaignId);
        }
        CampaignStage stage = campaignStageRepository.findById(stageId)
                .orElseThrow(() -> new ResourceNotFoundException("Campaign stage not found with id: " + stageId));
        stage.setDeleted(true);
        campaignStageRepository.save(stage);
        log.info("Marked campaign stage with id {} as deleted.", stageId);
    }

    @Override
    public List<CampaignStageDTO> getConvertedCampaignStages(UUID id) {
        return getCampaignStagesByCampaignId(id).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public CampaignStageDTO convertToDto(CampaignStage stage) {
        CampaignStageDTO dto = new CampaignStageDTO();
        dto.setId(stage.getId());
        dto.setName(stage.getName());
        dto.setStartDate(stage.getStartDate());
        dto.setEndDate(stage.getEndDate());
        dto.setQuantitySold(stage.getQuantitySold());
        dto.setTargetQuantity(stage.getTargetQuantity());
        dto.setStatus(stage.getStatus());
        dto.setCreatedAt(stage.getCreatedAt());
        dto.setUpdatedAt(stage.getUpdatedAt());
        return dto;
    }

    @Scheduled(fixedRate = 60000)
    @Transactional
    public void scheduleUpdateStageStatuses() {
        log.info("Running scheduled task to update campaign stage statuses.");
        LocalDateTime now = LocalDateTime.now();
        campaignStageRepository.findByIsDeletedFalse().forEach(stage -> {
            CampaignStageStatus oldStatus = stage.getStatus();
            updateStageStatus(stage);
            CampaignStageStatus currentStatus = stage.getStatus();
            if (currentStatus != oldStatus) {
                StageHistory history = new StageHistory();
                history.setCampaignStage(stage);
                history.setPreStatus(oldStatus);
                history.setCurStatus(currentStatus);
                history.setTransitionTime(now);
                stageHistoryRepository.save(history);
                log.info("Stage {} status changed from {} to {} at {}", stage.getId(), oldStatus, currentStatus, now);
            }
        });
    }

    private void updateStageStatus(CampaignStage stage) {
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(stage.getStartDate())) {
            stage.setStatus(CampaignStageStatus.UPCOMING);
        } else if (now.isAfter(stage.getEndDate())) {
            stage.setStatus(CampaignStageStatus.COMPLETED);
        } else {
            stage.setStatus(CampaignStageStatus.ACTIVE);
        }
    }
}
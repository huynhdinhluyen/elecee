package com.example.electrical_preorder_system_backend.controller;

import com.example.electrical_preorder_system_backend.dto.request.CreateCampaignRequest;
import com.example.electrical_preorder_system_backend.dto.request.CreateCampaignStageRequest;
import com.example.electrical_preorder_system_backend.dto.request.UpdateCampaignRequest;
import com.example.electrical_preorder_system_backend.dto.request.UpdateCampaignStageRequest;
import com.example.electrical_preorder_system_backend.dto.response.ApiResponse;
import com.example.electrical_preorder_system_backend.dto.response.CampaignDTO;
import com.example.electrical_preorder_system_backend.dto.response.StageHistoryDTO;
import com.example.electrical_preorder_system_backend.entity.Campaign;
import com.example.electrical_preorder_system_backend.entity.CampaignStage;
import com.example.electrical_preorder_system_backend.service.campaign.ICampaignService;
import com.example.electrical_preorder_system_backend.service.campaign_stage.ICampaignStageService;
import com.example.electrical_preorder_system_backend.service.stage_history.ICampaignHistoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("${api.prefix}/campaigns")
public class CampaignController {

    private final ICampaignService campaignService;
    private final ICampaignStageService campaignStageService;
    private final ICampaignHistoryService campaignHistoryService;

    @GetMapping
    public ResponseEntity<ApiResponse> getCampaigns(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<CampaignDTO> campaignPage = campaignService.getConvertedCampaigns(pageable);
        return ResponseEntity.ok(new ApiResponse("Campaigns retrieved successfully", campaignPage));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> getCampaignById(@PathVariable UUID id) {
        Campaign campaign = campaignService.getCampaignById(id);
        return ResponseEntity.ok(new ApiResponse("Campaign retrieved successfully", campaignService.convertToDto(campaign)));
    }

    @PostMapping
    @SecurityRequirement(name = "Bearer Authentication")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse> createCampaign(@RequestBody @Valid CreateCampaignRequest request) {
        Campaign campaign = campaignService.createCampaign(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse("Campaign created successfully",
                        campaignService.convertToDto(campaign)));
    }

    @PutMapping("/{id}")
    @SecurityRequirement(name = "Bearer Authentication")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse> updateCampaign(@PathVariable UUID id,
                                                      @RequestBody @Valid UpdateCampaignRequest request) {
        Campaign campaign = campaignService.updateCampaign(id, request);
        return ResponseEntity.ok(new ApiResponse("Campaign updated successfully", campaignService.convertToDto(campaign)));
    }

    @DeleteMapping("/{id}")
    @SecurityRequirement(name = "Bearer Authentication")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse> deleteCampaign(@PathVariable UUID id) {
        campaignService.deleteCampaign(id);
        return ResponseEntity.ok(new ApiResponse("Campaign deleted successfully", id));
    }

    @GetMapping("/{campaignId}/stages")
    public ResponseEntity<ApiResponse> getCampaignStagesByCampaignId(@PathVariable UUID campaignId) {
        return ResponseEntity.ok(new ApiResponse("Campaign stages retrieved successfully",
                campaignStageService.getConvertedCampaignStages(campaignId)));
    }

    @PostMapping("/{campaignId}/stages")
    @SecurityRequirement(name = "Bearer Authentication")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse> createCampaignStage(@PathVariable UUID campaignId,
                                                           @RequestBody @Valid CreateCampaignStageRequest request) {
        CampaignStage stage = campaignStageService.createCampaignStage(request, campaignId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse("Campaign stage created successfully",
                        campaignStageService.convertToDto(stage)));
    }

    @PutMapping("/{campaignId}/stages/{stageId}")
    @SecurityRequirement(name = "Bearer Authentication")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse> updateCampaignStage(@PathVariable UUID campaignId,
                                                           @PathVariable UUID stageId,
                                                           @RequestBody @Valid UpdateCampaignStageRequest request) {
        CampaignStage stage = campaignStageService.updateCampaignStage(campaignId, stageId, request);
        return ResponseEntity.ok(new ApiResponse("Campaign stage updated successfully",
                campaignStageService.convertToDto(stage)));
    }

    @DeleteMapping("/{campaignId}/stages/{stageId}")
    @SecurityRequirement(name = "Bearer Authentication")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse> deleteCampaignStage(@PathVariable UUID campaignId,
                                                           @PathVariable UUID stageId) {
        campaignStageService.deleteCampaignStage(campaignId, stageId);
        return ResponseEntity.ok(new ApiResponse("Campaign deleted successfully", stageId));
    }

    @GetMapping("/{campaignId}/histories")
    public ResponseEntity<ApiResponse> getCampaignHistories(@PathVariable UUID campaignId) {
        List<StageHistoryDTO> histories = campaignHistoryService.getHistoriesByCampaignId(campaignId);
        return ResponseEntity.ok(new ApiResponse("Campaign histories retrieved successfully", histories));
    }
}
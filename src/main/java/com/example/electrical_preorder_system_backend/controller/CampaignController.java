package com.example.electrical_preorder_system_backend.controller;

import com.example.electrical_preorder_system_backend.dto.filter.CampaignFilterCriteria;
import com.example.electrical_preorder_system_backend.dto.request.campaign.CreateCampaignRequest;
import com.example.electrical_preorder_system_backend.dto.request.campaign.UpdateCampaignRequest;
import com.example.electrical_preorder_system_backend.dto.request.campaign_stage.CreateCampaignStageRequest;
import com.example.electrical_preorder_system_backend.dto.request.campaign_stage.UpdateCampaignStageRequest;
import com.example.electrical_preorder_system_backend.dto.response.ApiResponse;
import com.example.electrical_preorder_system_backend.dto.response.campaign.CampaignDTO;
import com.example.electrical_preorder_system_backend.dto.response.stage_history.StageHistoryDTO;
import com.example.electrical_preorder_system_backend.entity.Campaign;
import com.example.electrical_preorder_system_backend.entity.CampaignStage;
import com.example.electrical_preorder_system_backend.enums.CampaignStatus;
import com.example.electrical_preorder_system_backend.service.campaign.ICampaignService;
import com.example.electrical_preorder_system_backend.service.campaign_stage.ICampaignStageService;
import com.example.electrical_preorder_system_backend.service.stage_history.ICampaignHistoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("${api.prefix}/campaigns")
@Tag(name = "Campaign API", description = "APIs for managing pre-order campaigns and stages")
public class CampaignController {

    private final ICampaignService campaignService;
    private final ICampaignStageService campaignStageService;
    private final ICampaignHistoryService campaignHistoryService;

    @Operation(
            summary = "Get all campaigns",
            description = "Returns a paginated list of active (non-deleted) campaigns with search and sorting"
    )
    @GetMapping
    public ResponseEntity<ApiResponse> getCampaigns(
            @Parameter(description = "Filter by campaign name")
            @RequestParam(required = false) String name,

            @Parameter(description = "Filter by campaign status (SCHEDULED, ACTIVE, COMPLETED, CANCELLED)")
            @RequestParam(required = false) CampaignStatus status,

            @Parameter(description = "Filter by product ID")
            @RequestParam(required = false) UUID productId,

            @Parameter(description = "Filter by start date (from)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDateFrom,

            @Parameter(description = "Filter by start date (to)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDateTo,

            @Parameter(description = "Filter by end date (from)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDateFrom,

            @Parameter(description = "Filter by end date (to)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDateTo,

            @Parameter(description = "Page number (zero-based)")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Number of items per page")
            @RequestParam(defaultValue = "10") int size,

            @Parameter(description = "Sort field (name, startDate, endDate, status)")
            @RequestParam(defaultValue = "startDate") String sort,

            @Parameter(description = "Sort direction (asc, desc)")
            @RequestParam(defaultValue = "desc") String direction) {

        Sort.Direction sortDirection = direction.equalsIgnoreCase("asc") ?
                Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));

        CampaignFilterCriteria criteria = new CampaignFilterCriteria();
        criteria.setName(name);
        criteria.setStatus(status);
        criteria.setProductId(productId);
        criteria.setStartDateFrom(startDateFrom);
        criteria.setStartDateTo(startDateTo);
        criteria.setEndDateFrom(endDateFrom);
        criteria.setEndDateTo(endDateTo);

        Page<CampaignDTO> campaignPage = campaignService.getFilteredCampaigns(criteria, pageable);
        return ResponseEntity.ok(new ApiResponse("Campaigns retrieved successfully", campaignPage));
    }

    @Operation(
            summary = "Get campaign by ID",
            description = "Returns a single campaign by its UUID identifier"
    )
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> getCampaignById(
            @Parameter(description = "Campaign UUID", required = true) @PathVariable UUID id
    ) {
        CampaignDTO campaign = campaignService.getCampaignById(id);
        return ResponseEntity.ok(new ApiResponse("Campaign retrieved successfully", campaign));
    }

    @Operation(
            summary = "Create new campaign",
            description = "Create a new pre-order campaign. Requires admin role."
    )
    @SecurityRequirement(name = "Bearer Authentication")
    @PostMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse> createCampaign(@RequestBody @Valid CreateCampaignRequest request) {
        Campaign campaign = campaignService.createCampaign(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse("Campaign created successfully",
                        campaignService.convertToDto(campaign)));
    }

    @Operation(
            summary = "Update a campaign",
            description = "Update an existing campaign by ID. Requires admin role."
    )
    @SecurityRequirement(name = "Bearer Authentication")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse> updateCampaign(
            @Parameter(description = "Campaign UUID", required = true) @PathVariable UUID id,
            @Parameter(description = "Updated campaign data", required = true)
            @RequestBody @Valid UpdateCampaignRequest request
    ) {
        Campaign campaign = campaignService.updateCampaign(id, request);
        return ResponseEntity.ok(new ApiResponse("Campaign updated successfully", campaignService.convertToDto(campaign)));
    }

    @Operation(
            summary = "Delete a campaign",
            description = "Soft delete a campaign by ID (marks as deleted). Requires admin role."
    )
    @SecurityRequirement(name = "Bearer Authentication")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse> deleteCampaign(
            @Parameter(description = "Campaign UUID", required = true) @PathVariable UUID id
    ) {
        campaignService.deleteCampaign(id);
        return ResponseEntity.ok(new ApiResponse("Campaign deleted successfully", id));
    }

    @Operation(
            summary = "Get campaign stages",
            description = "Returns all stages for a specific campaign"
    )
    @GetMapping("/{campaignId}/stages")
    public ResponseEntity<ApiResponse> getCampaignStagesByCampaignId(
            @Parameter(description = "Campaign UUID", required = true) @PathVariable UUID campaignId
    ) {
        return ResponseEntity.ok(new ApiResponse("Campaign stages retrieved successfully",
                campaignStageService.getConvertedCampaignStages(campaignId)));
    }

    @Operation(
            summary = "Create campaign stage",
            description = "Create a new stage for a specific campaign. Requires admin role."
    )
    @SecurityRequirement(name = "Bearer Authentication")
    @PostMapping("/{campaignId}/stages")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse> createCampaignStage(
            @Parameter(description = "Campaign UUID", required = true) @PathVariable UUID campaignId,
            @Parameter(description = "Campaign stage data", required = true)
            @RequestBody @Valid CreateCampaignStageRequest request
    ) {
        CampaignStage stage = campaignStageService.createCampaignStage(request, campaignId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse("Campaign stage created successfully",
                        campaignStageService.convertToDto(stage)));
    }

    @Operation(
            summary = "Update campaign stage",
            description = "Update an existing stage for a specific campaign. Requires admin role."
    )
    @SecurityRequirement(name = "Bearer Authentication")
    @PutMapping("/{campaignId}/stages/{stageId}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse> updateCampaignStage(
            @Parameter(description = "Campaign UUID", required = true) @PathVariable UUID campaignId,
            @Parameter(description = "Stage UUID", required = true) @PathVariable UUID stageId,
            @Parameter(description = "Updated stage data", required = true)
            @RequestBody @Valid UpdateCampaignStageRequest request
    ) {
        CampaignStage stage = campaignStageService.updateCampaignStage(campaignId, stageId, request);
        return ResponseEntity.ok(new ApiResponse("Campaign stage updated successfully",
                campaignStageService.convertToDto(stage)));
    }

    @Operation(
            summary = "Delete campaign stage",
            description = "Soft delete a stage for a specific campaign (marks as deleted). Requires admin role."
    )
    @SecurityRequirement(name = "Bearer Authentication")
    @DeleteMapping("/{campaignId}/stages/{stageId}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse> deleteCampaignStage(
            @Parameter(description = "Campaign UUID", required = true) @PathVariable UUID campaignId,
            @Parameter(description = "Stage UUID", required = true) @PathVariable UUID stageId
    ) {
        campaignStageService.deleteCampaignStage(campaignId, stageId);
        return ResponseEntity.ok(new ApiResponse("Campaign deleted successfully", stageId));
    }

    @Operation(
            summary = "Get campaign stage history",
            description = "Returns the status transition history for all stages of a campaign"
    )
    @GetMapping("/{campaignId}/histories")
    public ResponseEntity<ApiResponse> getCampaignHistories(
            @Parameter(description = "Campaign UUID", required = true) @PathVariable UUID campaignId
    ) {
        List<StageHistoryDTO> histories = campaignHistoryService.getHistoriesByCampaignId(campaignId);
        return ResponseEntity.ok(new ApiResponse("Campaign histories retrieved successfully", histories));
    }
}
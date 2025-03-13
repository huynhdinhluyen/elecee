package com.example.electrical_preorder_system_backend.service.stage_history;

import com.example.electrical_preorder_system_backend.dto.response.stage_history.StageHistoryDTO;
import com.example.electrical_preorder_system_backend.entity.Campaign;
import com.example.electrical_preorder_system_backend.entity.StageHistory;
import com.example.electrical_preorder_system_backend.exception.ResourceNotFoundException;
import com.example.electrical_preorder_system_backend.repository.CampaignRepository;
import com.example.electrical_preorder_system_backend.repository.StageHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CampaignHistoryService implements ICampaignHistoryService {

    private final StageHistoryRepository stageHistoryRepository;
    private final CampaignRepository campaignRepository;

    @Override
    public List<StageHistoryDTO> getHistoriesByCampaignId(UUID campaignId) {
        Campaign campaign = campaignRepository.findActiveCampaignById(campaignId);
        if (campaign == null) {
            throw new ResourceNotFoundException("Campaign not found");
        }
        List<StageHistory> histories = stageHistoryRepository.findHistoriesByCampaignId(campaignId);
        return histories.stream().map(this::convertToDto).collect(Collectors.toList());
    }

    private StageHistoryDTO convertToDto(StageHistory history) {
        StageHistoryDTO dto = new StageHistoryDTO();
        dto.setId(history.getId());
        dto.setPreStatus(history.getPreStatus());
        dto.setCurStatus(history.getCurStatus());
        dto.setTransitionTime(history.getTransitionTime());
        dto.setCampaignStageId(history.getCampaignStage().getId());
        dto.setCreatedAt(history.getCreatedAt());
        dto.setUpdatedAt(history.getUpdatedAt());
        return dto;
    }

}

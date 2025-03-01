package com.example.electrical_preorder_system_backend.service.stage_history;

import com.example.electrical_preorder_system_backend.dto.response.StageHistoryDTO;

import java.util.List;
import java.util.UUID;

public interface ICampaignHistoryService {
    List<StageHistoryDTO> getHistoriesByCampaignId(UUID campaignId);
}

package com.example.electrical_preorder_system_backend.service.campaign;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CampaignStatusScheduler {

    private final ICampaignService campaignService;

    public CampaignStatusScheduler(ICampaignService campaignService) {
        this.campaignService = campaignService;
    }

    // Runs every 60,000 milliseconds (1 minute)
    @Scheduled(fixedRate = 60000)
    @Transactional
    public void updateCampaignStatuses() {
        campaignService.updateCampaignStatuses();
    }
}

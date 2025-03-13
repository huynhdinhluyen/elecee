package com.example.electrical_preorder_system_backend.dto.cache;

import com.example.electrical_preorder_system_backend.dto.response.campaign.CampaignDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CachedCampaignPage implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private List<CampaignDTO> content;
    private int number;
    private int size;
    private long totalElements;
    private int totalPages;

    public static CachedCampaignPage from(Page<CampaignDTO> page) {
        CachedCampaignPage cachedPage = new CachedCampaignPage();
        cachedPage.setContent(page.getContent());
        cachedPage.setNumber(page.getNumber());
        cachedPage.setSize(page.getSize());
        cachedPage.setTotalElements(page.getTotalElements());
        cachedPage.setTotalPages(page.getTotalPages());
        return cachedPage;
    }

    public Page<CampaignDTO> toPage() {
        return new PageImpl<>(content, PageRequest.of(number, size), totalElements);
    }
}

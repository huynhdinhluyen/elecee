package com.example.electrical_preorder_system_backend.entity;

import com.example.electrical_preorder_system_backend.enums.CampaignStageStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "\"stage_history\"", indexes = {
        @Index(name = "idx_stage_history_transition", columnList = "campaign_stage_id, transitionTime")
})
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class StageHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CampaignStageStatus preStatus;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CampaignStageStatus posStatus;

    @Column(nullable = false)
    private LocalDateTime transitionTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "campaign_stage_id", nullable = false)
    private CampaignStage campaignStage;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}

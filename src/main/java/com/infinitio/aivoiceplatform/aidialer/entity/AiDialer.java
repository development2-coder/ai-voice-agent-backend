package com.infinitio.aivoiceplatform.aidialer.entity;

import com.infinitio.aivoiceplatform.aidialer.constant.DialerStatus;
import com.infinitio.aivoiceplatform.campaigncontact.entity.CampaignContact;
import com.infinitio.aivoiceplatform.campaign.entity.Campaign;
import com.infinitio.aivoiceplatform.agent.entity.Agent;
import com.infinitio.aivoiceplatform.flow.entity.Flow;
import com.infinitio.aivoiceplatform.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
        name = "ai_dialers",
        indexes = {
                @Index(
                        name = "ix_ai_dialers_campaign_id",
                        columnList = "campaign_id"
                ),
                @Index(
                        name = "ix_ai_dialers_agent_id",
                        columnList = "agent_id"
                ),
                @Index(
                        name = "ix_ai_dialers_flow_id",
                        columnList = "flow_id"
                ),
                @Index(
                        name = "ix_ai_dialers_status",
                        columnList = "status"
                ),
                @Index(
                        name = "ix_ai_dialers_is_active",
                        columnList = "is_active"
                ),
                @Index(
                        name = "ix_ai_dialers_is_deleted",
                        columnList = "is_deleted"
                )
        }
)
public class AiDialer extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "campaign_id",
            nullable = false
    )
    private Campaign campaign;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "agent_id",
            nullable = false
    )
    private Agent agent;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "flow_id",
            nullable = false
    )
    private Flow flow;

    @Column(
            name = "dialer_name",
            nullable = false,
            length = 150
    )
    private String dialerName;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "status",
            nullable = false,
            length = 30
    )
    private DialerStatus status;

    @Column(
            name = "calls_per_minute",
            nullable = false
    )
    private Integer callsPerMinute;

    @Column(
            name = "max_concurrent_calls",
            nullable = false
    )
    private Integer maxConcurrentCalls;

    @Column(
            name = "max_retry_attempts",
            nullable = false
    )
    private Integer maxRetryAttempts;

    @Column(
            name = "retry_delay_seconds",
            nullable = false
    )
    private Integer retryDelaySeconds;

    @Column(
            name = "scheduled_start_at"
    )
    private LocalDateTime scheduledStartAt;

    @Column(
            name = "scheduled_end_at"
    )
    private LocalDateTime scheduledEndAt;

    @Column(
            name = "started_at"
    )
    private LocalDateTime startedAt;

    @Column(
            name = "paused_at"
    )
    private LocalDateTime pausedAt;

    @Column(
            name = "completed_at"
    )
    private LocalDateTime completedAt;
}
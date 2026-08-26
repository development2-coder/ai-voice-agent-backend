package com.infinitio.aivoiceplatform.aidialer.entity;
import com.infinitio.aivoiceplatform.aidialer.constant.CallAttemptStatus;
import com.infinitio.aivoiceplatform.campaigncontact.entity.CampaignContact;
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
        name = "dialer_calls",
        indexes = {
                @Index(
                        name = "ix_dialer_calls_dialer_id",
                        columnList = "dialer_id"
                ),
                @Index(
                        name = "ix_dialer_calls_campaign_contact_id",
                        columnList = "campaign_contact_id"
                ),
                @Index(
                        name = "ix_dialer_calls_status",
                        columnList = "status"
                ),
                @Index(
                        name = "ix_dialer_calls_exotel_call_id",
                        columnList = "exotel_call_id"
                ),
                @Index(
                        name = "ix_dialer_calls_scheduled_at",
                        columnList = "scheduled_at"
                )
        }
)
public class DialerCall extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "dialer_id",
            nullable = false
    )
    private AiDialer dialer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "campaign_contact_id",
            nullable = false
    )
    private CampaignContact campaignContact;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "status",
            nullable = false,
            length = 30
    )
    private CallAttemptStatus status;

    @Column(
            name = "attempt_number",
            nullable = false
    )
    private Integer attemptNumber;

    @Column(
            name = "exotel_call_id",
            length = 150
    )
    private String exotelCallId;

    @Column(
            name = "phone_number",
            nullable = false,
            length = 30
    )
    private String phoneNumber;

    @Column(
            name = "scheduled_at"
    )
    private LocalDateTime scheduledAt;

    @Column(
            name = "started_at"
    )
    private LocalDateTime startedAt;

    @Column(
            name = "answered_at"
    )
    private LocalDateTime answeredAt;

    @Column(
            name = "ended_at"
    )
    private LocalDateTime endedAt;

    @Column(
            name = "duration_seconds"
    )
    private Integer durationSeconds;

    @Column(
            name = "flow_execution_public_id",
            length = 100
    )
    private String flowExecutionPublicId;

    @Column(
            name = "failure_reason",
            length = 500
    )
    private String failureReason;

    @Column(
            name = "hangup_reason",
            length = 100
    )
    private String hangupReason;
}
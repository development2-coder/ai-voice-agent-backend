package com.infinitio.aivoiceplatform.call.entity;

import com.infinitio.aivoiceplatform.campaigncontact.entity.CampaignContact;
import com.infinitio.aivoiceplatform.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

/**
 * Call Entity.
 *
 * Represents an actual call attempt made
 * for a campaign contact.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
        name = "calls",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_call_provider_call_id",
                        columnNames = "provider_call_id"
                )
        }
)
public class Call extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "campaign_contact_id",
            nullable = false
    )
    private CampaignContact campaignContact;

    @Column(
            name = "provider",
            nullable = false,
            length = 50
    )
    private String provider;

    @Column(
            name = "provider_call_id",
            length = 150
    )
    private String providerCallId;

    @Column(
            name = "from_number",
            nullable = false,
            length = 30
    )
    private String fromNumber;

    @Column(
            name = "to_number",
            nullable = false,
            length = 30
    )
    private String toNumber;

    @Column(
            name = "direction",
            nullable = false,
            length = 20
    )
    private String direction;

    @Column(
            name = "status",
            nullable = false,
            length = 30
    )
    private String status;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "answered_at")
    private LocalDateTime answeredAt;

    @Column(name = "ended_at")
    private LocalDateTime endedAt;

    @Column(name = "duration_seconds")
    private Integer durationSeconds;

    @Column(
            name = "failure_reason",
            length = 500
    )
    private String failureReason;

    @Column(
            name = "recording_url",
            length = 500
    )
    private String recordingUrl;

    @Column(
            name = "description",
            length = 500
    )
    private String description;

    @PrePersist
    public void initializeDefaults() {

        if (status == null || status.isBlank()) {
            status = "INITIATED";
        }
    }
}
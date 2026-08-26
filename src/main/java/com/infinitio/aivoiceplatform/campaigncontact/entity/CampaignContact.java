package com.infinitio.aivoiceplatform.campaigncontact.entity;

import com.infinitio.aivoiceplatform.campaign.entity.Campaign;
import com.infinitio.aivoiceplatform.campaigncontact.constant.CampaignContactConstants;
import com.infinitio.aivoiceplatform.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

/**
 * Campaign Contact Entity.
 *
 * Represents an individual contact belonging
 * to an outbound campaign.
 *
 * Standard contact information is stored in
 * dedicated columns while campaign-specific
 * information is stored inside customData.
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
        name = "campaign_contacts",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_campaign_contact_phone",
                        columnNames = {
                                "campaign_id",
                                "phone_number"
                        }
                )
        }
)
public class CampaignContact extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "campaign_id",
            nullable = false
    )
    private Campaign campaign;

    @Column(
            name = "name",
            length = CampaignContactConstants.NAME_MAX_LENGTH
    )
    private String name;

    @Column(
            name = "phone_number",
            nullable = false,
            length = CampaignContactConstants.PHONE_NUMBER_MAX_LENGTH
    )
    private String phoneNumber;

    @Column(
            name = "external_reference",
            length =
                    CampaignContactConstants
                            .EXTERNAL_REFERENCE_MAX_LENGTH
    )
    private String externalReference;

    @Column(name = "priority")
    private Integer priority;

    @Column(
            name = "status",
            nullable = false,
            length = CampaignContactConstants.STATUS_MAX_LENGTH
    )
    private String status;

    @Column(
            name = "attempt_count",
            nullable = false
    )
    private Integer attemptCount;

    @Column(name = "last_attempt_at")
    private LocalDateTime lastAttemptAt;

    @Column(
            name = "description",
            length =
                    CampaignContactConstants
                            .DESCRIPTION_MAX_LENGTH
    )
    private String description;

    /**
     * Campaign-specific contact data.
     *
     * Example:
     *
     * {
     *     "emi_amount": "12500",
     *     "due_date": "2026-09-05",
     *     "loan_account_no": "LN10001"
     * }
     */
    @Column(
            name = "custom_data",
            columnDefinition = "TEXT"
    )
    private String customData;

    @PrePersist
    public void initializeDefaults() {

        if (status == null
                || status.isBlank()) {

            status =
                    CampaignContactConstants
                            .STATUS_PENDING;
        }

        if (attemptCount == null) {

            attemptCount = 0;
        }

        if (priority == null) {

            priority = 0;
        }
    }
}
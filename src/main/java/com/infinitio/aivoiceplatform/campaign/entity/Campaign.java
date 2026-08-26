package com.infinitio.aivoiceplatform.campaign.entity;

import com.infinitio.aivoiceplatform.agent.entity.Agent;
import com.infinitio.aivoiceplatform.campaign.constant.CampaignConstants;
import com.infinitio.aivoiceplatform.common.entity.BaseEntity;
import com.infinitio.aivoiceplatform.phonenumber.entity.PhoneNumber;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * Campaign Entity.
 *
 * Represents an outbound calling campaign.
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
        name = "campaigns",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_campaign_code",
                        columnNames = "campaign_code"
                )
        }
)
public class Campaign extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "agent_id",
            nullable = false
    )
    private Agent agent;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "phone_number_id",
            nullable = false
    )
    private PhoneNumber phoneNumber;

    @Column(
            name = "campaign_code",
            nullable = false,
            length = CampaignConstants.CAMPAIGN_CODE_MAX_LENGTH
    )
    private String campaignCode;

    @Column(
            name = "campaign_name",
            nullable = false,
            length = CampaignConstants.CAMPAIGN_NAME_MAX_LENGTH
    )
    private String campaignName;

    @Column(
            name = "campaign_type",
            nullable = false,
            length = CampaignConstants.CAMPAIGN_TYPE_MAX_LENGTH
    )
    private String campaignType;

    @Column(
            name = "status",
            nullable = false,
            length = CampaignConstants.STATUS_MAX_LENGTH
    )
    private String status;

    @Column(
            name = "description",
            length = CampaignConstants.DESCRIPTION_MAX_LENGTH
    )
    private String description;

    @Column(
            name = "custom_data",
            columnDefinition = "JSON"
    )
    private String customData;

    @PrePersist
    public void initializeDefaults() {

        if (status == null || status.isBlank()) {
            status = CampaignConstants.STATUS_DRAFT;
        }
    }
}
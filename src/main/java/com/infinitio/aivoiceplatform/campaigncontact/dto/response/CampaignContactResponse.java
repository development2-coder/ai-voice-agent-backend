package com.infinitio.aivoiceplatform.campaigncontact.dto.response;

import lombok.*;

import java.time.LocalDateTime;

/**
 * Campaign Contact Response.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CampaignContactResponse {

    private String publicId;

    private String campaignPublicId;

    private String name;

    private String phoneNumber;

    private String externalReference;

    private Integer priority;

    private String status;

    private Integer attemptCount;

    private LocalDateTime lastAttemptAt;

    private String description;

    private String customData;

    private Integer isActive;
}
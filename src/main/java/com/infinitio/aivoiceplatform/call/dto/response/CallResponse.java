package com.infinitio.aivoiceplatform.call.dto.response;

import lombok.*;

import java.time.LocalDateTime;

/**
 * Call Response.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CallResponse {

    private String publicId;

    private String campaignContactPublicId;

    private String provider;

    private String providerCallId;

    private String fromNumber;

    private String toNumber;

    private String direction;

    private String status;

    private LocalDateTime startedAt;

    private LocalDateTime answeredAt;

    private LocalDateTime endedAt;

    private Integer durationSeconds;

    private String failureReason;

    private String recordingUrl;

    private String description;

    private Integer isActive;
}
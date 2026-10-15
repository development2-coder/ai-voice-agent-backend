package com.infinitio.aivoiceplatform.call.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Response DTO for call details.
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

    /**
     * Call duration in seconds.
     */
    private Integer durationSeconds;

    /**
     * Formatted call duration, HH:mm:ss.
     */
    private String duration;

    private String failureReason;

    private String recordingUrl;

    private String description;

    private Integer isActive;
}
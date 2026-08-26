package com.infinitio.aivoiceplatform.aidialer.dto.response;

import com.infinitio.aivoiceplatform.aidialer.constant.DialerStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DialerResponse {

    private String publicId;

    private String dialerName;

    private String campaignPublicId;

    private String campaignName;

    private String agentPublicId;

    private String agentName;

    private String flowPublicId;

    private String flowName;

    private DialerStatus status;

    private Integer callsPerMinute;

    private Integer maxConcurrentCalls;

    private Integer maxRetryAttempts;

    private Integer retryDelaySeconds;

    private LocalDateTime scheduledStartAt;

    private LocalDateTime scheduledEndAt;

    private LocalDateTime startedAt;

    private LocalDateTime pausedAt;

    private LocalDateTime completedAt;

    private Integer isActive;

    private Integer isDeleted;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
package com.infinitio.aivoiceplatform.aidialer.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateAiDialerRequest {

    private String dialerName;

    private String campaignPublicId;

    private String agentPublicId;

    private String flowPublicId;

    @Min(
            value = 1,
            message = "Calls per minute must be at least 1"
    )
    @Max(
            value = 1000,
            message = "Calls per minute cannot exceed 1000"
    )
    private Integer callsPerMinute;

    @Min(
            value = 1,
            message = "Maximum concurrent calls must be at least 1"
    )
    @Max(
            value = 1000,
            message = "Maximum concurrent calls cannot exceed 1000"
    )
    private Integer maxConcurrentCalls;

    @Min(
            value = 0,
            message = "Maximum retry attempts cannot be negative"
    )
    @Max(
            value = 20,
            message = "Maximum retry attempts cannot exceed 20"
    )
    private Integer maxRetryAttempts;

    @Min(
            value = 0,
            message = "Retry delay cannot be negative"
    )
    private Integer retryDelaySeconds;

    private LocalDateTime scheduledStartAt;

    private LocalDateTime scheduledEndAt;
}
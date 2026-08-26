package com.infinitio.aivoiceplatform.aidialer.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateAiDialerRequest {

    @NotBlank(message = "Dialer name is required")
    private String dialerName;

    @NotBlank(message = "Campaign public ID is required")
    private String campaignPublicId;

    @NotBlank(message = "Agent public ID is required")
    private String agentPublicId;

    @NotBlank(message = "Flow public ID is required")
    private String flowPublicId;

    @NotNull(message = "Calls per minute is required")
    @Min(
            value = 1,
            message = "Calls per minute must be at least 1"
    )
    @Max(
            value = 1000,
            message = "Calls per minute cannot exceed 1000"
    )
    private Integer callsPerMinute;

    @NotNull(message = "Maximum concurrent calls is required")
    @Min(
            value = 1,
            message = "Maximum concurrent calls must be at least 1"
    )
    @Max(
            value = 1000,
            message = "Maximum concurrent calls cannot exceed 1000"
    )
    private Integer maxConcurrentCalls;

    @NotNull(message = "Maximum retry attempts is required")
    @Min(
            value = 0,
            message = "Maximum retry attempts cannot be negative"
    )
    @Max(
            value = 20,
            message = "Maximum retry attempts cannot exceed 20"
    )
    private Integer maxRetryAttempts;

    @NotNull(message = "Retry delay is required")
    @Min(
            value = 0,
            message = "Retry delay cannot be negative"
    )
    private Integer retryDelaySeconds;

    private LocalDateTime scheduledStartAt;

    private LocalDateTime scheduledEndAt;
}
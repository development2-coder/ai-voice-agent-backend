package com.infinitio.aivoiceplatform.aidialer.dto.response;

import com.infinitio.aivoiceplatform.aidialer.constant.CallAttemptStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DialerCallResponse {

    private String publicId;

    private String dialerPublicId;

    private String campaignContactPublicId;

    private CallAttemptStatus status;

    private Integer attemptNumber;

    private String phoneNumber;

    private String exotelCallId;

    private LocalDateTime scheduledAt;

    private LocalDateTime startedAt;

    private LocalDateTime answeredAt;

    private LocalDateTime endedAt;

    private Integer durationSeconds;

    private String flowExecutionPublicId;

    private String failureReason;

    private String hangupReason;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
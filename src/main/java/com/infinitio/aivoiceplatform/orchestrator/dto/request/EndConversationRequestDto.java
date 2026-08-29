package com.infinitio.aivoiceplatform.orchestrator.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Request DTO used to terminate a conversation.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EndConversationRequestDto {

    /**
     * Unique public identifier of the call.
     */
    @NotBlank
    private String callId;

    /**
     * Reason for conversation termination.
     */
    private String reason;
}
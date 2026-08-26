package com.infinitio.aivoiceplatform.llm.dto.runtime;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Represents a runtime request for LLM text generation.
 *
 * <p>
 * This DTO is provider-independent. Provider-specific request
 * parameters are handled by the corresponding provider implementation.
 * </p>
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LlmGenerationRequestDto {

    /**
     * Unique identifier of the active call.
     */
    private String callId;

    /**
     * Requested language of the conversation.
     */
    private String language;

    /**
     * Conversation messages supplied to the LLM.
     */
    private List<LlmMessageDto> messages;

    /**
     * Indicates whether the caller expects a final response.
     *
     * <p>
     * This can be used by the orchestrator to distinguish
     * intermediate processing from a final conversational response.
     * </p>
     */
    private boolean finalResponse;
}
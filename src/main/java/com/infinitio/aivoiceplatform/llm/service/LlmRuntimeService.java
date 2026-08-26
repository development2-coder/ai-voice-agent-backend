package com.infinitio.aivoiceplatform.llm.service;

import com.infinitio.aivoiceplatform.llm.dto.runtime.LlmGenerationRequestDto;
import com.infinitio.aivoiceplatform.llm.dto.runtime.LlmGenerationResponseDto;

/**
 * Defines runtime business operations for LLM generation.
 *
 * <p>
 * The runtime service is responsible for validating and coordinating
 * LLM generation requests before delegating provider-specific execution
 * to the configured LLM provider.
 * </p>
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
public interface LlmRuntimeService {

    /**
     * Generates an LLM response for the supplied conversation.
     *
     * @param request LLM generation request
     * @return generated LLM response
     */
    LlmGenerationResponseDto generate(
            LlmGenerationRequestDto request);
}
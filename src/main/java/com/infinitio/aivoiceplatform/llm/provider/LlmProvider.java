package com.infinitio.aivoiceplatform.llm.provider;

import com.infinitio.aivoiceplatform.llm.dto.runtime.LlmGenerationRequestDto;
import com.infinitio.aivoiceplatform.llm.dto.runtime.LlmGenerationResponseDto;

/**
 * Defines the contract for runtime LLM providers.
 *
 * <p>
 * Provider-specific implementations must implement this interface so that
 * the LLM runtime layer remains independent of a particular LLM provider.
 * </p>
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
public interface LlmProvider {

    /**
     * Returns the unique provider code.
     *
     * @return provider code
     */
    String getProviderCode();

    /**
     * Generates a response for the supplied conversation.
     *
     * @param request LLM generation request
     * @return LLM generation response
     */
    LlmGenerationResponseDto generate(
            LlmGenerationRequestDto request);

    /**
     * Checks whether the provider is currently available.
     *
     * @return true when the provider is available
     */
    boolean isAvailable();
}
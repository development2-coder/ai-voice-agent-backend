package com.infinitio.aivoiceplatform.llm.dto.runtime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Represents a single message used during LLM runtime generation.
 *
 * <p>
 * The message follows the role/content structure required by the
 * configured LLM provider.
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
public class LlmMessageDto {

    /**
     * Message role.
     *
     * <p>
     * Supported roles are provider-dependent and may include
     * system, user, and assistant.
     * </p>
     */
    private String role;

    /**
     * Message content.
     */
    private String content;
}
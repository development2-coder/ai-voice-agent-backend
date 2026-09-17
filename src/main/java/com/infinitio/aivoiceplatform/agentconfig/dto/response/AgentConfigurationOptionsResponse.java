package com.infinitio.aivoiceplatform.agentconfig.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Response containing selectable Agent Configuration options.
 *
 * <p>
 * The response provides the values required by the Agent creation
 * and configuration UI for language, speaker and greeting selection.
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
public class AgentConfigurationOptionsResponse {

    /**
     * Languages supported by the configured voice runtime.
     */
    private List<ConfigurationOptionResponse> languages;

    /**
     * Speakers supported by the configured TTS runtime.
     */
    private List<ConfigurationOptionResponse> speakers;

    /**
     * Greeting messages configured for the Agent UI.
     */
    private List<ConfigurationOptionResponse> greetings;
}
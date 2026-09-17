package com.infinitio.aivoiceplatform.agentconfig.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Represents one selectable Agent Configuration option.
 *
 * <p>
 * This DTO is used by configuration APIs to expose a frontend-friendly
 * value and display label without exposing provider-specific runtime
 * configuration details.
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
public class ConfigurationOptionResponse {

    /**
     * Internal value submitted by the frontend.
     */
    private String value;

    /**
     * Human-readable label displayed by the frontend.
     */
    private String label;
}
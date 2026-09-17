package com.infinitio.aivoiceplatform.agentconfig.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * Contains configurable Agent Configuration UI options.
 *
 * <p>
 * Greeting options are loaded from external application configuration
 * rather than being hardcoded in Java source code.
 * </p>
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Getter
@Setter
@ConfigurationProperties(
        prefix = "agent.configuration.options"
)
public class AgentConfigurationOptionsProperties {

    /**
     * Configured greeting messages available for selection.
     */
    private List<String> greetings =
            new ArrayList<>();
}
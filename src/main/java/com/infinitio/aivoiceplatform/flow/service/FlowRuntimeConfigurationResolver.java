package com.infinitio.aivoiceplatform.flow.service;

import java.util.Map;

/**
 * Resolves runtime configuration values for Flow nodes.
 *
 * <p>
 * Flow node configuration has higher priority than the Agent
 * default configuration. When a node does not provide an override,
 * the value is resolved from the Agent runtime configuration.
 * </p>
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
public interface FlowRuntimeConfigurationResolver {

    /**
     * Resolves a configuration value.
     *
     * @param nodeConfiguration node-specific configuration
     * @param runtimeContext current Flow runtime context
     * @param configurationName configuration property name
     * @param defaultValue fallback value
     * @return resolved configuration value
     */
    Object resolve(
            Map<String, Object> nodeConfiguration,
            Map<String, Object> runtimeContext,
            String configurationName,
            Object defaultValue
    );

    /**
     * Resolves the complete Agent runtime configuration.
     *
     * @param runtimeContext current Flow runtime context
     * @return Agent runtime configuration or empty map
     */
    Map<String, Object> getAgentRuntimeConfiguration(
            Map<String, Object> runtimeContext
    );
}
package com.infinitio.aivoiceplatform.flow.service.impl;

import com.infinitio.aivoiceplatform.flow.service.FlowRuntimeConfigurationResolver;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Map;

/**
 * Default implementation of Flow runtime configuration resolution.
 *
 * <p>
 * The resolver follows the configuration priority:
 * </p>
 *
 * <pre>
 * Flow Node Override
 *        ↓
 * Agent Runtime Configuration
 *        ↓
 * Default Value
 * </pre>
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Slf4j
@Service
public class FlowRuntimeConfigurationResolverImpl
        implements FlowRuntimeConfigurationResolver {

    /**
     * Runtime context key containing Agent configuration.
     */
    private static final String RUNTIME_CONFIGURATION =
            "runtimeConfiguration";

    /**
     * {@inheritDoc}
     */
    @Override
    public Object resolve(
            Map<String, Object> nodeConfiguration,
            Map<String, Object> runtimeContext,
            String configurationName,
            Object defaultValue) {

        /*
         * 1. Node-level configuration has the highest priority.
         */
        if (nodeConfiguration != null
                && nodeConfiguration.containsKey(
                configurationName)) {

            Object nodeValue =
                    nodeConfiguration.get(
                            configurationName
                    );

            if (hasValue(
                    nodeValue
            )) {

                log.debug(
                        "Using Flow node configuration. " +
                                "configurationName={}, value={}",
                        configurationName,
                        nodeValue
                );

                return nodeValue;
            }
        }

        /*
         * 2. Resolve from Agent runtime configuration.
         */
        Map<String, Object> agentConfiguration =
                getAgentRuntimeConfiguration(
                        runtimeContext
                );

        if (agentConfiguration.containsKey(
                configurationName
        )) {

            Object agentValue =
                    agentConfiguration.get(
                            configurationName
                    );

            if (hasValue(
                    agentValue
            )) {

                log.debug(
                        "Using Agent runtime configuration. " +
                                "configurationName={}, value={}",
                        configurationName,
                        agentValue
                );

                return agentValue;
            }
        }

        /*
         * 3. Use a fallback only when neither node nor Agent
         * configuration contains a usable value.
         */
        log.debug(
                "Using default Flow runtime configuration. " +
                        "configurationName={}, value={}",
                configurationName,
                defaultValue
        );

        return defaultValue;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> getAgentRuntimeConfiguration(
            Map<String, Object> runtimeContext) {

        if (runtimeContext == null) {

            return Collections.emptyMap();
        }

        Object configuration =
                runtimeContext.get(
                        RUNTIME_CONFIGURATION
                );

        if (!(configuration instanceof Map)) {

            log.debug(
                    "Agent runtime configuration is not available " +
                            "in Flow context."
            );

            return Collections.emptyMap();
        }

        return (Map<String, Object>) configuration;
    }

    /**
     * Determines whether a configuration value is usable.
     *
     * @param value configuration value
     * @return true when a usable value exists
     */
    private boolean hasValue(
            Object value) {

        if (value == null) {
            return false;
        }

        if (value instanceof String) {

            return !((String) value).isBlank();
        }

        return true;
    }
}
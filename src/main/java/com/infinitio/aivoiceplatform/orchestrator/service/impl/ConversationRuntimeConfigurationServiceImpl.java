package com.infinitio.aivoiceplatform.orchestrator.service.impl;

import com.infinitio.aivoiceplatform.agent.entity.Agent;
import com.infinitio.aivoiceplatform.agent.validator.AgentValidator;
import com.infinitio.aivoiceplatform.agentconfig.constant.AgentConfigConstants;
import com.infinitio.aivoiceplatform.agentconfig.constant.AgentConfigMessages;
import com.infinitio.aivoiceplatform.agentconfig.entity.AgentConfig;
import com.infinitio.aivoiceplatform.agentconfig.repository.AgentConfigRepository;
import com.infinitio.aivoiceplatform.exception.ResourceNotFoundException;
import com.infinitio.aivoiceplatform.flow.entity.Flow;
import com.infinitio.aivoiceplatform.flow.validator.FlowValidator;
import com.infinitio.aivoiceplatform.organization.tenant.entity.Tenant;
import com.infinitio.aivoiceplatform.organization.tenant.validator.TenantValidator;
import com.infinitio.aivoiceplatform.orchestrator.constant.ConversationOrchestratorMessages;
import com.infinitio.aivoiceplatform.orchestrator.dto.response.ConversationRuntimeConfigurationResponseDto;
import com.infinitio.aivoiceplatform.orchestrator.service.ConversationRuntimeConfigurationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Resolves and validates the runtime configuration required
 * by the Conversation Orchestrator.
 *
 * <p>
 * Runtime configuration is resolved from the persisted Tenant,
 * Agent, Agent Configuration and Flow entities.
 * </p>
 *
 * <p>
 * The telephony layer does not decide the AI provider, model,
 * voice, system prompt or greeting configuration. Those values
 * are resolved from the persisted Agent and Agent Configuration.
 * </p>
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ConversationRuntimeConfigurationServiceImpl
        implements ConversationRuntimeConfigurationService {

    private static final Integer NOT_DELETED = 0;

    private final TenantValidator tenantValidator;

    private final AgentValidator agentValidator;

    private final FlowValidator flowValidator;

    private final AgentConfigRepository agentConfigRepository;

    /**
     * {@inheritDoc}
     */
    @Override
    public ConversationRuntimeConfigurationResponseDto
    resolveRuntimeConfiguration(
            String tenantId,
            String agentId,
            Integer agentVersion,
            String flowPublicId) {

        log.info(
                "Resolving conversation runtime configuration. " +
                        "tenantId={}, agentId={}, agentVersion={}, " +
                        "flowPublicId={}",
                tenantId,
                agentId,
                agentVersion,
                flowPublicId
        );

        validateRequiredIdentifiers(
                tenantId,
                agentId,
                flowPublicId
        );

        Tenant tenant =
                tenantValidator.validateAndGet(
                        tenantId
                );

        Agent agent =
                agentValidator.validateAndGet(
                        agentId
                );

        validateTenantAgentRelationship(
                tenant,
                agent
        );

        Flow flow =
                flowValidator.validateAndGet(
                        flowPublicId
                );

        validateAgentFlowRelationship(
                agent,
                flow
        );

        validateAgentActiveState(
                agent
        );

        validateFlowActiveState(
                flow
        );

        AgentConfig agentConfig =
                agentConfigRepository
                        .findByAgentIdAndIsDeleted(
                                agent.getId(),
                                NOT_DELETED
                        )
                        .orElseThrow(() -> {

                            log.warn(
                                    "Agent configuration not found. " +
                                            "agentPublicId={}",
                                    agent.getPublicId()
                            );

                            return new ResourceNotFoundException(
                                    AgentConfigMessages.NOT_FOUND
                            );
                        });

        validateAgentConfiguration(
                agentConfig,
                agent
        );

        Integer resolvedAgentVersion =
                resolveAgentVersion(
                        agentVersion,
                        flow
                );

        String welcomeMessage =
                resolveWelcomeMessage(
                        agent
                );

        String greetingMessage =
                resolveGreetingMessage(
                        agentConfig
                );

        ConversationRuntimeConfigurationResponseDto
                response =
                ConversationRuntimeConfigurationResponseDto
                        .builder()
                        .tenantId(
                                tenant.getPublicId()
                        )
                        .agentId(
                                agent.getPublicId()
                        )
                        .agentVersion(
                                resolvedAgentVersion
                        )
                        .flowPublicId(
                                flow.getPublicId()
                        )
                        .language(
                                resolveEffectiveLanguage(
                                        agent,
                                        agentConfig
                                )
                        )
                        .welcomeMessage(
                                welcomeMessage
                        )
                        .greetingMessage(
                                greetingMessage
                        )
                        .sttProvider(
                                agentConfig.getSttProvider()
                        )
                        .sttModel(
                                agentConfig.getSttModel()
                        )
                        .llmProvider(
                                agentConfig.getLlmProvider()
                        )
                        .llmModel(
                                agentConfig.getLlmModel()
                        )
                        .ttsProvider(
                                agentConfig.getTtsProvider()
                        )
                        .ttsModel(
                                agentConfig.getTtsModel()
                        )
                        .voice(
                                agentConfig.getVoice()
                        )
                        .systemPrompt(
                                agentConfig.getSystemPrompt()
                        )
                        .temperature(
                                agentConfig.getTemperature()
                        )
                        .maxTokens(
                                agentConfig.getMaxTokens()
                        )
                        .build();

        log.info(
                "Conversation runtime configuration resolved successfully. " +
                        "tenantId={}, agentId={}, agentVersion={}, " +
                        "flowPublicId={}, welcomeConfigured={}, " +
                        "greetingConfigured={}, sttProvider={}, " +
                        "llmProvider={}, ttsProvider={}",
                response.getTenantId(),
                response.getAgentId(),
                response.getAgentVersion(),
                response.getFlowPublicId(),
                isConfigured(
                        response.getWelcomeMessage()
                ),
                isConfigured(
                        response.getGreetingMessage()
                ),
                response.getSttProvider(),
                response.getLlmProvider(),
                response.getTtsProvider()
        );

        return response;
    }

    // =========================================================
    // REQUIRED IDENTIFIERS
    // =========================================================

    /**
     * Validates mandatory runtime identifiers.
     */
    private void validateRequiredIdentifiers(
            String tenantId,
            String agentId,
            String flowPublicId) {

        if (tenantId == null
                || tenantId.isBlank()) {

            log.warn(
                    "Conversation runtime tenant ID is missing."
            );

            throw new IllegalArgumentException(
                    ConversationOrchestratorMessages
                            .TENANT_ID_REQUIRED
            );
        }

        if (agentId == null
                || agentId.isBlank()) {

            log.warn(
                    "Conversation runtime agent ID is missing."
            );

            throw new IllegalArgumentException(
                    ConversationOrchestratorMessages
                            .AGENT_ID_REQUIRED
            );
        }

        if (flowPublicId == null
                || flowPublicId.isBlank()) {

            log.warn(
                    "Conversation runtime Flow public ID is missing."
            );

            throw new IllegalArgumentException(
                    ConversationOrchestratorMessages
                            .FLOW_PUBLIC_ID_REQUIRED
            );
        }
    }

    // =========================================================
    // TENANT → AGENT
    // =========================================================

    /**
     * Ensures that the Agent belongs to the requested Tenant.
     */
    private void validateTenantAgentRelationship(
            Tenant tenant,
            Agent agent) {

        if (tenant == null) {

            log.error(
                    "Tenant validation returned null."
            );

            throw new IllegalStateException(
                    ConversationOrchestratorMessages
                            .CONVERSATION_PROCESSING_FAILED
            );
        }

        if (agent == null) {

            log.error(
                    "Agent validation returned null."
            );

            throw new IllegalStateException(
                    ConversationOrchestratorMessages
                            .CONVERSATION_PROCESSING_FAILED
            );
        }

        if (agent.getTenant() == null) {

            log.error(
                    "Agent has no Tenant association. " +
                            "agentPublicId={}",
                    agent.getPublicId()
            );

            throw new IllegalStateException(
                    ConversationOrchestratorMessages
                            .CONVERSATION_PROCESSING_FAILED
            );
        }

        if (!tenant.getId()
                .equals(
                        agent.getTenant().getId()
                )) {

            log.warn(
                    "Tenant-Agent isolation validation failed. " +
                            "tenantPublicId={}, agentPublicId={}, " +
                            "agentTenantPublicId={}",
                    tenant.getPublicId(),
                    agent.getPublicId(),
                    agent.getTenant().getPublicId()
            );

            throw new IllegalArgumentException(
                    ConversationOrchestratorMessages
                            .CONVERSATION_PROCESSING_FAILED
            );
        }

        log.debug(
                "Tenant-Agent relationship validated. " +
                        "tenantPublicId={}, agentPublicId={}",
                tenant.getPublicId(),
                agent.getPublicId()
        );
    }

    // =========================================================
    // AGENT → FLOW
    // =========================================================

    /**
     * Ensures that the Flow belongs to the requested Agent.
     */
    private void validateAgentFlowRelationship(
            Agent agent,
            Flow flow) {

        if (flow == null) {

            log.error(
                    "Flow validation returned null."
            );

            throw new IllegalStateException(
                    ConversationOrchestratorMessages
                            .FLOW_EXECUTION_RESULT_EMPTY
            );
        }

        if (flow.getAgent() == null) {

            log.error(
                    "Flow has no Agent association. " +
                            "flowPublicId={}",
                    flow.getPublicId()
            );

            throw new IllegalStateException(
                    ConversationOrchestratorMessages
                            .FLOW_EXECUTION_FAILED
            );
        }

        if (!agent.getId()
                .equals(
                        flow.getAgent().getId()
                )) {

            log.warn(
                    "Agent-Flow isolation validation failed. " +
                            "agentPublicId={}, flowPublicId={}, " +
                            "flowAgentPublicId={}",
                    agent.getPublicId(),
                    flow.getPublicId(),
                    flow.getAgent().getPublicId()
            );

            throw new IllegalArgumentException(
                    ConversationOrchestratorMessages
                            .FLOW_EXECUTION_FAILED
            );
        }

        log.debug(
                "Agent-Flow relationship validated. " +
                        "agentPublicId={}, flowPublicId={}",
                agent.getPublicId(),
                flow.getPublicId()
        );
    }

    // =========================================================
    // AGENT STATE
    // =========================================================

    /**
     * Ensures the Agent is active.
     */
    private void validateAgentActiveState(
            Agent agent) {

        if (agent.getIsActive() == null
                || agent.getIsActive() != 1) {

            log.warn(
                    "Agent is inactive. agentPublicId={}",
                    agent.getPublicId()
            );

            throw new IllegalStateException(
                    ConversationOrchestratorMessages
                            .CONVERSATION_PROCESSING_FAILED
            );
        }

        log.debug(
                "Agent active-state validation passed. " +
                        "agentPublicId={}",
                agent.getPublicId()
        );
    }

    // =========================================================
    // FLOW STATE
    // =========================================================

    /**
     * Ensures the Flow is active.
     */
    private void validateFlowActiveState(
            Flow flow) {

        if (flow.getIsActive() == null
                || flow.getIsActive() != 1) {

            log.warn(
                    "Flow is inactive. flowPublicId={}",
                    flow.getPublicId()
            );

            throw new IllegalStateException(
                    ConversationOrchestratorMessages
                            .FLOW_EXECUTION_FAILED
            );
        }

        log.debug(
                "Flow active-state validation passed. " +
                        "flowPublicId={}",
                flow.getPublicId()
        );
    }

    // =========================================================
    // AGENT CONFIGURATION
    // =========================================================

    /**
     * Validates the Agent Configuration and its ownership.
     */
    private void validateAgentConfiguration(
            AgentConfig agentConfig,
            Agent agent) {

        if (agentConfig.getAgent() == null) {

            log.error(
                    "Agent configuration has no Agent association. " +
                            "agentConfigPublicId={}",
                    agentConfig.getPublicId()
            );

            throw new IllegalStateException(
                    AgentConfigMessages.NOT_FOUND
            );
        }

        if (!agent.getId()
                .equals(
                        agentConfig.getAgent().getId()
                )) {

            log.warn(
                    "Agent configuration ownership validation failed. " +
                            "agentPublicId={}, agentConfigPublicId={}",
                    agent.getPublicId(),
                    agentConfig.getPublicId()
            );

            throw new IllegalArgumentException(
                    AgentConfigMessages.NOT_FOUND
            );
        }

        if (agentConfig.getIsActive() == null
                || agentConfig.getIsActive() != 1) {

            log.warn(
                    "Agent configuration is inactive. " +
                            "agentConfigPublicId={}",
                    agentConfig.getPublicId()
            );

            throw new IllegalStateException(
                    AgentConfigConstants.STATUS_INACTIVE
            );
        }

        if (!AgentConfigConstants.STATUS_ACTIVE
                .equalsIgnoreCase(
                        agentConfig.getStatus()
                )) {

            log.warn(
                    "Agent configuration is not active. " +
                            "agentConfigPublicId={}, status={}",
                    agentConfig.getPublicId(),
                    agentConfig.getStatus()
            );

            throw new IllegalStateException(
                    AgentConfigConstants.STATUS_ACTIVE
            );
        }

        log.debug(
                "Agent configuration validated. " +
                        "agentPublicId={}, agentConfigPublicId={}",
                agent.getPublicId(),
                agentConfig.getPublicId()
        );
    }

    // =========================================================
    // GREETING CONFIGURATION
    // =========================================================

    /**
     * Resolves the welcome message configured directly on the Agent.
     *
     * <p>
     * The Agent welcome message is intentionally kept separate
     * from the Flow greeting. The conversation runtime uses this
     * value as the first agent-level message before Flow execution.
     * </p>
     *
     * @param agent active Agent
     * @return configured Agent welcome message, or {@code null}
     */
    private String resolveWelcomeMessage(
            Agent agent) {

        if (agent == null
                || agent.getWelcomeMessage() == null
                || agent.getWelcomeMessage().isBlank()) {

            return null;
        }

        return agent.getWelcomeMessage().trim();
    }

    /**
     * Resolves the greeting message configured in Agent Configuration.
     *
     * <p>
     * This message is kept separate from the Agent welcome message
     * because it belongs to the Agent Configuration layer.
     * </p>
     *
     * @param agentConfig active Agent Configuration
     * @return configured greeting message, or {@code null}
     */
    private String resolveGreetingMessage(
            AgentConfig agentConfig) {

        if (agentConfig == null
                || agentConfig.getGreetingMessage() == null
                || agentConfig.getGreetingMessage().isBlank()) {

            return null;
        }

        return agentConfig.getGreetingMessage().trim();
    }

    /**
     * Checks whether a runtime text value is configured.
     *
     * @param value configuration value
     * @return {@code true} when a non-blank value exists
     */
    private boolean isConfigured(
            String value) {

        return value != null
                && !value.isBlank();
    }

    // =========================================================
    // AGENT VERSION
    // =========================================================

    /**
     * Resolves the runtime Agent version.
     *
     * <p>
     * The current source does not contain a separate persisted
     * AgentVersion entity. Therefore an explicitly supplied
     * version is validated, while the Flow version is used when
     * no version is supplied.
     * </p>
     */
    private Integer resolveAgentVersion(
            Integer requestedVersion,
            Flow flow) {

        if (requestedVersion != null) {

            if (requestedVersion < 1) {

                log.warn(
                        "Invalid Agent version supplied. " +
                                "agentVersion={}",
                        requestedVersion
                );

                throw new IllegalArgumentException(
                        "Agent version must be greater than zero."
                );
            }

            return requestedVersion;
        }

        Integer flowVersion =
                flow.getVersion();

        if (flowVersion == null
                || flowVersion < 1) {

            log.error(
                    "Flow has an invalid version. " +
                            "flowPublicId={}, version={}",
                    flow.getPublicId(),
                    flowVersion
            );

            throw new IllegalStateException(
                    ConversationOrchestratorMessages
                            .FLOW_EXECUTION_FAILED
            );
        }

        log.debug(
                "Using Flow version as runtime Agent version. " +
                        "flowPublicId={}, version={}",
                flow.getPublicId(),
                flowVersion
        );

        return flowVersion;
    }

    /**
     * Resolves the effective language for the conversation runtime.
     *
     * <p>
     * The language configured directly on the Agent is treated as
     * the primary language. Agent Configuration language is used as
     * a fallback for backward compatibility with existing records.
     * </p>
     *
     * @param agent active Agent
     * @param agentConfig Agent Configuration
     * @return effective conversation language
     */
    private String resolveEffectiveLanguage(
            Agent agent,
            AgentConfig agentConfig) {

        if (agent != null
                && agent.getLanguage() != null
                && !agent.getLanguage().isBlank()) {

            return agent.getLanguage().trim();
        }

        if (agentConfig != null
                && agentConfig.getLanguage() != null
                && !agentConfig.getLanguage().isBlank()) {

            return agentConfig.getLanguage().trim();
        }

        throw new IllegalStateException(
                "Agent language is not configured."
        );
    }
}
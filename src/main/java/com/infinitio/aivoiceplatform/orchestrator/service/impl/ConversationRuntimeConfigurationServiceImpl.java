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
 * voice or system prompt. Those values are resolved from the
 * Agent Configuration belonging to the selected Agent.
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
                                agentConfig.getLanguage()
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
                        .build();

        log.info(
                "Conversation runtime configuration resolved successfully. " +
                        "tenantId={}, agentId={}, agentVersion={}, " +
                        "flowPublicId={}, sttProvider={}, llmProvider={}, " +
                        "ttsProvider={}",
                response.getTenantId(),
                response.getAgentId(),
                response.getAgentVersion(),
                response.getFlowPublicId(),
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
                    "Tenant isolation validation failed. " +
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
    // AGENT VERSION
    // =========================================================

    /**
     * Resolves the runtime agent version.
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
}
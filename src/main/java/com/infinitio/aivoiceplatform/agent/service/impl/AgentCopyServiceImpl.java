package com.infinitio.aivoiceplatform.agent.service.impl;

import com.infinitio.aivoiceplatform.agent.constant.AgentMessages;
import com.infinitio.aivoiceplatform.agent.dto.request.CopyAgentRequest;
import com.infinitio.aivoiceplatform.agent.dto.request.CreateAgentRequest;
import com.infinitio.aivoiceplatform.agent.dto.response.AgentResponse;
import com.infinitio.aivoiceplatform.agent.entity.Agent;
import com.infinitio.aivoiceplatform.agent.mapper.AgentMapper;
import com.infinitio.aivoiceplatform.agent.repository.AgentRepository;
import com.infinitio.aivoiceplatform.agent.service.AgentCopyService;
import com.infinitio.aivoiceplatform.agent.validator.AgentValidator;
import com.infinitio.aivoiceplatform.agentconfig.entity.AgentConfig;
import com.infinitio.aivoiceplatform.agentconfig.repository.AgentConfigRepository;
import com.infinitio.aivoiceplatform.auth.service.CurrentUserService;
import com.infinitio.aivoiceplatform.exception.ResourceNotFoundException;
import com.infinitio.aivoiceplatform.flow.entity.Flow;
import com.infinitio.aivoiceplatform.flow.entity.FlowEdge;
import com.infinitio.aivoiceplatform.flow.entity.FlowNode;
import com.infinitio.aivoiceplatform.flow.repository.FlowEdgeRepository;
import com.infinitio.aivoiceplatform.flow.repository.FlowNodeRepository;
import com.infinitio.aivoiceplatform.flow.repository.FlowRepository;
import com.infinitio.aivoiceplatform.master.role.constant.RoleConstants;
import com.infinitio.aivoiceplatform.prompt.constant.PromptConstants;
import com.infinitio.aivoiceplatform.prompt.entity.Prompt;
import com.infinitio.aivoiceplatform.prompt.repository.PromptRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementation of the complete Agent copy operation.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AgentCopyServiceImpl
        implements AgentCopyService {

    private static final Integer NOT_DELETED = 0;

    private static final Integer ACTIVE = 1;

    private final AgentRepository agentRepository;

    private final AgentMapper agentMapper;

    private final AgentValidator agentValidator;

    private final AgentConfigRepository agentConfigRepository;

    private final PromptRepository promptRepository;

    private final FlowRepository flowRepository;

    private final FlowNodeRepository flowNodeRepository;

    private final FlowEdgeRepository flowEdgeRepository;

    private final CurrentUserService currentUserService;


    // =========================================================
    // COPY AGENT
    // =========================================================

    /**
     * {@inheritDoc}
     */
    @Override
    public AgentResponse copy(
            String publicId,
            CopyAgentRequest request) {

        log.info(
                "Copying Agent. sourcePublicId={}, targetAgentCode={}",
                publicId,
                request != null
                        ? request.getAgentCode()
                        : null
        );

        Agent sourceAgent =
                agentValidator.validateAndGet(
                        publicId
                );

        validateCopyAccess(
                sourceAgent
        );

        /*
         * Build a CreateAgentRequest so the existing Agent
         * validation rules are reused.
         */
        CreateAgentRequest createRequest =
                CreateAgentRequest.builder()
                        .organizationPublicId(
                                sourceAgent
                                        .getOrganization()
                                        .getPublicId()
                        )
                        .tenantPublicId(
                                sourceAgent
                                        .getTenant()
                                        .getPublicId()
                        )
                        .agentCode(
                                request
                                        .getAgentCode()
                                        .trim()
                        )
                        .agentName(
                                request
                                        .getAgentName()
                                        .trim()
                        )
                        .description(
                                request.getDescription() != null
                                        ? request
                                        .getDescription()
                                        .trim()
                                        : sourceAgent
                                        .getDescription()
                        )
                        .welcomeMessage(
                                sourceAgent
                                        .getWelcomeMessage()
                        )
                        .language(
                                sourceAgent
                                        .getLanguage()
                        )
                        .build();

        agentValidator.validateCreate(
                createRequest,
                sourceAgent
                        .getTenant()
                        .getId()
        );

        Long currentUserId =
                currentUserService.getCurrentUserId();

        /*
         * Create a completely new Agent entity.
         *
         * AgentMapper ignores ID, publicId, audit fields,
         * tenant and organization, so those are assigned below.
         */
        Agent copiedAgent =
                agentMapper.toEntity(
                        createRequest
                );

        copiedAgent.setOrganization(
                sourceAgent.getOrganization()
        );

        copiedAgent.setTenant(
                sourceAgent.getTenant()
        );

        copiedAgent.setCreatedBy(
                currentUserId
        );

        copiedAgent.setIsActive(
                ACTIVE
        );

        copiedAgent.setIsDeleted(
                NOT_DELETED
        );

        Agent savedAgent =
                agentRepository.save(
                        copiedAgent
                );

        /*
         * Copy all related Agent data.
         */
        copyAgentConfiguration(
                sourceAgent,
                savedAgent,
                currentUserId
        );

        copyPrompts(
                sourceAgent,
                savedAgent,
                currentUserId
        );

        copyFlows(
                sourceAgent,
                savedAgent,
                currentUserId
        );

        log.info(
                "Agent copied successfully. " +
                        "sourcePublicId={}, copiedPublicId={}",
                publicId,
                savedAgent.getPublicId()
        );

        return agentMapper.toResponse(
                savedAgent
        );
    }


    // =========================================================
    // ACCESS VALIDATION
    // =========================================================

    /**
     * Prevents non-SUPER_ADMIN users from copying an Agent
     * belonging to another Tenant.
     *
     * @param sourceAgent source Agent
     */
    private void validateCopyAccess(
            Agent sourceAgent) {

        var currentUser =
                currentUserService.getCurrentUser();

        if (RoleConstants.SUPER_ADMIN.equals(
                currentUser
                        .getRole()
                        .getRoleName()
        )) {

            return;
        }

        if (!currentUser
                .getTenant()
                .getId()
                .equals(
                        sourceAgent
                                .getTenant()
                                .getId()
                )) {

            /*
             * Return "not found" instead of exposing the existence
             * of another tenant's Agent.
             */
            throw new ResourceNotFoundException(
                    AgentMessages.AGENT_COPY_SOURCE_NOT_FOUND
            );
        }
    }


    // =========================================================
    // COPY AGENT CONFIGURATION
    // =========================================================

    /**
     * Copies Agent Configuration when the source Agent has one.
     *
     * @param sourceAgent source Agent
     * @param copiedAgent copied Agent
     * @param currentUserId authenticated user ID
     */
    private void copyAgentConfiguration(
            Agent sourceAgent,
            Agent copiedAgent,
            Long currentUserId) {

        agentConfigRepository
                .findByAgentIdAndIsDeleted(
                        sourceAgent.getId(),
                        NOT_DELETED
                )
                .ifPresent(
                        sourceConfig -> {

                            AgentConfig copiedConfig =
                                    AgentConfig.builder()
                                            .agent(
                                                    copiedAgent
                                            )
                                            .llmProvider(
                                                    sourceConfig
                                                            .getLlmProvider()
                                            )
                                            .llmModel(
                                                    sourceConfig
                                                            .getLlmModel()
                                            )
                                            .sttProvider(
                                                    sourceConfig
                                                            .getSttProvider()
                                            )
                                            .sttModel(
                                                    sourceConfig
                                                            .getSttModel()
                                            )
                                            .ttsProvider(
                                                    sourceConfig
                                                            .getTtsProvider()
                                            )
                                            .ttsModel(
                                                    sourceConfig
                                                            .getTtsModel()
                                            )
                                            .language(
                                                    sourceConfig
                                                            .getLanguage()
                                            )
                                            .voice(
                                                    sourceConfig
                                                            .getVoice()
                                            )
                                            .greetingMessage(
                                                    sourceConfig
                                                            .getGreetingMessage()
                                            )
                                            .systemPrompt(
                                                    sourceConfig
                                                            .getSystemPrompt()
                                            )
                                            .temperature(
                                                    sourceConfig
                                                            .getTemperature()
                                            )
                                            .maxTokens(
                                                    sourceConfig
                                                            .getMaxTokens()
                                            )
                                            .status(
                                                    sourceConfig
                                                            .getStatus()
                                            )
                                            .isActive(
                                                    sourceConfig
                                                            .getIsActive()
                                            )
                                            .isDeleted(
                                                    NOT_DELETED
                                            )
                                            .createdBy(
                                                    currentUserId
                                            )
                                            .build();

                            agentConfigRepository.save(
                                    copiedConfig
                            );
                        }
                );
    }


    // =========================================================
    // COPY PROMPTS
    // =========================================================

    /**
     * Copies all active Prompts of the source Agent.
     *
     * <p>
     * Prompt code and name are globally unique in the current
     * database model. Therefore unique values are generated
     * for the copied Prompts.
     * </p>
     *
     * @param sourceAgent source Agent
     * @param copiedAgent copied Agent
     * @param currentUserId authenticated user ID
     */
    private void copyPrompts(
            Agent sourceAgent,
            Agent copiedAgent,
            Long currentUserId) {

        List<Prompt> sourcePrompts =
                promptRepository
                        .findAllByAgentIdAndIsDeleted(
                                sourceAgent.getId(),
                                NOT_DELETED
                        );

        for (Prompt sourcePrompt :
                sourcePrompts) {

            Prompt copiedPrompt =
                    Prompt.builder()
                            .agent(
                                    copiedAgent
                            )
                            .promptCode(
                                    generateUniquePromptCode(
                                            sourcePrompt
                                                    .getPromptCode(),
                                            copiedAgent
                                                    .getAgentCode()
                                    )
                            )
                            .promptName(
                                    generateUniquePromptName(
                                            sourcePrompt
                                                    .getPromptName(),
                                            copiedAgent
                                                    .getAgentName()
                                    )
                            )
                            .description(
                                    sourcePrompt
                                            .getDescription()
                            )
                            .systemPrompt(
                                    sourcePrompt
                                            .getSystemPrompt()
                            )
                            .promptType(
                                    sourcePrompt
                                            .getPromptType()
                            )
                            .version(
                                    sourcePrompt
                                            .getVersion()
                            )
                            .defaultPrompt(
                                    sourcePrompt
                                            .getDefaultPrompt()
                            )
                            .isActive(
                                    sourcePrompt
                                            .getIsActive()
                            )
                            .isDeleted(
                                    NOT_DELETED
                            )
                            .createdBy(
                                    currentUserId
                            )
                            .build();

            promptRepository.save(
                    copiedPrompt
            );
        }
    }


    // =========================================================
    // UNIQUE PROMPT CODE
    // =========================================================

    /**
     * Generates a globally unique Prompt code.
     *
     * @param sourceCode source Prompt code
     * @param agentCode copied Agent code
     * @return unique Prompt code
     */
    private String generateUniquePromptCode(
            String sourceCode,
            String agentCode) {

        String base =
                normalizeToLength(
                        agentCode
                                + "_"
                                + sourceCode,
                        PromptConstants
                                .PROMPT_CODE_MAX_LENGTH
                );

        String candidate = base;

        int suffix = 1;

        while (
                promptRepository
                        .existsByPromptCode(
                                candidate
                        )
        ) {

            String suffixText =
                    "_COPY"
                            + suffix++;

            candidate =
                    normalizeToLength(
                            base,
                            PromptConstants
                                    .PROMPT_CODE_MAX_LENGTH
                                    - suffixText.length()
                    )
                            + suffixText;
        }

        return candidate;
    }


    // =========================================================
    // UNIQUE PROMPT NAME
    // =========================================================

    /**
     * Generates a globally unique Prompt name.
     *
     * @param sourceName source Prompt name
     * @param agentName copied Agent name
     * @return unique Prompt name
     */
    private String generateUniquePromptName(
            String sourceName,
            String agentName) {

        String base =
                normalizeToLength(
                        agentName
                                + " - "
                                + sourceName,
                        PromptConstants
                                .PROMPT_NAME_MAX_LENGTH
                );

        String candidate = base;

        int suffix = 1;

        while (
                promptRepository
                        .existsByPromptName(
                                candidate
                        )
        ) {

            String suffixText =
                    " - Copy "
                            + suffix++;

            candidate =
                    normalizeToLength(
                            base,
                            PromptConstants
                                    .PROMPT_NAME_MAX_LENGTH
                                    - suffixText.length()
                    )
                            + suffixText;
        }

        return candidate;
    }


    // =========================================================
    // COPY FLOWS
    // =========================================================

    /**
     * Copies all active Flows of the source Agent.
     *
     * @param sourceAgent source Agent
     * @param copiedAgent copied Agent
     * @param currentUserId authenticated user ID
     */
    private void copyFlows(
            Agent sourceAgent,
            Agent copiedAgent,
            Long currentUserId) {

        List<Flow> sourceFlows =
                flowRepository
                        .findAllByAgentIdAndIsDeleted(
                                sourceAgent.getId(),
                                NOT_DELETED
                        );

        for (Flow sourceFlow :
                sourceFlows) {

            Flow copiedFlow =
                    Flow.builder()
                            .agent(
                                    copiedAgent
                            )
                            .name(
                                    sourceFlow.getName()
                            )
                            .description(
                                    sourceFlow.getDescription()
                            )
                            .flowType(
                                    sourceFlow.getFlowType()
                            )
                            .status(
                                    sourceFlow.getStatus()
                            )
                            .version(
                                    sourceFlow.getVersion()
                            )
                            .isActive(
                                    sourceFlow.getIsActive()
                            )
                            .isDeleted(
                                    NOT_DELETED
                            )
                            .createdBy(
                                    currentUserId
                            )
                            .build();

            Flow savedFlow =
                    flowRepository.save(
                            copiedFlow
                    );

            copyFlowGraph(
                    sourceFlow,
                    savedFlow,
                    currentUserId
            );
        }
    }


    // =========================================================
    // COPY FLOW GRAPH
    // =========================================================

    /**
     * Copies active Flow Nodes and Flow Edges and reconnects the
     * copied edges to the newly created nodes.
     *
     * @param sourceFlow source Flow
     * @param copiedFlow copied Flow
     * @param currentUserId authenticated user ID
     */
    private void copyFlowGraph(
            Flow sourceFlow,
            Flow copiedFlow,
            Long currentUserId) {

        List<FlowNode> sourceNodes =
                flowNodeRepository
                        .findByFlowIdAndIsDeleted(
                                sourceFlow.getId(),
                                NOT_DELETED
                        );

        Map<Long, FlowNode> nodeMap =
                new HashMap<>();

        /*
         * ---------------------------------------------------------
         * Copy Nodes
         * ---------------------------------------------------------
         */
        for (FlowNode sourceNode :
                sourceNodes) {

            FlowNode copiedNode =
                    FlowNode.builder()
                            .flow(
                                    copiedFlow
                            )
                            .nodeKey(
                                    sourceNode.getNodeKey()
                            )
                            .name(
                                    sourceNode.getName()
                            )
                            .nodeType(
                                    sourceNode.getNodeType()
                            )
                            .configuration(
                                    sourceNode.getConfiguration()
                            )
                            .positionX(
                                    sourceNode.getPositionX()
                            )
                            .positionY(
                                    sourceNode.getPositionY()
                            )
                            .isActive(
                                    sourceNode.getIsActive()
                            )
                            .isDeleted(
                                    NOT_DELETED
                            )
                            .createdBy(
                                    currentUserId
                            )
                            .build();

            FlowNode savedNode =
                    flowNodeRepository.save(
                            copiedNode
                    );

            nodeMap.put(
                    sourceNode.getId(),
                    savedNode
            );
        }

        /*
         * ---------------------------------------------------------
         * Copy Edges
         * ---------------------------------------------------------
         */
        List<FlowEdge> sourceEdges =
                flowEdgeRepository
                        .findByFlowIdAndIsDeleted(
                                sourceFlow.getId(),
                                NOT_DELETED
                        );

        for (FlowEdge sourceEdge :
                sourceEdges) {

            FlowNode copiedSourceNode =
                    nodeMap.get(
                            sourceEdge
                                    .getSourceNode()
                                    .getId()
                    );

            FlowNode copiedTargetNode =
                    nodeMap.get(
                            sourceEdge
                                    .getTargetNode()
                                    .getId()
                    );

            /*
             * An active edge should normally always have active
             * source and target nodes. Skip it safely if the
             * source graph contains inconsistent data.
             */
            if (copiedSourceNode == null
                    || copiedTargetNode == null) {

                log.warn(
                        "Skipping Flow edge because source or target " +
                                "node was not copied. " +
                                "sourceFlowPublicId={}, edgePublicId={}",
                        sourceFlow.getPublicId(),
                        sourceEdge.getPublicId()
                );

                continue;
            }

            FlowEdge copiedEdge =
                    FlowEdge.builder()
                            .flow(
                                    copiedFlow
                            )
                            .sourceNode(
                                    copiedSourceNode
                            )
                            .sourcePort(
                                    sourceEdge
                                            .getSourcePort()
                            )
                            .targetNode(
                                    copiedTargetNode
                            )
                            .targetPort(
                                    sourceEdge
                                            .getTargetPort()
                            )
                            .label(
                                    sourceEdge
                                            .getLabel()
                            )
                            .conditionExpression(
                                    sourceEdge
                                            .getConditionExpression()
                            )
                            .priority(
                                    sourceEdge
                                            .getPriority()
                            )
                            .isActive(
                                    sourceEdge
                                            .getIsActive()
                            )
                            .isDeleted(
                                    NOT_DELETED
                            )
                            .createdBy(
                                    currentUserId
                            )
                            .build();

            flowEdgeRepository.save(
                    copiedEdge
            );
        }
    }


    // =========================================================
    // STRING LENGTH
    // =========================================================

    /**
     * Truncates a value to a maximum length.
     *
     * @param value source value
     * @param maxLength maximum length
     * @return truncated value
     */
    private String normalizeToLength(
            String value,
            int maxLength) {

        if (value == null) {
            return "";
        }

        if (value.length() <= maxLength) {
            return value;
        }

        return value.substring(
                0,
                maxLength
        );
    }
}
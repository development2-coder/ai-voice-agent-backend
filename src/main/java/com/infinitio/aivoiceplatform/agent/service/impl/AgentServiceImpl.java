package com.infinitio.aivoiceplatform.agent.service.impl;

import com.infinitio.aivoiceplatform.agent.dto.request.CreateAgentRequest;
import com.infinitio.aivoiceplatform.agent.dto.request.UpdateAgentRequest;
import com.infinitio.aivoiceplatform.agent.dto.response.AgentResponse;
import com.infinitio.aivoiceplatform.agent.entity.Agent;
import com.infinitio.aivoiceplatform.agent.mapper.AgentMapper;
import com.infinitio.aivoiceplatform.agent.repository.AgentRepository;
import com.infinitio.aivoiceplatform.agent.service.AgentService;
import com.infinitio.aivoiceplatform.agent.validator.AgentValidator;
import com.infinitio.aivoiceplatform.auth.service.CurrentUserService;
import com.infinitio.aivoiceplatform.common.dto.PageResponse;
import com.infinitio.aivoiceplatform.flow.dto.response.FlowNodeDefinitionResponse;
import com.infinitio.aivoiceplatform.flow.service.FlowNodeDefinitionService;
import com.infinitio.aivoiceplatform.organization.tenant.entity.Tenant;
import com.infinitio.aivoiceplatform.organization.tenant.validator.TenantValidator;
import com.infinitio.aivoiceplatform.agent.constant.AgentConstants;
import com.infinitio.aivoiceplatform.flow.constant.FlowNodeType;
import com.infinitio.aivoiceplatform.flow.constant.FlowType;
import com.infinitio.aivoiceplatform.flow.dto.request.AddFlowEdgeRequest;
import com.infinitio.aivoiceplatform.flow.dto.request.AddFlowNodeRequest;
import com.infinitio.aivoiceplatform.flow.dto.request.CreateFlowRequest;
import com.infinitio.aivoiceplatform.flow.dto.response.FlowResponse;
import com.infinitio.aivoiceplatform.flow.service.FlowService;
import com.infinitio.aivoiceplatform.flow.service.FlowEdgeService;
import com.infinitio.aivoiceplatform.agent.dto.response.AgentWorkspaceResponse;
import com.infinitio.aivoiceplatform.flow.dto.response.FlowDefinitionResponse;
import com.infinitio.aivoiceplatform.flow.dto.response.FlowResponse;
import com.infinitio.aivoiceplatform.flow.service.FlowNodeDefinitionService;
import com.infinitio.aivoiceplatform.flow.service.FlowService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service implementation for Agent.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AgentServiceImpl implements AgentService {

    private static final Integer ACTIVE = 1;
    private static final Integer NOT_DELETED = 0;

    private final AgentRepository agentRepository;

    private final AgentMapper agentMapper;

    private final AgentValidator agentValidator;

    private final TenantValidator tenantValidator;

    private final CurrentUserService currentUserService;

    private final FlowService flowService;

    private final FlowEdgeService flowEdgeService;

    private final FlowNodeDefinitionService
            flowNodeDefinitionService;

    // =========================================================
    // CREATE
    // =========================================================

    /**
     * Creates an Agent and initializes its default Draft Flow.
     *
     * <p>
     * A newly created Agent receives an empty visual workflow
     * containing only START and END nodes. The conversation
     * sequence is intentionally not predefined so that the
     * frontend Flow Builder can construct a client-defined flow.
     * </p>
     *
     * @param request Agent creation request
     * @return created Agent response
     */
    @Override
    public AgentResponse create(
            CreateAgentRequest request) {

        log.info(
                "Creating Agent. Agent Code : {}",
                request.getAgentCode()
        );

        log.info(
                "Resolving Tenant for Agent creation. Tenant Public Id : {}",
                request.getTenantPublicId()
        );

        Tenant tenant =
                tenantValidator.validateAndGet(
                        request.getTenantPublicId()
                );

        agentValidator.validateCreate(
                request,
                tenant.getId()
        );

        Agent agent =
                agentMapper.toEntity(request);

        agent.setCreatedBy(
                currentUserService.getCurrentUserId()
        );

        agent.setTenant(tenant);

        /*
         * Tenant belongs to an Organization.
         * Keep Agent organization synchronized with Tenant.
         */
        agent.setOrganization(
                tenant.getOrganization()
        );

        agent.setIsActive(
                ACTIVE
        );

        agent.setIsDeleted(
                NOT_DELETED
        );

        Agent savedAgent =
                agentRepository.save(agent);

        log.info(
                "Agent created successfully. Public Id : {}",
                savedAgent.getPublicId()
        );

        /*
         * Initialize the visual Flow Builder workspace.
         *
         * Only START and END nodes are created.
         * No STT -> LLM -> TTS sequence is hardcoded.
         */
//        initializeDefaultFlow(
//                savedAgent
//        );

        log.info(
                "Agent workspace initialized successfully. " +
                        "agentPublicId={}",
                savedAgent.getPublicId()
        );

        return agentMapper.toResponse(
                savedAgent
        );
    }


    // =========================================================
    // UPDATE
    // =========================================================

    @Override
    public AgentResponse update(
            UpdateAgentRequest request) {

        log.info(
                "Updating Agent. Public Id : {}",
                request.getPublicId()
        );

        Agent existingAgent =
                agentValidator.validateAndGet(
                        request.getPublicId()
                );

        /*
         * If tenantPublicId is provided, validate the new tenant.
         * Otherwise retain the existing tenant.
         */
        Tenant tenant;

        if (request.getTenantPublicId() != null
                && !request.getTenantPublicId().isBlank()) {

            tenant =
                    tenantValidator.validateAndGet(
                            request.getTenantPublicId()
                    );

        } else {

            tenant =
                    existingAgent.getTenant();
        }

        agentValidator.validateUpdate(
                request,
                tenant.getId(),
                existingAgent.getPublicId()
        );

        agentMapper.updateEntityFromRequest(
                request,
                existingAgent
        );

        /*
         * Update tenant relationship.
         */
        existingAgent.setTenant(tenant);

        /*
         * Tenant belongs to Organization.
         */
        existingAgent.setOrganization(
                tenant.getOrganization()
        );

        Agent updatedAgent =
                agentRepository.save(
                        existingAgent
                );

        log.info(
                "Agent updated successfully. Public Id : {}",
                updatedAgent.getPublicId()
        );

        return agentMapper.toResponse(
                updatedAgent
        );
    }


    // =========================================================
    // GET BY PUBLIC ID
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public AgentResponse getByPublicId(
            String publicId) {

        log.info(
                "Fetching Agent. Public Id : {}",
                publicId
        );

        Agent agent =
                agentValidator.validateAndGet(
                        publicId
                );

        return agentMapper.toResponse(agent);
    }


    // =========================================================
    // GET ALL
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public PageResponse<AgentResponse> getAll(
            int page,
            int size) {

        log.info(
                "Fetching Agents. Page : {}, Size : {}",
                page,
                size
        );

        Page<Agent> agentPage =
                agentRepository.findByIsDeleted(
                        NOT_DELETED,
                        PageRequest.of(page, size)
                );

        return PageResponse
                .<AgentResponse>builder()
                .content(
                        agentPage.getContent()
                                .stream()
                                .map(agentMapper::toResponse)
                                .toList()
                )
                .pageNumber(
                        agentPage.getNumber()
                )
                .pageSize(
                        agentPage.getSize()
                )
                .totalElements(
                        agentPage.getTotalElements()
                )
                .totalPages(
                        agentPage.getTotalPages()
                )
                .first(
                        agentPage.isFirst()
                )
                .last(
                        agentPage.isLast()
                )
                .build();
    }


    // =========================================================
    // DELETE
    // =========================================================

    @Override
    public void delete(
            String publicId) {

        log.info(
                "Deleting Agent. Public Id : {}",
                publicId
        );

        Agent agent =
                agentValidator.validateAndGet(
                        publicId
                );

        agent.markAsDeleted(1L);

        agentRepository.save(agent);

        log.info(
                "Agent deleted successfully. Public Id : {}",
                publicId
        );
    }


    // =========================================================
    // ACTIVATE
    // =========================================================

    @Override
    public void activate(
            String publicId) {

        log.info(
                "Activating Agent. Public Id : {}",
                publicId
        );

        Agent agent =
                agentValidator.validateAndGet(
                        publicId
                );

        agent.activate(1L);

        agentRepository.save(agent);

        log.info(
                "Agent activated successfully. Public Id : {}",
                publicId
        );
    }


    // =========================================================
    // DEACTIVATE
    // =========================================================

    @Override
    public void deactivate(
            String publicId) {

        log.info(
                "Deactivating Agent. Public Id : {}",
                publicId
        );

        Agent agent =
                agentValidator.validateAndGet(
                        publicId
                );

        agent.deactivate(1L);

        agentRepository.save(agent);

        log.info(
                "Agent deactivated successfully. Public Id : {}",
                publicId
        );
    }

    /**
     * Initializes the default Draft Flow for a newly created Agent.
     *
     * <p>
     * The initial Flow contains only START and END nodes connected
     * through the main output/input ports. The actual conversation
     * behavior is intentionally left to the user through the visual
     * Flow Builder.
     * </p>
     *
     * @param agent newly created Agent
     */
//    private void initializeDefaultFlow(
//            Agent agent) {
//
//        log.info(
//                "Initializing default Flow. agentPublicId={}",
//                agent.getPublicId()
//        );
//
//        CreateFlowRequest flowRequest =
//                CreateFlowRequest.builder()
//                        .agentPublicId(
//                                agent.getPublicId()
//                        )
//                        .name(
//                                AgentConstants.INITIAL_FLOW_NAME
//                        )
//                        .description(
//                                AgentConstants.INITIAL_FLOW_DESCRIPTION
//                        )
//                        .flowType(
//                                FlowType.BOTH
//                        )
//                        .build();
//
//        FlowResponse flow =
//                flowService.create(
//                        flowRequest
//                );
//
//        log.debug(
//                "Default Flow created. flowPublicId={}, agentPublicId={}",
//                flow.getPublicId(),
//                agent.getPublicId()
//        );
//
//        AddFlowNodeRequest startNodeRequest =
//                AddFlowNodeRequest.builder()
//                        .flowPublicId(
//                                flow.getPublicId()
//                        )
//                        .nodeKey(
//                                AgentConstants.INITIAL_START_NODE_KEY
//                        )
//                        .name(
//                                AgentConstants.INITIAL_START_NODE_NAME
//                        )
//                        .nodeType(
//                                FlowNodeType.START
//                        )
//                        .configuration("{}")
//                        .positionX(100.0)
//                        .positionY(200.0)
//                        .build();
//
//        flowService.addNode(
//                startNodeRequest
//        );
//
//        AddFlowNodeRequest endNodeRequest =
//                AddFlowNodeRequest.builder()
//                        .flowPublicId(
//                                flow.getPublicId()
//                        )
//                        .nodeKey(
//                                AgentConstants.INITIAL_END_NODE_KEY
//                        )
//                        .name(
//                                AgentConstants.INITIAL_END_NODE_NAME
//                        )
//                        .nodeType(
//                                FlowNodeType.END
//                        )
//                        .configuration("{}")
//                        .positionX(500.0)
//                        .positionY(200.0)
//                        .build();
//
//        flowService.addNode(
//                endNodeRequest
//        );
//
//        AddFlowEdgeRequest edgeRequest =
//                AddFlowEdgeRequest.builder()
//                        .flowPublicId(
//                                flow.getPublicId()
//                        )
//                        .sourceNodeKey(
//                                AgentConstants.INITIAL_START_NODE_KEY
//                        )
//                        .sourcePort(
//                                AgentConstants.MAIN_PORT
//                        )
//                        .targetNodeKey(
//                                AgentConstants.INITIAL_END_NODE_KEY
//                        )
//                        .targetPort(
//                                AgentConstants.MAIN_PORT
//                        )
//                        .priority(0)
//                        .build();
//
//        flowEdgeService.addEdge(
//                edgeRequest
//        );
//
//        log.info(
//                "Default Flow nodes initialized. " +
//                        "flowPublicId={}, startNode={}, endNode={}",
//                flow.getPublicId(),
//                AgentConstants.INITIAL_START_NODE_KEY,
//                AgentConstants.INITIAL_END_NODE_KEY
//        );
//    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public AgentWorkspaceResponse getWorkspace(
            String publicId) {

        log.info(
                "Fetching Agent workspace. publicId={}",
                publicId
        );

        Agent agent =
                agentValidator.validateAndGet(
                        publicId
                );

        AgentResponse agentResponse =
                agentMapper.toResponse(
                        agent
                );

//        FlowResponse flowResponse =
//                flowService.getLatestByAgentPublicId(
//                        publicId
//                );
//
//        FlowDefinitionResponse flowDefinition =
//                flowService.getDefinition(
//                        flowResponse.getPublicId()
//                );

        List<FlowNodeDefinitionResponse> nodeTypes =
                flowNodeDefinitionService.getAll();

        log.info(
                "Agent workspace fetched successfully. " +
                        "agentPublicId={}, nodeTypeCount={}",
                publicId,
//                flowResponse.getPublicId(),
//                flowDefinition.getNodes().size(),
                nodeTypes.size()
        );

        return AgentWorkspaceResponse.builder()
                .agent(agentResponse)
//                .flow(flowDefinition)
                .nodeTypes(nodeTypes)
                .build();
    }
}
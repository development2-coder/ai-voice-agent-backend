package com.infinitio.aivoiceplatform.flow.service.impl;
import com.infinitio.aivoiceplatform.exception.ResourceNotFoundException;
import com.infinitio.aivoiceplatform.flow.constant.FlowMessages;
import com.infinitio.aivoiceplatform.flow.dto.response.*;
import com.infinitio.aivoiceplatform.agent.entity.Agent;
import com.infinitio.aivoiceplatform.agent.validator.AgentValidator;
import com.infinitio.aivoiceplatform.auth.service.CurrentUserService;
import com.infinitio.aivoiceplatform.flow.constant.FlowStatus;
import com.infinitio.aivoiceplatform.flow.dto.request.AddFlowNodeRequest;
import com.infinitio.aivoiceplatform.flow.dto.request.CreateFlowRequest;
import com.infinitio.aivoiceplatform.flow.dto.request.UpdateFlowNodeRequest;
import com.infinitio.aivoiceplatform.flow.dto.request.UpdateFlowRequest;
import com.infinitio.aivoiceplatform.flow.entity.Flow;
import com.infinitio.aivoiceplatform.flow.mapper.FlowMapper;
import com.infinitio.aivoiceplatform.flow.repository.FlowRepository;
import com.infinitio.aivoiceplatform.flow.service.FlowEdgeService;
import com.infinitio.aivoiceplatform.flow.service.FlowNodeService;
import com.infinitio.aivoiceplatform.flow.service.FlowService;
import com.infinitio.aivoiceplatform.flow.validator.FlowGraphValidator;
import com.infinitio.aivoiceplatform.flow.validator.FlowValidator;
import com.infinitio.aivoiceplatform.flow.constant.FlowType;
import com.infinitio.aivoiceplatform.exception.ResourceNotFoundException;
import com.infinitio.aivoiceplatform.flow.dto.request.AddFlowEdgeRequest;
import com.infinitio.aivoiceplatform.flow.dto.request.AddFlowNodeRequest;
import com.infinitio.aivoiceplatform.flow.dto.request.SaveFlowEdgeRequest;
import com.infinitio.aivoiceplatform.flow.dto.request.SaveFlowNodeRequest;
import com.infinitio.aivoiceplatform.flow.dto.request.SaveFlowWorkspaceRequest;
import com.infinitio.aivoiceplatform.flow.dto.request.UpdateFlowNodeRequest;
import java.util.Arrays;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Default implementation of Flow management operations.
 *
 * <p>
 * This service owns Flow-level lifecycle operations.
 * Node lifecycle operations are delegated to
 * {@link FlowNodeService}.
 * </p>
 *
 * <p>
 * Graph validation is delegated to
 * {@link FlowGraphValidator} before a Flow is activated.
 * </p>
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class FlowServiceImpl
        implements FlowService {

    /**
     * Represents a soft-deleted Flow.
     */
    private static final Integer DELETED = 1;

    /**
     * Flow repository.
     */
    private final FlowRepository flowRepository;

    /**
     * Agent validator.
     */
    private final AgentValidator agentValidator;

    /**
     * Flow validator.
     */
    private final FlowValidator flowValidator;

    /**
     * Flow mapper.
     */
    private final FlowMapper flowMapper;

    /**
     * Node management service.
     */
    private final FlowNodeService flowNodeService;

    /**
     * Graph structure validator.
     */
    private final FlowGraphValidator flowGraphValidator;

    /**
     * Current authenticated user service.
     */
    private final CurrentUserService currentUserService;

    private final FlowEdgeService flowEdgeService;

    // =========================================================
    // CREATE FLOW
    // =========================================================

    /**
     * {@inheritDoc}
     */
    @Override
    public FlowResponse create(
            CreateFlowRequest request) {

        log.info(
                "Creating Flow. agentPublicId={}, name={}",
                request.getAgentPublicId(),
                request.getName()
        );

        Agent agent =
                agentValidator.validateAndGet(
                        request.getAgentPublicId()
                );

        flowValidator.validateForCreate(
                request,
                agent
        );

        Flow flow =
                flowMapper.toEntity(
                        request
                );

        flow.setAgent(
                agent
        );

        flow.setCreatedBy(
                currentUserService.getCurrentUserId()
        );

        Flow saved =
                flowRepository.save(
                        flow
                );

        log.info(
                "Flow created successfully. " +
                        "publicId={}, agentPublicId={}",
                saved.getPublicId(),
                agent.getPublicId()
        );

        return flowMapper.toResponse(
                saved
        );
    }

    // =========================================================
    // UPDATE FLOW
    // =========================================================

    /**
     * {@inheritDoc}
     */
    @Override
    public FlowResponse update(
            UpdateFlowRequest request) {

        log.info(
                "Updating Flow. publicId={}",
                request.getPublicId()
        );

        Agent agent =
                agentValidator.validateAndGet(
                        request.getAgentPublicId()
                );

        flowValidator.validateForUpdate(
                request,
                agent
        );

        Flow flow =
                flowValidator.validateAndGet(
                        request.getPublicId()
                );

        flowMapper.updateEntity(
                request,
                flow
        );

        flow.setAgent(
                agent
        );

        flow.setUpdatedBy(
                currentUserService.getCurrentUserId()
        );

        Flow saved =
                flowRepository.save(
                        flow
                );

        log.info(
                "Flow updated successfully. " +
                        "publicId={}, agentPublicId={}",
                saved.getPublicId(),
                agent.getPublicId()
        );

        return flowMapper.toResponse(
                saved
        );
    }

    // =========================================================
    // GET FLOW
    // =========================================================

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public FlowResponse getByPublicId(
            String publicId) {

        log.debug(
                "Fetching Flow. publicId={}",
                publicId
        );

        Flow flow =
                flowValidator.validateAndGet(
                        publicId
                );

        return flowMapper.toResponse(
                flow
        );
    }

    // =========================================================
// GET COMPLETE FLOW DEFINITION
// =========================================================

    /**
     * {@inheritDoc}
     *
     * <p>
     * Retrieves the complete Flow definition required by the
     * Flow Builder, including Flow metadata, active nodes and
     * active edges.
     * </p>
     */
    @Override
    @Transactional(readOnly = true)
    public FlowDefinitionResponse getDefinition(
            String publicId) {

        log.debug(
                "Fetching complete Flow definition. publicId={}",
                publicId
        );

        Flow flow =
                flowValidator.validateAndGet(
                        publicId
                );

        FlowResponse flowResponse =
                flowMapper.toResponse(
                        flow
                );

        List<FlowNodeResponse> nodes =
                flowNodeService.getNodes(
                        publicId
                );

        List<FlowEdgeResponse> edges =
                flowEdgeService.getEdges(
                        publicId
                );

        log.debug(
                "Flow definition fetched successfully. " +
                        "publicId={}, nodeCount={}, edgeCount={}",
                publicId,
                nodes.size(),
                edges.size()
        );

        return FlowDefinitionResponse.builder()
                .flow(flowResponse)
                .nodes(nodes)
                .edges(edges)
                .build();
    }

    // =========================================================
    // GET NODES
    // =========================================================

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public List<FlowNodeResponse> getNodes(
            String flowPublicId) {

        log.debug(
                "Delegating Flow node retrieval. " +
                        "flowPublicId={}",
                flowPublicId
        );

        return flowNodeService.getNodes(
                flowPublicId
        );
    }

    // =========================================================
    // ADD NODE
    // =========================================================

    /**
     * {@inheritDoc}
     */
    @Override
    public FlowNodeResponse addNode(
            AddFlowNodeRequest request) {

        log.info(
                "Delegating Flow node creation. " +
                        "flowPublicId={}, nodeKey={}, nodeType={}",
                request.getFlowPublicId(),
                request.getNodeKey(),
                request.getNodeType()
        );

        return flowNodeService.addNode(
                request
        );
    }

    // =========================================================
    // UPDATE NODE
    // =========================================================

    /**
     * {@inheritDoc}
     */
    @Override
    public FlowNodeResponse updateNode(
            UpdateFlowNodeRequest request) {

        log.info(
                "Delegating Flow node update. " +
                        "nodePublicId={}",
                request.getPublicId()
        );

        return flowNodeService.updateNode(
                request
        );
    }

    // =========================================================
    // DELETE NODE
    // =========================================================

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteNode(
            String nodePublicId) {

        log.info(
                "Delegating Flow node deletion. " +
                        "nodePublicId={}",
                nodePublicId
        );

        flowNodeService.deleteNode(
                nodePublicId
        );
    }

    // =========================================================
    // ACTIVATE FLOW
    // =========================================================

    /**
     * {@inheritDoc}
     *
     * <p>
     * A Flow can only become ACTIVE after the complete graph
     * has passed structural validation.
     * </p>
     */
    @Override
    public void activate(
            String publicId) {

        log.info(
                "Activating Flow. publicId={}",
                publicId
        );

        Flow flow =
                flowValidator.validateAndGet(
                        publicId
                );

        /*
         * Validate the complete graph before activation.
         *
         * This checks:
         * - START node
         * - END node
         * - edge integrity
         * - node reachability
         * - END node outgoing transitions
         */
        flowGraphValidator.validateForActivation(
                flow
        );

        flow.setStatus(
                FlowStatus.ACTIVE
        );

        flow.setUpdatedBy(
                currentUserService.getCurrentUserId()
        );

        flowRepository.save(
                flow
        );

        log.info(
                "Flow activated successfully. publicId={}",
                publicId
        );
    }

    // =========================================================
    // DEACTIVATE FLOW
    // =========================================================

    /**
     * {@inheritDoc}
     */
    @Override
    public void deactivate(
            String publicId) {

        log.info(
                "Deactivating Flow. publicId={}",
                publicId
        );

        Flow flow =
                flowValidator.validateAndGet(
                        publicId
                );

        flow.setStatus(
                FlowStatus.INACTIVE
        );

        flow.setUpdatedBy(
                currentUserService.getCurrentUserId()
        );

        flowRepository.save(
                flow
        );

        log.info(
                "Flow deactivated successfully. publicId={}",
                publicId
        );
    }

    // =========================================================
    // DELETE FLOW
    // =========================================================

    /**
     * {@inheritDoc}
     */
    @Override
    public void delete(
            String publicId) {

        log.info(
                "Deleting Flow. publicId={}",
                publicId
        );

        Flow flow =
                flowValidator.validateAndGet(
                        publicId
                );

        flow.setIsDeleted(
                DELETED
        );

        flow.setUpdatedBy(
                currentUserService.getCurrentUserId()
        );

        flowRepository.save(
                flow
        );

        log.info(
                "Flow deleted successfully. publicId={}",
                publicId
        );
    }

    @Override
    public List<FlowTypeResponse> getFlowTypes() {

        log.debug("Fetching supported flow types");

        return Arrays.stream(FlowType.values())
                .map(this::buildFlowTypeResponse)
                .toList();
    }

    /**
     * Builds the response representation for a flow type.
     *
     * @param flowType flow type
     * @return flow type response
     */
    private FlowTypeResponse buildFlowTypeResponse(FlowType flowType) {

        return FlowTypeResponse.builder()
                .code(flowType.name())
                .displayName(getFlowTypeDisplayName(flowType))
                .description(getFlowTypeDescription(flowType))
                .build();
    }

    /**
     * Returns the display name for the specified flow type.
     *
     * @param flowType flow type
     * @return display name
     */
    private String getFlowTypeDisplayName(FlowType flowType) {

        return switch (flowType) {
            case INBOUND -> "Inbound";
            case OUTBOUND -> "Outbound";
            case BOTH -> "Both";
        };
    }

    /**
     * Returns the description for the specified flow type.
     *
     * @param flowType flow type
     * @return description
     */
    private String getFlowTypeDescription(FlowType flowType) {

        return switch (flowType) {
            case INBOUND -> "Flow for incoming calls";
            case OUTBOUND -> "Flow for outgoing calls";
            case BOTH -> "Flow usable for inbound and outbound calls";
        };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public FlowResponse getLatestByAgentPublicId(
            String agentPublicId) {

        log.debug(
                "Fetching latest Flow for Agent. agentPublicId={}",
                agentPublicId
        );

        Agent agent =
                agentValidator.validateAndGet(
                        agentPublicId
                );

        Flow flow =
                flowRepository
                        .findFirstByAgentIdAndIsDeletedOrderByVersionDesc(
                                agent.getId(),
                                0
                        )
                        .orElseThrow(() -> {

                            log.warn(
                                    "No Flow found for Agent. agentPublicId={}",
                                    agentPublicId
                            );

                            return new ResourceNotFoundException(
                                    FlowMessages.NOT_FOUND
                            );
                        });

        log.debug(
                "Latest Flow found. agentPublicId={}, flowPublicId={}",
                agentPublicId,
                flow.getPublicId()
        );

        return flowMapper.toResponse(
                flow
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public FlowDefinitionResponse saveWorkspace(
            String publicId,
            SaveFlowWorkspaceRequest request) {

        log.info(
                "Saving complete Flow workspace. flowPublicId={}",
                publicId
        );

        Flow flow =
                flowValidator.validateAndGet(
                        publicId
                );

        /*
         * ---------------------------------------------------------
         * 1. Update Flow metadata
         * ---------------------------------------------------------
         */
        UpdateFlowRequest flowUpdateRequest =
                UpdateFlowRequest.builder()
                        .publicId(
                                flow.getPublicId()
                        )
                        .agentPublicId(
                                flow.getAgent().getPublicId()
                        )
                        .name(
                                request.getName()
                        )
                        .description(
                                request.getDescription()
                        )
                        .flowType(
                                request.getFlowType()
                        )
                        .build();

        update(flowUpdateRequest);

        /*
         * ---------------------------------------------------------
         * 2. Read existing nodes
         * ---------------------------------------------------------
         */
        List<FlowNodeResponse> existingNodes =
                flowNodeService.getNodes(
                        publicId
                );

        /*
         * ---------------------------------------------------------
         * 3. Update existing nodes
         * ---------------------------------------------------------
         */
        for (SaveFlowNodeRequest nodeRequest :
                request.getNodes()) {

            if (nodeRequest.getPublicId() == null
                    || nodeRequest.getPublicId().isBlank()) {

                continue;
            }

            boolean existingNode =
                    existingNodes.stream()
                            .anyMatch(
                                    node ->
                                            node.getPublicId()
                                                    .equals(
                                                            nodeRequest
                                                                    .getPublicId()
                                                    )
                            );

            if (!existingNode) {

                log.warn(
                        "Flow node does not belong to Flow. " +
                                "flowPublicId={}, nodePublicId={}",
                        publicId,
                        nodeRequest.getPublicId()
                );

                throw new ResourceNotFoundException(
                        FlowMessages.NODE_NOT_FOUND
                );
            }

            UpdateFlowNodeRequest updateNodeRequest =
                    UpdateFlowNodeRequest.builder()
                            .publicId(
                                    nodeRequest.getPublicId()
                            )
                            .nodeKey(
                                    nodeRequest.getNodeKey()
                            )
                            .name(
                                    nodeRequest.getName()
                            )
                            .nodeType(
                                    nodeRequest.getNodeType()
                            )
                            .configuration(
                                    nodeRequest.getConfiguration()
                            )
                            .positionX(
                                    nodeRequest.getPositionX()
                            )
                            .positionY(
                                    nodeRequest.getPositionY()
                            )
                            .build();

            flowNodeService.updateNode(
                    updateNodeRequest
            );
        }

        /*
         * ---------------------------------------------------------
         * 4. Delete nodes removed from the canvas
         * ---------------------------------------------------------
         */
        for (FlowNodeResponse existingNode :
                existingNodes) {

            boolean stillExists =
                    request.getNodes()
                            .stream()
                            .anyMatch(
                                    node ->
                                            existingNode
                                                    .getPublicId()
                                                    .equals(
                                                            node.getPublicId()
                                                    )
                            );

            if (!stillExists) {

                /*
                 * Edges are removed before nodes so that
                 * deleted nodes cannot leave invalid connections.
                 */
                flowEdgeService
                        .getEdges(publicId)
                        .stream()
                        .filter(
                                edge ->
                                        existingNode
                                                .getNodeKey()
                                                .equals(
                                                        edge.getSourceNodeKey()
                                                )
                                                || existingNode
                                                .getNodeKey()
                                                .equals(
                                                        edge.getTargetNodeKey()
                                                )
                        )
                        .forEach(
                                edge ->
                                        flowEdgeService.deleteEdge(
                                                edge.getPublicId()
                                        )
                        );

                flowNodeService.deleteNode(
                        existingNode.getPublicId()
                );
            }
        }

        /*
         * ---------------------------------------------------------
         * 5. Create new nodes
         * ---------------------------------------------------------
         */
        for (SaveFlowNodeRequest nodeRequest :
                request.getNodes()) {

            if (nodeRequest.getPublicId() != null
                    && !nodeRequest.getPublicId().isBlank()) {

                continue;
            }

            AddFlowNodeRequest addNodeRequest =
                    AddFlowNodeRequest.builder()
                            .flowPublicId(
                                    publicId
                            )
                            .nodeKey(
                                    nodeRequest.getNodeKey()
                            )
                            .name(
                                    nodeRequest.getName()
                            )
                            .nodeType(
                                    nodeRequest.getNodeType()
                            )
                            .configuration(
                                    nodeRequest.getConfiguration()
                            )
                            .positionX(
                                    nodeRequest.getPositionX()
                            )
                            .positionY(
                                    nodeRequest.getPositionY()
                            )
                            .build();

            flowNodeService.addNode(
                    addNodeRequest
            );
        }

        /*
         * ---------------------------------------------------------
         * 6. Replace existing edges with submitted edges
         * ---------------------------------------------------------
         *
         * Edge update is not currently exposed by FlowEdgeService.
         * Therefore the safest implementation is:
         *
         * delete current active edges
         * create edges exactly as submitted by the Builder
         */
        List<FlowEdgeResponse> existingEdges =
                flowEdgeService.getEdges(
                        publicId
                );

        for (FlowEdgeResponse edge :
                existingEdges) {

            flowEdgeService.deleteEdge(
                    edge.getPublicId()
            );
        }

        /*
         * ---------------------------------------------------------
         * 7. Create submitted edges
         * ---------------------------------------------------------
         */
        if (request.getEdges() != null) {

            for (SaveFlowEdgeRequest edgeRequest :
                    request.getEdges()) {

                AddFlowEdgeRequest addEdgeRequest =
                        AddFlowEdgeRequest.builder()
                                .flowPublicId(
                                        publicId
                                )
                                .sourceNodeKey(
                                        edgeRequest
                                                .getSourceNodeKey()
                                )
                                .sourcePort(
                                        edgeRequest
                                                .getSourcePort()
                                )
                                .targetNodeKey(
                                        edgeRequest
                                                .getTargetNodeKey()
                                )
                                .targetPort(
                                        edgeRequest
                                                .getTargetPort()
                                )
                                .label(
                                        edgeRequest.getLabel()
                                )
                                .conditionExpression(
                                        edgeRequest
                                                .getConditionExpression()
                                )
                                .priority(
                                        edgeRequest.getPriority()
                                )
                                .build();

                flowEdgeService.addEdge(
                        addEdgeRequest
                );
            }
        }

        /*
         * ---------------------------------------------------------
         * 8. Validate complete graph
         * ---------------------------------------------------------
         */
        Flow updatedFlow =
                flowValidator.validateAndGet(
                        publicId
                );

        flowGraphValidator.validateForActivation(
                updatedFlow
        );

        log.info(
                "Flow workspace saved successfully. " +
                        "flowPublicId={}, nodeCount={}, edgeCount={}",
                publicId,
                flowNodeService.getNodes(publicId).size(),
                flowEdgeService.getEdges(publicId).size()
        );

        return getDefinition(
                publicId
        );
    }
}
package com.infinitio.aivoiceplatform.flow.service.impl;

import com.infinitio.aivoiceplatform.auth.service.CurrentUserService;
import com.infinitio.aivoiceplatform.exception.ConflictException;
import com.infinitio.aivoiceplatform.exception.ResourceNotFoundException;
import com.infinitio.aivoiceplatform.flow.constant.FlowMessages;
import com.infinitio.aivoiceplatform.flow.dto.request.AddFlowEdgeRequest;
import com.infinitio.aivoiceplatform.flow.dto.response.FlowEdgeResponse;
import com.infinitio.aivoiceplatform.flow.dto.response.FlowNodePortResponse;
import com.infinitio.aivoiceplatform.flow.entity.Flow;
import com.infinitio.aivoiceplatform.flow.entity.FlowEdge;
import com.infinitio.aivoiceplatform.flow.entity.FlowNode;
import com.infinitio.aivoiceplatform.flow.mapper.FlowEdgeMapper;
import com.infinitio.aivoiceplatform.flow.repository.FlowEdgeRepository;
import com.infinitio.aivoiceplatform.flow.repository.FlowNodeRepository;
import com.infinitio.aivoiceplatform.flow.service.FlowEdgeService;
import com.infinitio.aivoiceplatform.flow.service.FlowNodePortDefinitionService;
import com.infinitio.aivoiceplatform.flow.validator.FlowValidator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Default implementation of Flow edge management.
 *
 * <p>
 * Edges are port-aware. An edge is therefore defined by:
 * </p>
 *
 * <pre>
 * sourceNode + sourcePort
 *             ↓
 *         targetPort
 *             ↓
 * targetNode
 * </pre>
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class FlowEdgeServiceImpl
        implements FlowEdgeService {

    /**
     * Active record.
     */
    private static final Integer NOT_DELETED = 0;

    /**
     * Soft-deleted record.
     */
    private static final Integer DELETED = 1;

    /**
     * Flow edge repository.
     */
    private final FlowEdgeRepository edgeRepository;

    /**
     * Flow node repository.
     */
    private final FlowNodeRepository nodeRepository;

    /**
     * Flow validator.
     */
    private final FlowValidator flowValidator;

    /**
     * Flow edge mapper.
     */
    private final FlowEdgeMapper edgeMapper;

    /**
     * Port definition service.
     */
    private final FlowNodePortDefinitionService portDefinitionService;

    /**
     * Current authenticated user service.
     */
    private final CurrentUserService currentUserService;

    // =========================================================
    // GET EDGES
    // =========================================================

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public List<FlowEdgeResponse> getEdges(
            String flowPublicId) {

        log.debug(
                "Fetching Flow edges. flowPublicId={}",
                flowPublicId
        );

        Flow flow =
                flowValidator.validateAndGet(
                        flowPublicId
                );

        List<FlowEdgeResponse> edges =
                edgeRepository
                        .findByFlowIdAndIsDeletedOrderByPriorityAsc(
                                flow.getId(),
                                NOT_DELETED
                        )
                        .stream()
                        .map(
                                edgeMapper::toResponse
                        )
                        .toList();

        log.debug(
                "Flow edges fetched successfully. " +
                        "flowPublicId={}, edgeCount={}",
                flowPublicId,
                edges.size()
        );

        return edges;
    }

    // =========================================================
    // ADD EDGE
    // =========================================================

    /**
     * {@inheritDoc}
     */
    @Override
    public FlowEdgeResponse addEdge(
            AddFlowEdgeRequest request) {

        log.info(
                "Adding Flow edge. " +
                        "flowPublicId={}, sourceNode={}, " +
                        "sourcePort={}, targetNode={}, targetPort={}",
                request.getFlowPublicId(),
                request.getSourceNodeKey(),
                request.getSourcePort(),
                request.getTargetNodeKey(),
                request.getTargetPort()
        );

        Flow flow =
                flowValidator.validateAndGet(
                        request.getFlowPublicId()
                );

        FlowNode sourceNode =
                findNode(
                        flow,
                        request.getSourceNodeKey(),
                        true
                );

        FlowNode targetNode =
                findNode(
                        flow,
                        request.getTargetNodeKey(),
                        false
                );

        validateDifferentNodes(
                sourceNode,
                targetNode
        );

        validateSourcePort(
                sourceNode,
                request.getSourcePort()
        );

        validateTargetPort(
                targetNode,
                request.getTargetPort()
        );

        validateDuplicateEdge(
                sourceNode,
                request.getSourcePort(),
                targetNode,
                request.getTargetPort()
        );

        FlowEdge edge =
                edgeMapper.toEntity(
                        request
                );

        edge.setFlow(
                flow
        );

        edge.setSourceNode(
                sourceNode
        );

        edge.setTargetNode(
                targetNode
        );

        edge.setCreatedBy(
                currentUserService.getCurrentUserId()
        );

        FlowEdge saved =
                edgeRepository.save(
                        edge
                );

        log.info(
                "Flow edge created successfully. " +
                        "flowPublicId={}, edgePublicId={}, " +
                        "sourceNode={}, sourcePort={}, " +
                        "targetNode={}, targetPort={}",
                flow.getPublicId(),
                saved.getPublicId(),
                sourceNode.getNodeKey(),
                request.getSourcePort(),
                targetNode.getNodeKey(),
                request.getTargetPort()
        );

        return edgeMapper.toResponse(
                saved
        );
    }

    // =========================================================
    // DELETE EDGE
    // =========================================================

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteEdge(
            String edgePublicId) {

        log.info(
                "Deleting Flow edge. publicId={}",
                edgePublicId
        );

        FlowEdge edge =
                edgeRepository
                        .findByPublicIdAndIsDeleted(
                                edgePublicId,
                                NOT_DELETED
                        )
                        .orElseThrow(() -> {

                            log.warn(
                                    "Flow edge not found. " +
                                            "edgePublicId={}",
                                    edgePublicId
                            );

                            return new ResourceNotFoundException(
                                    FlowMessages.EDGE_NOT_FOUND
                            );
                        });

        edge.setIsDeleted(
                DELETED
        );

        edge.setUpdatedBy(
                currentUserService.getCurrentUserId()
        );

        edgeRepository.save(
                edge
        );

        log.info(
                "Flow edge deleted successfully. " +
                        "publicId={}",
                edgePublicId
        );
    }

    // =========================================================
    // NODE LOOKUP
    // =========================================================

    /**
     * Finds a node belonging to the Flow.
     *
     * @param flow Flow
     * @param nodeKey node key
     * @param source whether this is a source node
     * @return active node
     */
    private FlowNode findNode(
            Flow flow,
            String nodeKey,
            boolean source) {

        return nodeRepository
                .findByFlowIdAndNodeKeyAndIsDeleted(
                        flow.getId(),
                        nodeKey,
                        NOT_DELETED
                )
                .orElseThrow(() -> {

                    log.warn(
                            "{} node not found. " +
                                    "flowPublicId={}, nodeKey={}",
                            source
                                    ? "Source"
                                    : "Target",
                            flow.getPublicId(),
                            nodeKey
                    );

                    return new ResourceNotFoundException(
                            FlowMessages.NODE_NOT_FOUND
                    );
                });
    }

    // =========================================================
    // NODE VALIDATION
    // =========================================================

    /**
     * Prevents a node from being connected to itself.
     *
     * @param sourceNode source node
     * @param targetNode target node
     */
    private void validateDifferentNodes(
            FlowNode sourceNode,
            FlowNode targetNode) {

        if (!sourceNode.getId()
                .equals(targetNode.getId())) {

            return;
        }

        log.warn(
                "Self edge rejected. nodeKey={}",
                sourceNode.getNodeKey()
        );

        throw new ConflictException(
                FlowMessages.SELF_EDGE_NOT_ALLOWED
        );
    }

    // =========================================================
    // SOURCE PORT VALIDATION
    // =========================================================

    /**
     * Validates that the requested source port is defined for
     * the source node type.
     *
     * @param node source node
     * @param sourcePort source output port
     */
    private void validateSourcePort(
            FlowNode node,
            String sourcePort) {

        boolean valid =
                portDefinitionService
                        .getOutputPorts(
                                node.getNodeType()
                        )
                        .stream()
                        .map(
                                FlowNodePortResponse::getPortId
                        )
                        .anyMatch(
                                sourcePort::equals
                        );

        if (valid) {
            return;
        }

        log.warn(
                "Invalid source port. " +
                        "nodeKey={}, nodeType={}, sourcePort={}",
                node.getNodeKey(),
                node.getNodeType(),
                sourcePort
        );

        throw new ResourceNotFoundException(
                FlowMessages.SOURCE_PORT_NOT_FOUND
        );
    }

    // =========================================================
    // TARGET PORT VALIDATION
    // =========================================================

    /**
     * Validates that the requested target port is defined for
     * the target node type.
     *
     * @param node target node
     * @param targetPort target input port
     */
    private void validateTargetPort(
            FlowNode node,
            String targetPort) {

        boolean valid =
                portDefinitionService
                        .getInputPorts(
                                node.getNodeType()
                        )
                        .stream()
                        .map(
                                FlowNodePortResponse::getPortId
                        )
                        .anyMatch(
                                targetPort::equals
                        );

        if (valid) {
            return;
        }

        log.warn(
                "Invalid target port. " +
                        "nodeKey={}, nodeType={}, targetPort={}",
                node.getNodeKey(),
                node.getNodeType(),
                targetPort
        );

        throw new ResourceNotFoundException(
                FlowMessages.TARGET_PORT_NOT_FOUND
        );
    }

    // =========================================================
    // DUPLICATE EDGE VALIDATION
    // =========================================================

    /**
     * Prevents duplicate connections using the complete
     * connection identity.
     *
     * <p>
     * The identity is:
     * </p>
     *
     * <pre>
     * sourceNode + sourcePort +
     * targetNode + targetPort
     * </pre>
     *
     * @param sourceNode source node
     * @param sourcePort source port
     * @param targetNode target node
     * @param targetPort target port
     */
    private void validateDuplicateEdge(
            FlowNode sourceNode,
            String sourcePort,
            FlowNode targetNode,
            String targetPort) {

        boolean exists =
                edgeRepository
                        .existsBySourceNodeIdAndSourcePortAndTargetNodeIdAndTargetPortAndIsDeleted(
                                sourceNode.getId(),
                                sourcePort,
                                targetNode.getId(),
                                targetPort,
                                NOT_DELETED
                        );

        if (!exists) {
            return;
        }

        log.warn(
                "Duplicate Flow edge rejected. " +
                        "sourceNode={}, sourcePort={}, " +
                        "targetNode={}, targetPort={}",
                sourceNode.getNodeKey(),
                sourcePort,
                targetNode.getNodeKey(),
                targetPort
        );

        throw new ConflictException(
                FlowMessages.DUPLICATE_EDGE
        );
    }
}
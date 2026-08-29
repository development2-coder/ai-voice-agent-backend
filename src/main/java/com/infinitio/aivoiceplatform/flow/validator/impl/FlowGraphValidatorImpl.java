package com.infinitio.aivoiceplatform.flow.validator.impl;

import com.infinitio.aivoiceplatform.exception.ConflictException;
import com.infinitio.aivoiceplatform.flow.constant.FlowMessages;
import com.infinitio.aivoiceplatform.flow.constant.FlowNodeType;
import com.infinitio.aivoiceplatform.flow.entity.Flow;
import com.infinitio.aivoiceplatform.flow.entity.FlowEdge;
import com.infinitio.aivoiceplatform.flow.entity.FlowNode;
import com.infinitio.aivoiceplatform.flow.repository.FlowEdgeRepository;
import com.infinitio.aivoiceplatform.flow.repository.FlowNodeRepository;
import com.infinitio.aivoiceplatform.flow.validator.FlowGraphValidator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Default implementation of Flow graph validation.
 *
 * <p>
 * This validator verifies that a Flow represents a structurally
 * executable directed graph before it is activated.
 * </p>
 *
 * <p>
 * The validator currently checks:
 * </p>
 *
 * <ul>
 *     <li>Exactly one START node.</li>
 *     <li>At least one END node.</li>
 *     <li>Every edge has a valid source and target.</li>
 *     <li>Source and target belong to the same Flow.</li>
 *     <li>Self-edges are rejected.</li>
 *     <li>Every node is reachable from START.</li>
 *     <li>END nodes cannot have outgoing edges.</li>
 * </ul>
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FlowGraphValidatorImpl
        implements FlowGraphValidator {

    /**
     * Active record.
     */
    private static final Integer NOT_DELETED = 0;

    /**
     * Flow node repository.
     */
    private final FlowNodeRepository nodeRepository;

    /**
     * Flow edge repository.
     */
    private final FlowEdgeRepository edgeRepository;

    // =========================================================
    // PUBLIC VALIDATION
    // =========================================================

    /**
     * {@inheritDoc}
     */
    @Override
    public void validateForActivation(
            Flow flow) {

        if (flow == null) {

            log.warn(
                    "Flow graph validation failed because Flow is null."
            );

            throw new ConflictException(
                    FlowMessages.NOT_FOUND
            );
        }

        log.info(
                "Starting Flow graph validation. " +
                        "flowPublicId={}",
                flow.getPublicId()
        );

        List<FlowNode> nodes =
                nodeRepository
                        .findByFlowIdAndIsDeletedOrderByIdAsc(
                                flow.getId(),
                                NOT_DELETED
                        );

        validateNodes(
                nodes
        );

        List<FlowEdge> edges =
                edgeRepository
                        .findByFlowIdAndIsDeletedOrderByPriorityAsc(
                                flow.getId(),
                                NOT_DELETED
                        );

        validateEdges(
                flow,
                nodes,
                edges
        );

        validateReachability(
                flow,
                nodes,
                edges
        );

        log.info(
                "Flow graph validation successful. " +
                        "flowPublicId={}, nodeCount={}, edgeCount={}",
                flow.getPublicId(),
                nodes.size(),
                edges.size()
        );
    }

    // =========================================================
    // NODE VALIDATION
    // =========================================================

    /**
     * Validates START and END node requirements.
     *
     * @param nodes active Flow nodes
     */
    private void validateNodes(
            List<FlowNode> nodes) {

        long startCount =
                nodes.stream()
                        .filter(
                                node ->
                                        node.getNodeType()
                                                == FlowNodeType.START
                        )
                        .count();

        long endCount =
                nodes.stream()
                        .filter(
                                node ->
                                        node.getNodeType()
                                                == FlowNodeType.END
                        )
                        .count();

        if (startCount == 0) {

            log.warn(
                    "Flow graph validation failed. " +
                            "START node is missing."
            );

            throw new ConflictException(
                    FlowMessages.START_NODE_REQUIRED
            );
        }

        if (startCount > 1) {

            log.warn(
                    "Flow graph validation failed. " +
                            "Multiple START nodes found. count={}",
                    startCount
            );

            throw new ConflictException(
                    FlowMessages.MULTIPLE_START_NODES
            );
        }

        if (endCount == 0) {

            log.warn(
                    "Flow graph validation failed. " +
                            "END node is missing."
            );

            throw new ConflictException(
                    FlowMessages.END_NODE_REQUIRED
            );
        }
    }

    // =========================================================
    // EDGE VALIDATION
    // =========================================================

    /**
     * Validates Flow edges.
     *
     * @param flow Flow
     * @param nodes active nodes
     * @param edges active edges
     */
    private void validateEdges(
            Flow flow,
            List<FlowNode> nodes,
            List<FlowEdge> edges) {

        Set<Long> nodeIds =
                nodes.stream()
                        .map(
                                FlowNode::getId
                        )
                        .collect(
                                Collectors.toSet()
                        );

        for (FlowEdge edge : edges) {

            if (edge.getSourceNode() == null
                    || edge.getTargetNode() == null) {

                log.warn(
                        "Invalid Flow edge. " +
                                "Source or target is null. " +
                                "flowPublicId={}, edgePublicId={}",
                        flow.getPublicId(),
                        edge.getPublicId()
                );

                throw new ConflictException(
                        FlowMessages.INVALID_TRANSITION
                );
            }

            Long sourceId =
                    edge.getSourceNode()
                            .getId();

            Long targetId =
                    edge.getTargetNode()
                            .getId();

            if (!nodeIds.contains(
                    sourceId
            )) {

                log.warn(
                        "Flow edge source does not belong " +
                                "to the Flow. " +
                                "flowPublicId={}, edgePublicId={}",
                        flow.getPublicId(),
                        edge.getPublicId()
                );

                throw new ConflictException(
                        FlowMessages.EDGE_FLOW_MISMATCH
                );
            }

            if (!nodeIds.contains(
                    targetId
            )) {

                log.warn(
                        "Flow edge target does not belong " +
                                "to the Flow. " +
                                "flowPublicId={}, edgePublicId={}",
                        flow.getPublicId(),
                        edge.getPublicId()
                );

                throw new ConflictException(
                        FlowMessages.EDGE_FLOW_MISMATCH
                );
            }

            if (sourceId.equals(
                    targetId
            )) {

                log.warn(
                        "Self edge detected. " +
                                "flowPublicId={}, edgePublicId={}",
                        flow.getPublicId(),
                        edge.getPublicId()
                );

                throw new ConflictException(
                        FlowMessages.SELF_EDGE_NOT_ALLOWED
                );
            }
        }
    }

    // =========================================================
    // REACHABILITY
    // =========================================================

    /**
     * Ensures every active node can be reached from START.
     *
     * @param flow Flow
     * @param nodes active nodes
     * @param edges active edges
     */
    private void validateReachability(
            Flow flow,
            List<FlowNode> nodes,
            List<FlowEdge> edges) {

        FlowNode startNode =
                nodes.stream()
                        .filter(
                                node ->
                                        node.getNodeType()
                                                == FlowNodeType.START
                        )
                        .findFirst()
                        .orElseThrow(() ->
                                new ConflictException(
                                        FlowMessages.START_NODE_REQUIRED
                                )
                        );

        Set<Long> reachableNodeIds =
                new HashSet<>();

        Set<Long> visitedNodeIds =
                new HashSet<>();

        traverse(
                startNode.getId(),
                edges,
                reachableNodeIds,
                visitedNodeIds
        );

        for (FlowNode node : nodes) {

            if (reachableNodeIds.contains(
                    node.getId()
            )) {
                continue;
            }

            log.warn(
                    "Unreachable Flow node detected. " +
                            "flowPublicId={}, nodePublicId={}, " +
                            "nodeKey={}, nodeType={}",
                    flow.getPublicId(),
                    node.getPublicId(),
                    node.getNodeKey(),
                    node.getNodeType()
            );

            throw new ConflictException(
                    FlowMessages.INVALID_TRANSITION
            );
        }

        validateEndNodes(
                flow,
                nodes,
                edges
        );
    }

    /**
     * Traverses the directed graph from a node.
     *
     * @param currentNodeId current node
     * @param edges active edges
     * @param reachableNodeIds reachable nodes
     * @param visitedNodeIds visited nodes
     */
    private void traverse(
            Long currentNodeId,
            List<FlowEdge> edges,
            Set<Long> reachableNodeIds,
            Set<Long> visitedNodeIds) {

        if (currentNodeId == null) {
            return;
        }

        if (!visitedNodeIds.add(
                currentNodeId
        )) {
            return;
        }

        reachableNodeIds.add(
                currentNodeId
        );

        edges.stream()
                .filter(
                        edge ->
                                edge.getSourceNode() != null
                                        && edge.getSourceNode()
                                        .getId()
                                        .equals(
                                                currentNodeId
                                        )
                )
                .forEach(
                        edge ->
                                traverse(
                                        edge.getTargetNode()
                                                .getId(),
                                        edges,
                                        reachableNodeIds,
                                        visitedNodeIds
                                )
                );
    }

    // =========================================================
    // END VALIDATION
    // =========================================================

    /**
     * Ensures END nodes do not have outgoing transitions.
     *
     * @param flow Flow
     * @param nodes active nodes
     * @param edges active edges
     */
    private void validateEndNodes(
            Flow flow,
            List<FlowNode> nodes,
            List<FlowEdge> edges) {

        Set<Long> endNodeIds =
                nodes.stream()
                        .filter(
                                node ->
                                        node.getNodeType()
                                                == FlowNodeType.END
                        )
                        .map(
                                FlowNode::getId
                        )
                        .collect(
                                Collectors.toSet()
                        );

        boolean endHasOutgoingEdge =
                edges.stream()
                        .anyMatch(
                                edge ->
                                        edge.getSourceNode() != null
                                                && endNodeIds.contains(
                                                edge.getSourceNode()
                                                        .getId()
                                        )
                        );

        if (!endHasOutgoingEdge) {
            return;
        }

        log.warn(
                "END node has outgoing transition. " +
                        "flowPublicId={}",
                flow.getPublicId()
        );

        throw new ConflictException(
                FlowMessages.INVALID_TRANSITION
        );
    }
}
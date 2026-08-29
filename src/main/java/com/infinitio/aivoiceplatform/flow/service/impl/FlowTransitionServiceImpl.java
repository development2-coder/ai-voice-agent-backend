package com.infinitio.aivoiceplatform.flow.service.impl;

import com.infinitio.aivoiceplatform.exception.ResourceNotFoundException;
import com.infinitio.aivoiceplatform.flow.constant.FlowMessages;
import com.infinitio.aivoiceplatform.flow.entity.FlowEdge;
import com.infinitio.aivoiceplatform.flow.entity.FlowNode;
import com.infinitio.aivoiceplatform.flow.repository.FlowEdgeRepository;
import com.infinitio.aivoiceplatform.flow.service.FlowConditionService;
import com.infinitio.aivoiceplatform.flow.service.FlowTransitionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * Default implementation of Flow transition resolution.
 *
 * <p>
 * Transition resolution is port-aware. The selected output port
 * determines which outgoing edges are eligible for execution.
 * </p>
 *
 * <p>
 * This is required for an n8n-style visual Flow Builder where a
 * single node can have multiple output branches.
 * </p>
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FlowTransitionServiceImpl
        implements FlowTransitionService {

    /**
     * Active edge flag.
     */
    private static final Integer NOT_DELETED = 0;

    /**
     * Flow edge repository.
     */
    private final FlowEdgeRepository edgeRepository;

    /**
     * Flow condition service.
     */
    private final FlowConditionService conditionService;

    // =========================================================
    // PORT-AWARE TRANSITION
    // =========================================================

    /**
     * {@inheritDoc}
     */
    @Override
    public FlowNode getNextNode(
            FlowNode currentNode,
            String outputPort,
            Map<String, Object> context) {

        validateCurrentNode(
                currentNode
        );

        log.debug(
                "Resolving port-aware Flow transition. " +
                        "nodeKey={}, outputPort={}",
                currentNode.getNodeKey(),
                outputPort
        );

        if (outputPort == null
                || outputPort.isBlank()) {

            log.debug(
                    "No output port supplied. " +
                            "Falling back to condition-based transition. " +
                            "nodeKey={}",
                    currentNode.getNodeKey()
            );

            return getNextNode(
                    currentNode,
                    context
            );
        }

        List<FlowEdge> edges =
                edgeRepository
                        .findBySourceNodeIdAndSourcePortAndIsDeletedOrderByPriorityAsc(
                                currentNode.getId(),
                                outputPort,
                                NOT_DELETED
                        );

        if (edges.isEmpty()) {

            log.warn(
                    "No outgoing edge found for output port. " +
                            "nodeKey={}, outputPort={}",
                    currentNode.getNodeKey(),
                    outputPort
            );

            throw new ResourceNotFoundException(
                    FlowMessages.INVALID_TRANSITION
            );
        }

        FlowEdge selectedEdge =
                selectEdge(
                        edges,
                        context
                );

        return resolveTargetNode(
                currentNode,
                outputPort,
                selectedEdge
        );
    }

    // =========================================================
    // DEFAULT TRANSITION
    // =========================================================

    /**
     * {@inheritDoc}
     *
     * <p>
     * Used by nodes that don't explicitly select an output port.
     * </p>
     */
    @Override
    public FlowNode getNextNode(
            FlowNode currentNode,
            Map<String, Object> context) {

        validateCurrentNode(
                currentNode
        );

        log.debug(
                "Finding default Flow transition. nodeKey={}",
                currentNode.getNodeKey()
        );

        List<FlowEdge> edges =
                edgeRepository
                        .findBySourceNodeIdAndIsDeletedOrderByPriorityAsc(
                                currentNode.getId(),
                                NOT_DELETED
                        );

        if (edges.isEmpty()) {

            log.warn(
                    "No outgoing edge found. nodeKey={}",
                    currentNode.getNodeKey()
            );

            throw new ResourceNotFoundException(
                    FlowMessages.INVALID_TRANSITION
            );
        }

        FlowEdge selectedEdge =
                selectEdge(
                        edges,
                        context
                );

        return resolveTargetNode(
                currentNode,
                null,
                selectedEdge
        );
    }

    // =========================================================
    // EDGE SELECTION
    // =========================================================

    /**
     * Selects the matching edge using the existing condition
     * service.
     *
     * @param edges eligible edges
     * @param context execution context
     * @return selected edge
     */
    private FlowEdge selectEdge(
            List<FlowEdge> edges,
            Map<String, Object> context) {

        FlowEdge selectedEdge =
                conditionService.findMatchingEdge(
                        edges,
                        context
                );

        if (selectedEdge != null) {
            return selectedEdge;
        }

        log.warn(
                "No matching Flow transition found. " +
                        "edgeCount={}",
                edges.size()
        );

        throw new ResourceNotFoundException(
                FlowMessages.INVALID_TRANSITION
        );
    }

    // =========================================================
    // TARGET RESOLUTION
    // =========================================================

    /**
     * Resolves the target node from the selected edge.
     *
     * @param currentNode current node
     * @param outputPort selected output port
     * @param selectedEdge selected edge
     * @return target node
     */
    private FlowNode resolveTargetNode(
            FlowNode currentNode,
            String outputPort,
            FlowEdge selectedEdge) {

        if (selectedEdge == null) {

            log.error(
                    "Selected Flow edge is null. " +
                            "nodeKey={}, outputPort={}",
                    currentNode.getNodeKey(),
                    outputPort
            );

            throw new ResourceNotFoundException(
                    FlowMessages.INVALID_TRANSITION
            );
        }

        FlowNode nextNode =
                selectedEdge.getTargetNode();

        if (nextNode == null) {

            log.error(
                    "Target node missing from Flow edge. " +
                            "sourceNode={}, outputPort={}, " +
                            "edgePublicId={}",
                    currentNode.getNodeKey(),
                    outputPort,
                    selectedEdge.getPublicId()
            );

            throw new ResourceNotFoundException(
                    FlowMessages.INVALID_TRANSITION
            );
        }

        log.info(
                "Flow transition selected. " +
                        "source={}, sourcePort={}, " +
                        "target={}, targetPort={}, " +
                        "condition={}",
                currentNode.getNodeKey(),
                selectedEdge.getSourcePort(),
                nextNode.getNodeKey(),
                selectedEdge.getTargetPort(),
                selectedEdge.getConditionExpression()
        );

        return nextNode;
    }

    // =========================================================
    // VALIDATION
    // =========================================================

    /**
     * Validates the current execution node.
     *
     * @param currentNode current node
     */
    private void validateCurrentNode(
            FlowNode currentNode) {

        if (currentNode != null) {
            return;
        }

        log.error(
                "Cannot resolve Flow transition because " +
                        "current node is null."
        );

        throw new IllegalArgumentException(
                "Current flow node cannot be null."
        );
    }
}
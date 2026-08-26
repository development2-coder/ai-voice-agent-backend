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

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FlowTransitionServiceImpl
        implements FlowTransitionService {

    private final FlowEdgeRepository edgeRepository;

    private final FlowConditionService conditionService;

    @Override
    public FlowNode getNextNode(
            FlowNode currentNode,
            Map<String, Object> context) {

        if (currentNode == null) {

            throw new IllegalArgumentException(
                    "Current flow node cannot be null."
            );
        }

        log.debug(
                "Finding next node. currentNode={}",
                currentNode.getNodeKey()
        );

        List<FlowEdge> edges =
                edgeRepository
                        .findBySourceNodeIdOrderByPriorityAsc(
                                currentNode.getId()
                        );

        if (edges.isEmpty()) {

            log.error(
                    "No outgoing edge found. node={}",
                    currentNode.getNodeKey()
            );

            throw new ResourceNotFoundException(
                    FlowMessages.INVALID_TRANSITION
            );
        }

        FlowEdge selectedEdge =
                conditionService.findMatchingEdge(
                        edges,
                        context
                );

        if (selectedEdge == null) {

            log.error(
                    "No matching transition found. node={}",
                    currentNode.getNodeKey()
            );

            throw new ResourceNotFoundException(
                    FlowMessages.INVALID_TRANSITION
            );
        }

        FlowNode nextNode =
                selectedEdge.getTargetNode();

        if (nextNode == null) {

            throw new ResourceNotFoundException(
                    "Target node not found for flow transition."
            );
        }

        log.info(
                "Flow transition selected. source={}, target={}, condition={}",
                currentNode.getNodeKey(),
                nextNode.getNodeKey(),
                selectedEdge.getConditionExpression()
        );

        return nextNode;
    }
}
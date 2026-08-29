package com.infinitio.aivoiceplatform.flow.service;

import com.infinitio.aivoiceplatform.flow.entity.FlowNode;

import java.util.Map;

/**
 * Service responsible for resolving the next node in a Flow.
 *
 * <p>
 * Flow transitions are resolved using the output port selected
 * by the currently executing node. This allows the Flow Builder
 * to support n8n-style branching such as:
 * </p>
 *
 * <pre>
 * CONDITION
 *    ├── true  -> MESSAGE
 *    └── false -> API
 * </pre>
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
public interface FlowTransitionService {

    /**
     * Resolves the next node using the selected output port.
     *
     * @param currentNode currently executing node
     * @param outputPort selected output port
     * @param context current Flow execution context
     * @return next Flow node
     */
    FlowNode getNextNode(
            FlowNode currentNode,
            String outputPort,
            Map<String, Object> context
    );

    /**
     * Resolves the next node when the node does not explicitly
     * select an output port.
     *
     * <p>
     * This is retained for simple sequential nodes and for
     * backward compatibility with existing execution code.
     * </p>
     *
     * @param currentNode currently executing node
     * @param context current Flow execution context
     * @return next Flow node
     */
    FlowNode getNextNode(
            FlowNode currentNode,
            Map<String, Object> context
    );
}
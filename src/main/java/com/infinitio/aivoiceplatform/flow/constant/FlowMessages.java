package com.infinitio.aivoiceplatform.flow.constant;

/**
 * Message constants used by the Flow module.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
public final class FlowMessages {

    private FlowMessages() {
    }

    public static final String CREATED =
            "Flow created successfully.";

    public static final String UPDATED =
            "Flow updated successfully.";

    public static final String DELETED =
            "Flow deleted successfully.";

    public static final String ACTIVATED =
            "Flow activated successfully.";

    public static final String DEACTIVATED =
            "Flow deactivated successfully.";

    public static final String NOT_FOUND =
            "Flow not found.";

    public static final String NODE_NOT_FOUND =
            "Flow node not found.";

    public static final String EDGE_NOT_FOUND =
            "Flow edge not found.";

    public static final String NODE_CREATED =
            "Flow node created successfully.";

    public static final String NODE_UPDATED =
            "Flow node updated successfully.";

    public static final String NODE_DELETED =
            "Flow node deleted successfully.";

    public static final String EDGE_CREATED =
            "Flow edge created successfully.";

    public static final String EDGE_DELETED =
            "Flow edge deleted successfully.";

    public static final String EXECUTION_STARTED =
            "Flow execution started successfully.";

    public static final String EXECUTION_CONTINUED =
            "Flow execution continued successfully.";

    public static final String EXECUTION_NOT_FOUND =
            "Flow execution not found.";

    public static final String START_NODE_REQUIRED =
            "Flow must contain a START node.";

    public static final String END_NODE_REQUIRED =
            "Flow must contain an END node.";

    public static final String MULTIPLE_START_NODES =
            "Flow can contain only one START node.";

    public static final String INVALID_TRANSITION =
            "No valid transition found.";

    public static final String FLOW_NOT_ACTIVE =
            "Flow is not active.";

    public static final String INVALID_CONFIGURATION =
            "Invalid node configuration.";

    public static final String EXECUTION_FAILED =
            "Flow execution failed.";

    public static final String SOURCE_PORT_NOT_FOUND =
            "Source output port not found for node.";

    public static final String TARGET_PORT_NOT_FOUND =
            "Target input port not found for node.";

    public static final String INVALID_SOURCE_PORT =
            "Invalid source output port.";

    public static final String INVALID_TARGET_PORT =
            "Invalid target input port.";

    public static final String DUPLICATE_EDGE =
            "Flow edge already exists.";

    public static final String SELF_EDGE_NOT_ALLOWED =
            "Source and target node cannot be the same.";

    public static final String EDGE_FLOW_MISMATCH =
            "Source and target nodes must belong to the same flow.";

    /**
     * Node key already exists inside the Flow.
     */
    public static final String NODE_KEY_ALREADY_EXISTS =
            "Node key already exists in flow.";

    /**
     * START node cannot be converted to another node type.
     */
    public static final String START_NODE_TYPE_CANNOT_BE_CHANGED =
            "START node type cannot be changed.";

    /**
     * START node cannot be deleted.
     */
    public static final String START_NODE_CANNOT_BE_DELETED =
            "START node cannot be deleted.";
}
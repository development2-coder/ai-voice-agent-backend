package com.infinitio.aivoiceplatform.flow.dto.response;

import com.infinitio.aivoiceplatform.flow.constant.FlowNodeType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Defines the metadata required by the Flow Builder frontend
 * to render and configure a Flow node.
 *
 * <p>
 * The structure is inspired by the n8n node model where a node
 * exposes display information, configuration parameters and
 * connection capabilities.
 * </p>
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FlowNodeDefinitionResponse {

    /**
     * Internal node type.
     */
    private FlowNodeType nodeType;

    /**
     * Display name shown in the node library.
     */
    private String displayName;

    /**
     * Node category shown in the frontend library.
     */
    private String category;

    /**
     * Short description of the node.
     */
    private String description;

    /**
     * Icon identifier used by the frontend.
     */
    private String icon;

    /**
     * Whether the node can be manually added by the user.
     */
    private Boolean userCreatable;

    /**
     * Whether this node can be the first node.
     */
    private Boolean startNode;

    /**
     * Whether this node can terminate a flow.
     */
    private Boolean endNode;

    /**
     * Whether this node supports incoming connections.
     */
    private Boolean inputSupported;

    /**
     * Whether this node supports outgoing connections.
     */
    private Boolean outputSupported;

    /**
     * Whether multiple outgoing connections are allowed.
     */
    private Boolean multipleOutputs;

    /**
     * JSON configuration schema used by the frontend
     * to render the node configuration panel.
     */
    private String configurationSchema;

    /**
     * Input ports exposed by the node.
     */
    private List<FlowNodePortResponse> inputPorts;

    /**
     * Output ports exposed by the node.
     */
    private List<FlowNodePortResponse> outputPorts;
}
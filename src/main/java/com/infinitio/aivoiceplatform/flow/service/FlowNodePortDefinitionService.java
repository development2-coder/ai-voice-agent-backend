package com.infinitio.aivoiceplatform.flow.service;

import com.infinitio.aivoiceplatform.flow.constant.FlowNodeType;
import com.infinitio.aivoiceplatform.flow.dto.response.FlowNodePortResponse;

import java.util.List;

/**
 * Provides connection-port definitions for Flow node types.
 *
 * <p>
 * The Flow Builder uses these definitions to render input and
 * output connection handles dynamically.
 * </p>
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
public interface FlowNodePortDefinitionService {

    /**
     * Gets input ports for a node type.
     *
     * @param nodeType node type
     * @return input port definitions
     */
    List<FlowNodePortResponse> getInputPorts(
            FlowNodeType nodeType
    );

    /**
     * Gets output ports for a node type.
     *
     * @param nodeType node type
     * @return output port definitions
     */
    List<FlowNodePortResponse> getOutputPorts(
            FlowNodeType nodeType
    );
}
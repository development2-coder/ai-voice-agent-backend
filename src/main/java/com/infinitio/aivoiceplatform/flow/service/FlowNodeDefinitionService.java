package com.infinitio.aivoiceplatform.flow.service;

import com.infinitio.aivoiceplatform.flow.constant.FlowNodeType;
import com.infinitio.aivoiceplatform.flow.dto.response.FlowNodeDefinitionResponse;

import java.util.List;

/**
 * Provides the node library metadata used by the Flow Builder.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
public interface FlowNodeDefinitionService {

    /**
     * Returns all available Flow node definitions.
     *
     * @return node definitions
     */
    List<FlowNodeDefinitionResponse> getAll();

    /**
     * Returns one Flow node definition.
     *
     * @param nodeType node type
     * @return node definition
     */
    FlowNodeDefinitionResponse getByType(
            FlowNodeType nodeType
    );
}
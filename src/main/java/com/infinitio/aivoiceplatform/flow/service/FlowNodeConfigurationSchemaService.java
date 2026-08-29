package com.infinitio.aivoiceplatform.flow.service;

import com.infinitio.aivoiceplatform.flow.constant.FlowNodeType;

/**
 * Provides configuration schemas for Flow node types.
 *
 * <p>
 * The frontend uses these schemas to dynamically render the
 * configuration panel for a selected node.
 * </p>
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
public interface FlowNodeConfigurationSchemaService {

    /**
     * Gets the configuration schema for a node type.
     *
     * @param nodeType node type
     * @return JSON configuration schema
     */
    String getSchema(
            FlowNodeType nodeType
    );
}
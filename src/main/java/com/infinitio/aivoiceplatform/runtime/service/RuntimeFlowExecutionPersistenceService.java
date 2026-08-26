package com.infinitio.aivoiceplatform.runtime.service;

import com.infinitio.aivoiceplatform.runtime.context.RuntimeContext;

/**
 * Provides persistence operations for runtime state associated
 * with a flow execution.
 *
 * <p>
 * The runtime state is persisted through the existing
 * flow execution record instead of using Redis or a separate
 * runtime-state table.
 * </p>
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
public interface RuntimeFlowExecutionPersistenceService {

    /**
     * Persists runtime context for a flow execution.
     *
     * @param executionPublicId public identifier of the flow execution
     * @param context runtime context
     */
    void updateRuntimeContext(
            String executionPublicId,
            RuntimeContext context
    );

    /**
     * Retrieves runtime context for a flow execution.
     *
     * @param executionPublicId public identifier of the flow execution
     * @return runtime context
     */
    RuntimeContext getRuntimeContext(
            String executionPublicId
    );

    /**
     * Updates the current flow node for a flow execution.
     *
     * @param executionPublicId public identifier of the flow execution
     * @param nodeId current node identifier
     */
    void updateCurrentNode(
            String executionPublicId,
            Long nodeId
    );
}
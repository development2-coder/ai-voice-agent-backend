package com.infinitio.aivoiceplatform.flow.service;

import com.infinitio.aivoiceplatform.flow.dto.response.FlowExecutionResult;
import com.infinitio.aivoiceplatform.flow.entity.FlowExecution;
import com.infinitio.aivoiceplatform.flow.entity.FlowNode;

import java.util.Map;

/**
 * Executes Flow nodes and handles immediate runtime transitions.
 *
 * <p>
 * This service keeps runtime execution separate from the execution
 * continuation/lifecycle service.
 * </p>
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
public interface FlowExecutionRuntimeService {

    /**
     * Executes the supplied node and continues through
     * immediately executable nodes.
     *
     * @param execution current Flow execution
     * @param node node to execute
     * @param context execution context
     * @return execution result
     */
    FlowExecutionResult execute(
            FlowExecution execution,
            FlowNode node,
            Map<String, Object> context
    );
}
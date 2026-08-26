package com.infinitio.aivoiceplatform.runtime.service.impl;

import com.infinitio.aivoiceplatform.flow.entity.FlowExecution;
import com.infinitio.aivoiceplatform.flow.repository.FlowExecutionRepository;
import com.infinitio.aivoiceplatform.runtime.context.RuntimeContext;
import com.infinitio.aivoiceplatform.runtime.service.RuntimeContextService;
import com.infinitio.aivoiceplatform.runtime.service.RuntimeFlowExecutionPersistenceService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * MySQL-backed implementation for persistence of runtime state
 * associated with a flow execution.
 *
 * <p>
 * Runtime state is stored in the existing flow_executions table.
 * Redis is not used by this service.
 * </p>
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RuntimeFlowExecutionPersistenceServiceImpl
        implements RuntimeFlowExecutionPersistenceService {

    private final FlowExecutionRepository flowExecutionRepository;

    private final RuntimeContextService runtimeContextService;

    /**
     * Persists runtime context for a flow execution.
     *
     * @param executionPublicId public identifier of the flow execution
     * @param context runtime context
     */
    @Override
    @Transactional
    public void updateRuntimeContext(
            String executionPublicId,
            RuntimeContext context) {

        FlowExecution flowExecution =
                getFlowExecution(executionPublicId);

        flowExecution.setContextData(
                runtimeContextService.serialize(
                        context
                )
        );

        flowExecutionRepository.save(
                flowExecution
        );

        log.debug(
                "Runtime context persisted successfully. " +
                        "executionPublicId={}",
                executionPublicId
        );
    }

    /**
     * Retrieves runtime context for a flow execution.
     *
     * @param executionPublicId public identifier of the flow execution
     * @return runtime context
     */
    @Override
    @Transactional(readOnly = true)
    public RuntimeContext getRuntimeContext(
            String executionPublicId) {

        FlowExecution flowExecution =
                getFlowExecution(executionPublicId);

        return runtimeContextService.deserialize(
                flowExecution.getContextData()
        );
    }

    /**
     * Updates the current flow node for a flow execution.
     *
     * @param executionPublicId public identifier of the flow execution
     * @param nodeId current flow node identifier
     */
    @Override
    @Transactional
    public void updateCurrentNode(
            String executionPublicId,
            Long nodeId) {

        FlowExecution flowExecution =
                getFlowExecution(executionPublicId);

        flowExecution.setCurrentNodeId(
                nodeId
        );

        flowExecutionRepository.save(
                flowExecution
        );

        log.debug(
                "Current flow node persisted successfully. " +
                        "executionPublicId={}, nodeId={}",
                executionPublicId,
                nodeId
        );
    }

    /**
     * Retrieves a flow execution by its public identifier.
     *
     * @param executionPublicId public identifier
     * @return flow execution
     */
    private FlowExecution getFlowExecution(
            String executionPublicId) {

        if (executionPublicId == null
                || executionPublicId.isBlank()) {

            throw new IllegalArgumentException(
                    "Flow execution public id is required."
            );
        }

        return flowExecutionRepository
                .findByPublicId(
                        executionPublicId
                )
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Flow execution not found."
                        )
                );
    }
}
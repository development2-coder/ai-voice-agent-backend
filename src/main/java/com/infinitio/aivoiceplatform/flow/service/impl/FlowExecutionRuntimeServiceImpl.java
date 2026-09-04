package com.infinitio.aivoiceplatform.flow.service.impl;

import com.infinitio.aivoiceplatform.exception.ResourceNotFoundException;
import com.infinitio.aivoiceplatform.flow.constant.FlowExecutionContextKeys;
import com.infinitio.aivoiceplatform.flow.constant.FlowMessages;
import com.infinitio.aivoiceplatform.flow.dto.response.FlowExecutionResult;
import com.infinitio.aivoiceplatform.flow.dto.response.FlowNodeExecutionResult;
import com.infinitio.aivoiceplatform.flow.entity.FlowExecution;
import com.infinitio.aivoiceplatform.flow.entity.FlowNode;
import com.infinitio.aivoiceplatform.flow.service.FlowExecutionRuntimeService;
import com.infinitio.aivoiceplatform.flow.service.FlowNodeExecutionService;
import com.infinitio.aivoiceplatform.flow.service.FlowResultService;
import com.infinitio.aivoiceplatform.flow.service.FlowTransitionService;
import com.infinitio.aivoiceplatform.telephony.dto.request.TransferCallRequestDto;
import com.infinitio.aivoiceplatform.telephony.service.TelephonyTransferExecutionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

/**
 * Default Flow runtime execution implementation.
 *
 * <p>
 * Executes one node at a time and follows immediate transitions
 * iteratively. Waiting, completed and transferred states stop
 * execution and are converted into the public execution result.
 * </p>
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class FlowExecutionRuntimeServiceImpl
        implements FlowExecutionRuntimeService {

    /**
     * Maximum number of immediate node executions allowed
     * during one runtime call.
     *
     * <p>
     * This protects the application from an accidental cyclic
     * Flow causing an endless runtime loop.
     * </p>
     */
    private static final int MAX_RUNTIME_STEPS = 500;

    private final FlowNodeExecutionService nodeExecutionService;

    private final FlowTransitionService transitionService;

    private final FlowResultService resultService;

    private final TelephonyTransferExecutionService
            telephonyTransferExecutionService;

    /**
     * {@inheritDoc}
     */
    @Override
    public FlowExecutionResult execute(
            FlowExecution execution,
            FlowNode node,
            Map<String, Object> context) {

        validateArguments(
                execution,
                node,
                context
        );

        FlowNode currentNode =
                node;

        int runtimeSteps = 0;

        while (currentNode != null) {

            runtimeSteps++;

            if (runtimeSteps > MAX_RUNTIME_STEPS) {

                log.error(
                        "Maximum Flow runtime steps exceeded. " +
                                "executionPublicId={}, maxSteps={}",
                        execution.getPublicId(),
                        MAX_RUNTIME_STEPS
                );

                throw new IllegalStateException(
                        FlowMessages.EXECUTION_FAILED
                );
            }

            log.info(
                    "Executing Flow runtime node. " +
                            "executionPublicId={}, nodeKey={}, " +
                            "nodeType={}, step={}",
                    execution.getPublicId(),
                    currentNode.getNodeKey(),
                    currentNode.getNodeType(),
                    runtimeSteps
            );

            FlowNodeExecutionResult nodeResult =
                    nodeExecutionService.execute(
                            execution,
                            currentNode,
                            context
                    );

            executeTransferIfRequired(
                    nodeResult,
                    context,
                    execution
            );

            validateNodeResult(
                    nodeResult,
                    execution,
                    currentNode
            );

            if (shouldStop(
                    nodeResult
            )) {

                log.info(
                        "Flow runtime stopped at node. " +
                                "executionPublicId={}, nodeKey={}, " +
                                "waiting={}, completed={}, transferred={}",
                        execution.getPublicId(),
                        currentNode.getNodeKey(),
                        nodeResult.isWaiting(),
                        nodeResult.isCompleted(),
                        nodeResult.isTransferred()
                );

                return resultService.buildResult(
                        execution,
                        currentNode,
                        nodeResult
                );
            }

            String outputPort =
                    getSelectedOutputPort(
                            context
                    );

            FlowNode nextNode =
                    resolveNextNode(
                            currentNode,
                            outputPort,
                            context
                    );

            clearSelectedOutputPort(
                    context
            );

            currentNode =
                    nextNode;
        }

        log.error(
                "Flow runtime reached a null next node. " +
                        "executionPublicId={}",
                execution.getPublicId()
        );

        throw new ResourceNotFoundException(
                FlowMessages.INVALID_TRANSITION
        );
    }

    // =========================================================
    // VALIDATION
    // =========================================================

    private void validateArguments(
            FlowExecution execution,
            FlowNode node,
            Map<String, Object> context) {

        if (execution == null) {

            throw new IllegalArgumentException(
                    "Flow execution cannot be null."
            );
        }

        if (node == null) {

            throw new ResourceNotFoundException(
                    FlowMessages.NODE_NOT_FOUND
            );
        }

        if (context == null) {

            throw new IllegalArgumentException(
                    "Flow execution context cannot be null."
            );
        }
    }

    private void validateNodeResult(
            FlowNodeExecutionResult result,
            FlowExecution execution,
            FlowNode node) {

        if (result != null) {
            return;
        }

        log.error(
                "Flow node returned null execution result. " +
                        "executionPublicId={}, nodeKey={}",
                execution.getPublicId(),
                node.getNodeKey()
        );

        throw new IllegalStateException(
                FlowMessages.EXECUTION_FAILED
        );
    }

    private boolean shouldStop(
            FlowNodeExecutionResult result) {

        return result.isWaiting()
                || result.isCompleted()
                || result.isTransferred();
    }

    // =========================================================
    // TRANSITION
    // =========================================================

    private FlowNode resolveNextNode(
            FlowNode currentNode,
            String outputPort,
            Map<String, Object> context) {

        if (outputPort == null
                || outputPort.isBlank()) {

            log.debug(
                    "Resolving default Flow transition. " +
                            "nodeKey={}",
                    currentNode.getNodeKey()
            );

            return transitionService.getNextNode(
                    currentNode,
                    context
            );
        }

        log.debug(
                "Resolving port-aware Flow transition. " +
                        "nodeKey={}, outputPort={}",
                currentNode.getNodeKey(),
                outputPort
        );

        return transitionService.getNextNode(
                currentNode,
                outputPort,
                context
        );
    }

    private String getSelectedOutputPort(
            Map<String, Object> context) {

        Object value =
                context.get(
                        FlowExecutionContextKeys
                                .SELECTED_OUTPUT_PORT
                );

        if (value == null) {
            return null;
        }

        String outputPort =
                String.valueOf(
                        value
                ).trim();

        return outputPort.isBlank()
                ? null
                : outputPort;
    }

    private void clearSelectedOutputPort(
            Map<String, Object> context) {

        context.remove(
                FlowExecutionContextKeys
                        .SELECTED_OUTPUT_PORT
        );
    }

    /**
     * Executes the provider-level transfer when the current Flow
     * node has produced a transfer result.
     *
     * <p>
     * The Flow node remains responsible for generating the
     * provider-neutral transfer instruction. This method converts
     * that instruction into a provider request and delegates the
     * actual telephony operation to the provider execution service.
     * </p>
     *
     * @param nodeResult Flow node execution result
     * @param context Flow runtime context
     * @param execution current Flow execution
     */
    private void executeTransferIfRequired(
            FlowNodeExecutionResult nodeResult,
            Map<String, Object> context,
            FlowExecution execution) {

        if (nodeResult == null
                || !nodeResult.isTransferred()) {

            return;
        }

        String providerCode =
                resolveContextValue(
                        context,
                        FlowExecutionContextKeys.TELEPHONY_PROVIDER
                );

        String providerCallId =
                resolveContextValue(
                        context,
                        FlowExecutionContextKeys.PROVIDER_CALL_ID
                );

        String destination =
                resolveContextValue(
                        context,
                        FlowExecutionContextKeys.TRANSFER_DESTINATION
                );

        log.info(
                "Flow requested telephony transfer. "
                        + "executionPublicId={}, provider={}, "
                        + "providerCallId={}, destination={}",
                execution.getPublicId(),
                providerCode,
                providerCallId,
                destination
        );

        TransferCallRequestDto request =
                TransferCallRequestDto.builder()
                        .providerCallId(
                                providerCallId
                        )
                        .destination(
                                destination
                        )
                        .build();

        telephonyTransferExecutionService.executeTransfer(
                providerCode,
                request
        );

        log.info(
                "Flow transfer delegated to telephony provider. "
                        + "executionPublicId={}, provider={}",
                execution.getPublicId(),
                providerCode
        );
    }

    /**
     * Resolves a string value from the Flow runtime context.
     *
     * @param context Flow runtime context
     * @param key context key
     * @return resolved value or {@code null}
     */
    private String resolveContextValue(
            Map<String, Object> context,
            String key) {

        Object value =
                context.get(
                        key
                );

        if (value == null) {
            return null;
        }

        String resolved =
                String.valueOf(
                        value
                ).trim();

        return resolved.isBlank()
                ? null
                : resolved;
    }
}
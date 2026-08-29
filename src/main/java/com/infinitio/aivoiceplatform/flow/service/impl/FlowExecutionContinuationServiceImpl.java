package com.infinitio.aivoiceplatform.flow.service.impl;

import com.infinitio.aivoiceplatform.exception.ResourceNotFoundException;
import com.infinitio.aivoiceplatform.flow.constant.FlowExecutionContextKeys;
import com.infinitio.aivoiceplatform.flow.constant.FlowExecutionStatus;
import com.infinitio.aivoiceplatform.flow.constant.FlowMessages;
import com.infinitio.aivoiceplatform.flow.dto.request.ContinueAiResponseRequest;
import com.infinitio.aivoiceplatform.flow.dto.request.ContinueApiResponseRequest;
import com.infinitio.aivoiceplatform.flow.dto.request.ContinueFlowExecutionRequest;
import com.infinitio.aivoiceplatform.flow.dto.response.FlowExecutionResult;
import com.infinitio.aivoiceplatform.flow.entity.FlowExecution;
import com.infinitio.aivoiceplatform.flow.entity.FlowNode;
import com.infinitio.aivoiceplatform.flow.repository.FlowExecutionRepository;
import com.infinitio.aivoiceplatform.flow.repository.FlowNodeRepository;
import com.infinitio.aivoiceplatform.flow.service.FlowContextService;
import com.infinitio.aivoiceplatform.flow.service.FlowExecutionContinuationService;
import com.infinitio.aivoiceplatform.flow.service.FlowExecutionRuntimeService;
import com.infinitio.aivoiceplatform.flow.service.FlowTransitionService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Default implementation for Flow execution continuation.
 *
 * <p>
 * This service handles continuation requests and waiting-state
 * processing. Actual node execution is delegated to
 * {@link FlowExecutionRuntimeService}.
 * </p>
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class FlowExecutionContinuationServiceImpl
        implements FlowExecutionContinuationService {

    private final FlowExecutionRepository executionRepository;

    private final FlowNodeRepository nodeRepository;

    private final FlowContextService flowContextService;

    private final FlowTransitionService transitionService;

    private final FlowExecutionRuntimeService runtimeService;

    // =========================================================
    // NORMAL CONTINUATION
    // =========================================================

    /**
     * {@inheritDoc}
     */
    @Override
    public FlowExecutionResult continueExecution(
            ContinueFlowExecutionRequest request) {

        log.info(
                "Continuing Flow execution. executionPublicId={}",
                request.getExecutionPublicId()
        );

        FlowExecution execution =
                findExecution(
                        request.getExecutionPublicId()
                );

        validateExecutionState(
                execution
        );

        Map<String, Object> context =
                flowContextService.readContext(
                        execution.getContextData()
                );

        mergeRequestContext(
                context,
                request.getContext()
        );

        processUserInput(
                request.getUserInput(),
                context
        );

        FlowNode currentNode =
                findCurrentNode(
                        execution
                );

        if (isWaitingStatus(
                execution.getStatus()
        )) {

            clearWaitingStatus(
                    execution,
                    context
            );

            FlowNode nextNode =
                    resolveNextNode(
                            currentNode,
                            context
                    );

            return executeFromNode(
                    execution,
                    nextNode,
                    context
            );
        }

        execution.setStatus(
                FlowExecutionStatus.RUNNING
        );

        persistContext(
                execution,
                context
        );

        return executeFromNode(
                execution,
                currentNode,
                context
        );
    }

    // =========================================================
    // API CONTINUATION
    // =========================================================

    /**
     * {@inheritDoc}
     */
    @Override
    public FlowExecutionResult continueWithApiResponse(
            ContinueApiResponseRequest request) {

        log.info(
                "Continuing Flow execution with API response. " +
                        "executionPublicId={}",
                request.getExecutionPublicId()
        );

        FlowExecution execution =
                findExecution(
                        request.getExecutionPublicId()
                );

        validateWaitingStatus(
                execution,
                FlowExecutionStatus.WAITING_FOR_API,
                "API"
        );

        Map<String, Object> context =
                flowContextService.readContext(
                        execution.getContextData()
                );

        storeResponse(
                context,
                FlowExecutionContextKeys.WAITING_API_VARIABLE,
                request.getResponse(),
                "lastApiResponse"
        );

        mergeRequestContext(
                context,
                request.getContext()
        );

        clearApiWaitingContext(
                context
        );

        execution.setStatus(
                FlowExecutionStatus.RUNNING
        );

        persistContext(
                execution,
                context
        );

        FlowNode currentNode =
                findCurrentNode(
                        execution
                );

        FlowNode nextNode =
                resolveNextNode(
                        currentNode,
                        context
                );

        return executeFromNode(
                execution,
                nextNode,
                context
        );
    }

    // =========================================================
    // AI CONTINUATION
    // =========================================================

    /**
     * {@inheritDoc}
     */
    @Override
    public FlowExecutionResult continueWithAiResponse(
            ContinueAiResponseRequest request) {

        log.info(
                "Continuing Flow execution with AI response. " +
                        "executionPublicId={}",
                request.getExecutionPublicId()
        );

        FlowExecution execution =
                findExecution(
                        request.getExecutionPublicId()
                );

        validateWaitingStatus(
                execution,
                FlowExecutionStatus.WAITING_FOR_AI,
                "AI"
        );

        Map<String, Object> context =
                flowContextService.readContext(
                        execution.getContextData()
                );

        storeResponse(
                context,
                FlowExecutionContextKeys.WAITING_AI_VARIABLE,
                request.getResponse(),
                "lastAiResponse"
        );

        mergeRequestContext(
                context,
                request.getContext()
        );

        clearAiWaitingContext(
                context
        );

        execution.setStatus(
                FlowExecutionStatus.RUNNING
        );

        persistContext(
                execution,
                context
        );

        FlowNode currentNode =
                findCurrentNode(
                        execution
                );

        FlowNode nextNode =
                resolveNextNode(
                        currentNode,
                        context
                );

        return executeFromNode(
                execution,
                nextNode,
                context
        );
    }

    // =========================================================
    // RUNTIME
    // =========================================================

    /**
     * Delegates actual node execution to the runtime service.
     *
     * @param execution current execution
     * @param node node to execute
     * @param context execution context
     * @return execution result
     */
    private FlowExecutionResult executeFromNode(
            FlowExecution execution,
            FlowNode node,
            Map<String, Object> context) {

        if (node == null) {

            log.error(
                    "Cannot continue Flow because next node is null. " +
                            "executionPublicId={}",
                    execution.getPublicId()
            );

            throw new ResourceNotFoundException(
                    FlowMessages.NODE_NOT_FOUND
            );
        }

        log.debug(
                "Delegating Flow node execution to runtime service. " +
                        "executionPublicId={}, nodeKey={}, nodeType={}",
                execution.getPublicId(),
                node.getNodeKey(),
                node.getNodeType()
        );

        return runtimeService.execute(
                execution,
                node,
                context
        );
    }

    // =========================================================
    // TRANSITION
    // =========================================================

    /**
     * Resolves the next node after a waiting state has been
     * completed.
     *
     * <p>
     * The selected output port is read from the execution
     * context. If no explicit port was selected, the default
     * transition mechanism is used.
     * </p>
     */
    private FlowNode resolveNextNode(
            FlowNode currentNode,
            Map<String, Object> context) {

        if (currentNode == null) {

            throw new ResourceNotFoundException(
                    FlowMessages.NODE_NOT_FOUND
            );
        }

        String outputPort =
                getSelectedOutputPort(
                        context
                );

        if (outputPort == null) {

            log.debug(
                    "Resolving default transition after waiting state. " +
                            "nodeKey={}",
                    currentNode.getNodeKey()
            );

            return transitionService.getNextNode(
                    currentNode,
                    context
            );
        }

        log.debug(
                "Resolving port-aware transition after waiting state. " +
                        "nodeKey={}, outputPort={}",
                currentNode.getNodeKey(),
                outputPort
        );

        FlowNode nextNode =
                transitionService.getNextNode(
                        currentNode,
                        outputPort,
                        context
                );

        context.remove(
                FlowExecutionContextKeys
                        .SELECTED_OUTPUT_PORT
        );

        return nextNode;
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

    // =========================================================
    // INPUT
    // =========================================================

    /**
     * Stores incoming user input in the execution context.
     */
    private void processUserInput(
            String userInput,
            Map<String, Object> context) {

        if (userInput == null
                || userInput.isBlank()) {

            return;
        }

        context.put(
                "lastUserInput",
                userInput
        );

        Object waitingVariable =
                context.get(
                        FlowExecutionContextKeys
                                .WAITING_VARIABLE
                );

        if (waitingVariable == null) {
            return;
        }

        String variableName =
                String.valueOf(
                        waitingVariable
                ).trim();

        if (variableName.isBlank()) {
            return;
        }

        context.put(
                variableName,
                userInput
        );

        log.debug(
                "User input stored in Flow context. variable={}",
                variableName
        );
    }

    // =========================================================
    // EXTERNAL RESPONSE
    // =========================================================

    /**
     * Stores an external response in its configured context
     * variable and in the last-response variable.
     */
    private void storeResponse(
            Map<String, Object> context,
            String waitingVariableKey,
            Object response,
            String lastResponseKey) {

        Object waitingVariable =
                context.get(
                        waitingVariableKey
                );

        if (waitingVariable != null) {

            String variableName =
                    String.valueOf(
                            waitingVariable
                    ).trim();

            if (!variableName.isBlank()) {

                context.put(
                        variableName,
                        response
                );
            }
        }

        context.put(
                lastResponseKey,
                response
        );
    }

    // =========================================================
    // CONTEXT
    // =========================================================

    private void mergeRequestContext(
            Map<String, Object> context,
            Map<String, Object> requestContext) {

        if (requestContext == null
                || requestContext.isEmpty()) {

            return;
        }

        context.putAll(
                requestContext
        );

        log.debug(
                "Merged request context into Flow execution. " +
                        "entryCount={}",
                requestContext.size()
        );
    }

    private void clearApiWaitingContext(
            Map<String, Object> context) {

        context.remove(
                FlowExecutionContextKeys
                        .WAITING_API_VARIABLE
        );

        context.remove(
                FlowExecutionContextKeys
                        .API_REQUEST
        );
    }

    private void clearAiWaitingContext(
            Map<String, Object> context) {

        context.remove(
                FlowExecutionContextKeys
                        .WAITING_AI_VARIABLE
        );

        context.remove(
                FlowExecutionContextKeys
                        .AI_PROMPT
        );
    }

    private void persistContext(
            FlowExecution execution,
            Map<String, Object> context) {

        execution.setContextData(
                flowContextService.writeContext(
                        context
                )
        );

        executionRepository.save(
                execution
        );
    }

    // =========================================================
    // EXECUTION LOOKUP
    // =========================================================

    private FlowExecution findExecution(
            String executionPublicId) {

        return executionRepository
                .findByPublicId(
                        executionPublicId
                )
                .orElseThrow(() -> {

                    log.warn(
                            "Flow execution not found. " +
                                    "executionPublicId={}",
                            executionPublicId
                    );

                    return new ResourceNotFoundException(
                            FlowMessages.EXECUTION_NOT_FOUND
                    );
                });
    }

    private FlowNode findCurrentNode(
            FlowExecution execution) {

        if (execution.getCurrentNodeId() == null) {

            log.error(
                    "Current node ID is missing. " +
                            "executionPublicId={}",
                    execution.getPublicId()
            );

            throw new ResourceNotFoundException(
                    FlowMessages.NODE_NOT_FOUND
            );
        }

        return nodeRepository
                .findById(
                        execution.getCurrentNodeId()
                )
                .orElseThrow(() -> {

                    log.error(
                            "Current Flow node not found. " +
                                    "executionPublicId={}, nodeId={}",
                            execution.getPublicId(),
                            execution.getCurrentNodeId()
                    );

                    return new ResourceNotFoundException(
                            FlowMessages.NODE_NOT_FOUND
                    );
                });
    }

    // =========================================================
    // STATE VALIDATION
    // =========================================================

    private void validateExecutionState(
            FlowExecution execution) {

        FlowExecutionStatus status =
                execution.getStatus();

        if (status
                == FlowExecutionStatus.COMPLETED) {

            throw new IllegalStateException(
                    "Flow execution is already completed."
            );
        }

        if (status
                == FlowExecutionStatus.CANCELLED) {

            throw new IllegalStateException(
                    "Flow execution is cancelled."
            );
        }

        if (status
                == FlowExecutionStatus.TRANSFERRED) {

            throw new IllegalStateException(
                    "Flow execution has already been transferred."
            );
        }
    }

    private void validateWaitingStatus(
            FlowExecution execution,
            FlowExecutionStatus expectedStatus,
            String responseType) {

        if (execution.getStatus()
                == expectedStatus) {

            return;
        }

        log.warn(
                "Invalid Flow continuation state. " +
                        "executionPublicId={}, expected={}, actual={}",
                execution.getPublicId(),
                expectedStatus,
                execution.getStatus()
        );

        throw new IllegalStateException(
                "Flow execution is not waiting for "
                        + responseType
                        + " response."
        );
    }

    private boolean isWaitingStatus(
            FlowExecutionStatus status) {

        return status
                == FlowExecutionStatus.WAITING_FOR_INPUT

                || status
                == FlowExecutionStatus.WAITING_FOR_AI

                || status
                == FlowExecutionStatus.WAITING_FOR_API;
    }

    private void clearWaitingStatus(
            FlowExecution execution,
            Map<String, Object> context) {

        execution.setStatus(
                FlowExecutionStatus.RUNNING
        );

        persistContext(
                execution,
                context
        );
    }

    @Override
    public FlowExecutionResult continueAfterWait(
            String executionPublicId) {

        log.info(
                "Attempting to resume Flow after WAIT. " +
                        "executionPublicId={}",
                executionPublicId
        );

        FlowExecution execution =
                findExecution(
                        executionPublicId
                );

        validateWaitingStatus(
                execution,
                FlowExecutionStatus.WAITING_FOR_TIMER,
                "timer"
        );

        Map<String, Object> context =
                flowContextService.readContext(
                        execution.getContextData()
                );

        Object resumeAtValue =
                context.get(
                        FlowExecutionContextKeys.WAIT_RESUME_AT
                );

        if (resumeAtValue == null) {

            log.error(
                    "WAIT resume timestamp missing. " +
                            "executionPublicId={}",
                    executionPublicId
            );

            throw new IllegalStateException(
                    FlowMessages.INVALID_CONFIGURATION
            );
        }

        LocalDateTime resumeAt;

        try {

            resumeAt =
                    LocalDateTime.parse(
                            String.valueOf(
                                    resumeAtValue
                            )
                    );

        } catch (Exception exception) {

            log.error(
                    "Invalid WAIT resume timestamp. " +
                            "executionPublicId={}, value={}",
                    executionPublicId,
                    resumeAtValue,
                    exception
            );

            throw new IllegalStateException(
                    FlowMessages.INVALID_CONFIGURATION,
                    exception
            );
        }

        if (LocalDateTime.now()
                .isBefore(resumeAt)) {

            log.debug(
                    "WAIT timer has not expired. " +
                            "executionPublicId={}, resumeAt={}",
                    executionPublicId,
                    resumeAt
            );

            throw new IllegalStateException(
                    "Flow WAIT timer has not expired."
            );
        }

        context.remove(
                FlowExecutionContextKeys.WAIT_RESUME_AT
        );

        context.remove(
                FlowExecutionContextKeys.WAIT_DURATION_SECONDS
        );

        context.remove(
                FlowExecutionContextKeys.WAIT_NODE_PUBLIC_ID
        );

        execution.setStatus(
                FlowExecutionStatus.RUNNING
        );

        persistContext(
                execution,
                context
        );

        FlowNode currentNode =
                findCurrentNode(
                        execution
                );

        FlowNode nextNode =
                resolveNextNode(
                        currentNode,
                        context
                );

        log.info(
                "WAIT timer expired. Continuing Flow. " +
                        "executionPublicId={}, currentNode={}, nextNode={}",
                executionPublicId,
                currentNode.getNodeKey(),
                nextNode.getNodeKey()
        );

        return executeFromNode(
                execution,
                nextNode,
                context
        );
    }
}
package com.infinitio.aivoiceplatform.flow.service.impl;

import com.infinitio.aivoiceplatform.auth.service.CurrentUserService;
import com.infinitio.aivoiceplatform.exception.ResourceNotFoundException;
import com.infinitio.aivoiceplatform.flow.constant.FlowExecutionStatus;
import com.infinitio.aivoiceplatform.flow.constant.FlowMessages;
import com.infinitio.aivoiceplatform.flow.constant.FlowNodeType;
import com.infinitio.aivoiceplatform.flow.constant.FlowStatus;
import com.infinitio.aivoiceplatform.flow.dto.request.ContinueApiResponseRequest;
import com.infinitio.aivoiceplatform.flow.dto.request.ContinueAiResponseRequest;
import com.infinitio.aivoiceplatform.flow.dto.request.ContinueFlowExecutionRequest;
import com.infinitio.aivoiceplatform.flow.dto.request.StartFlowExecutionRequest;
import com.infinitio.aivoiceplatform.flow.dto.response.FlowExecutionResult;
import com.infinitio.aivoiceplatform.flow.dto.response.FlowNodeExecutionResult;
import com.infinitio.aivoiceplatform.flow.entity.Flow;
import com.infinitio.aivoiceplatform.flow.entity.FlowExecution;
import com.infinitio.aivoiceplatform.flow.entity.FlowNode;
import com.infinitio.aivoiceplatform.flow.repository.FlowExecutionRepository;
import com.infinitio.aivoiceplatform.flow.repository.FlowNodeRepository;
import com.infinitio.aivoiceplatform.flow.service.FlowContextService;
import com.infinitio.aivoiceplatform.flow.service.FlowExecutionContinuationService;
import com.infinitio.aivoiceplatform.flow.service.FlowExecutionRuntimeService;
import com.infinitio.aivoiceplatform.flow.service.FlowExecutionService;
import com.infinitio.aivoiceplatform.flow.service.FlowResultService;
import com.infinitio.aivoiceplatform.flow.validator.FlowValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Default implementation of Flow Execution Service.
 *
 * <p>
 * This service owns the Flow execution lifecycle:
 * </p>
 *
 * <ul>
 *     <li>Starting a Flow execution</li>
 *     <li>Retrieving an execution</li>
 *     <li>Delegating continuation requests</li>
 *     <li>Cancelling an execution</li>
 * </ul>
 *
 * <p>
 * Actual node execution is delegated to
 * {@link FlowExecutionRuntimeService}.
 * Continuation of waiting executions is delegated to
 * {@link FlowExecutionContinuationService}.
 * </p>
 *
 * <p>
 * This separation is required for the n8n-style Flow architecture,
 * where the saved Flow graph determines the next node at runtime.
 * </p>
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class FlowExecutionServiceImpl
        implements FlowExecutionService {

    private final FlowExecutionRepository
            executionRepository;

    private final FlowNodeRepository
            nodeRepository;

    private final FlowValidator
            flowValidator;

    private final FlowContextService
            flowContextService;

    private final FlowExecutionRuntimeService
            runtimeService;

    private final FlowExecutionContinuationService
            continuationService;

    private final FlowResultService
            resultService;

    private final CurrentUserService
            currentUserService;

    // =========================================================
    // START FLOW EXECUTION
    // =========================================================

    /**
     * {@inheritDoc}
     */
    @Override
    public FlowExecutionResult start(
            StartFlowExecutionRequest request) {

        validateStartRequest(
                request
        );

        log.info(
                "Starting Flow execution. " +
                        "flowPublicId={}, callPublicId={}, " +
                        "conversationPublicId={}",
                request.getFlowPublicId(),
                request.getCallPublicId(),
                request.getConversationPublicId()
        );

        /*
         * Validate the requested Flow and retrieve
         * the persistent Flow entity.
         */
        Flow flow =
                flowValidator.validateAndGet(
                        request.getFlowPublicId()
                );

        if (flow.getStatus()
                != FlowStatus.ACTIVE) {

            log.warn(
                    "Cannot start inactive Flow. " +
                            "flowPublicId={}, status={}",
                    request.getFlowPublicId(),
                    flow.getStatus()
            );

            throw new IllegalStateException(
                    FlowMessages.FLOW_NOT_ACTIVE
            );
        }

        /*
         * The Flow must contain exactly one START node.
         *
         * The Flow validator is responsible for graph-level
         * validation. This lookup retrieves the node that will
         * begin runtime execution.
         */
        FlowNode startNode =
                nodeRepository
                        .findByFlowIdAndNodeType(
                                flow.getId(),
                                FlowNodeType.START
                        )
                        .orElseThrow(() -> {

                            log.error(
                                    "START node not found. " +
                                            "flowPublicId={}",
                                    request.getFlowPublicId()
                            );

                            return new IllegalStateException(
                                    FlowMessages.START_NODE_REQUIRED
                            );
                        });

        /*
         * Create initial runtime context.
         */
        Map<String, Object> context =
                request.getContext() == null
                        ? new HashMap<>()
                        : new HashMap<>(
                        request.getContext()
                );

        /*
         * Store identifiers required by runtime nodes.
         *
         * The context is intentionally generic because each
         * tenant's Flow may require different runtime variables.
         */
        if (request.getCallPublicId() != null
                && !request.getCallPublicId().isBlank()) {

            context.put(
                    "callId",
                    request.getCallPublicId()
            );
        }

        if (request.getConversationPublicId() != null
                && !request.getConversationPublicId().isBlank()) {

            context.put(
                    "conversationPublicId",
                    request.getConversationPublicId()
            );
        }

        /*
         * Resolve authenticated user for audit fields.
         */
        Long currentUserId =
                currentUserService.getCurrentUserId();

        if (currentUserId == null) {

            log.error(
                    "Unable to start Flow execution because " +
                            "authenticated user ID is unavailable. " +
                            "flowPublicId={}, callPublicId={}",
                    request.getFlowPublicId(),
                    request.getCallPublicId()
            );

            throw new IllegalStateException(
                    "Authenticated user is required to start flow execution."
            );
        }

        /*
         * Create FlowExecution.
         *
         * The execution starts at START. The runtime service
         * will then determine the next node using the Flow graph.
         */
        FlowExecution execution =
                FlowExecution.builder()
                        .flow(
                                flow
                        )
                        .callPublicId(
                                request.getCallPublicId()
                        )
                        .conversationPublicId(
                                request.getConversationPublicId()
                        )
                        .currentNodeId(
                                startNode.getId()
                        )
                        .status(
                                FlowExecutionStatus.RUNNING
                        )
                        .contextData(
                                flowContextService.writeContext(
                                        context
                                )
                        )
                        .startedAt(
                                LocalDateTime.now()
                        )
                        .createdBy(
                                currentUserId
                        )
                        .build();

        FlowExecution savedExecution =
                executionRepository.save(
                        execution
                );

        log.info(
                "Flow execution created successfully. " +
                        "executionPublicId={}, flowPublicId={}, " +
                        "startNode={}, createdBy={}",
                savedExecution.getPublicId(),
                request.getFlowPublicId(),
                startNode.getNodeKey(),
                currentUserId
        );

        /*
         * IMPORTANT:
         *
         * Do not execute the START node here manually.
         *
         * The runtime service owns the complete node execution
         * and transition chain.
         *
         * Therefore:
         *
         * START
         *   ↓
         * FlowTransitionService
         *   ↓
         * tenant-configured next node
         */
        FlowExecutionResult result =
                runtimeService.execute(
                        savedExecution,
                        startNode,
                        context
                );

        log.info(
                "Flow execution started successfully. " +
                        "executionPublicId={}, flowPublicId={}, " +
                        "currentNode={}, status={}, action={}",
                savedExecution.getPublicId(),
                request.getFlowPublicId(),
                result == null
                        ? null
                        : result.getCurrentNodeKey(),
                result == null
                        ? null
                        : result.getStatus(),
                result == null
                        ? null
                        : result.getAction()
        );

        return result;
    }

    // =========================================================
    // NORMAL CONTINUATION
    // =========================================================

    /**
     * {@inheritDoc}
     */
    @Override
    public FlowExecutionResult continueExecution(
            ContinueFlowExecutionRequest request) {

        validateExecutionPublicId(
                request == null
                        ? null
                        : request.getExecutionPublicId()
        );

        log.info(
                "Delegating Flow continuation. " +
                        "executionPublicId={}",
                request.getExecutionPublicId()
        );

        return continuationService
                .continueExecution(
                        request
                );
    }

    // =========================================================
    // GET EXECUTION
    // =========================================================

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public FlowExecutionResult getExecution(
            String executionPublicId) {

        validateExecutionPublicId(
                executionPublicId
        );

        log.debug(
                "Retrieving Flow execution. " +
                        "executionPublicId={}",
                executionPublicId
        );

        FlowExecution execution =
                executionRepository
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

        FlowNode currentNode =
                findCurrentNode(
                        execution
                );

        /*
         * getExecution is a read operation.
         *
         * We intentionally do not execute the current node here.
         * Executing a node while retrieving state would cause
         * duplicate LLM/TTS/API execution.
         */
        Map<String, Object> context =
                flowContextService.readContext(
                        execution.getContextData()
                );

        FlowNodeExecutionResult nodeResult =
                FlowNodeExecutionResult.builder()
                        .status(
                                execution.getStatus()
                        )
                        .action(
                                resolveCurrentAction(
                                        execution
                                )
                        )
                        .context(
                                context
                        )
                        .build();

        FlowExecutionResult result =
                resultService.buildResult(
                        execution,
                        currentNode,
                        nodeResult
                );

        log.debug(
                "Flow execution retrieved. " +
                        "executionPublicId={}, nodeKey={}, " +
                        "nodeType={}, status={}",
                executionPublicId,
                currentNode.getNodeKey(),
                currentNode.getNodeType(),
                execution.getStatus()
        );

        return result;
    }

    // =========================================================
    // API RESPONSE
    // =========================================================

    /**
     * {@inheritDoc}
     */
    @Override
    public FlowExecutionResult continueWithApiResponse(
            ContinueApiResponseRequest request) {

        validateExecutionPublicId(
                request == null
                        ? null
                        : request.getExecutionPublicId()
        );

        log.info(
                "Delegating API response continuation. " +
                        "executionPublicId={}",
                request.getExecutionPublicId()
        );

        return continuationService
                .continueWithApiResponse(
                        request
                );
    }

    // =========================================================
    // AI RESPONSE
    // =========================================================

    /**
     * {@inheritDoc}
     */
    @Override
    public FlowExecutionResult continueWithAiResponse(
            ContinueAiResponseRequest request) {

        validateExecutionPublicId(
                request == null
                        ? null
                        : request.getExecutionPublicId()
        );

        log.info(
                "Delegating AI response continuation. " +
                        "executionPublicId={}",
                request.getExecutionPublicId()
        );

        return continuationService
                .continueWithAiResponse(
                        request
                );
    }

    // =========================================================
    // CANCEL
    // =========================================================

    /**
     * {@inheritDoc}
     */
    @Override
    public void cancel(
            String executionPublicId) {

        validateExecutionPublicId(
                executionPublicId
        );

        log.info(
                "Cancelling Flow execution. " +
                        "executionPublicId={}",
                executionPublicId
        );

        FlowExecution execution =
                executionRepository
                        .findByPublicId(
                                executionPublicId
                        )
                        .orElseThrow(() -> {

                            log.warn(
                                    "Cannot cancel missing Flow execution. " +
                                            "executionPublicId={}",
                                    executionPublicId
                            );

                            return new ResourceNotFoundException(
                                    FlowMessages.EXECUTION_NOT_FOUND
                            );
                        });

        if (execution.getStatus()
                == FlowExecutionStatus.COMPLETED) {

            log.debug(
                    "Flow execution is already completed. " +
                            "executionPublicId={}",
                    executionPublicId
            );

            return;
        }

        if (execution.getStatus()
                == FlowExecutionStatus.CANCELLED) {

            log.debug(
                    "Flow execution is already cancelled. " +
                            "executionPublicId={}",
                    executionPublicId
            );

            return;
        }

        execution.setStatus(
                FlowExecutionStatus.CANCELLED
        );

        execution.setCompletedAt(
                LocalDateTime.now()
        );

        execution.setUpdatedBy(
                currentUserService.getCurrentUserId()
        );

        executionRepository.save(
                execution
        );

        log.info(
                "Flow execution cancelled successfully. " +
                        "executionPublicId={}",
                executionPublicId
        );
    }

    // =========================================================
    // CURRENT ACTION
    // =========================================================

    /**
     * Resolves the current action when returning an existing
     * Flow execution without executing the node again.
     *
     * @param execution Flow execution
     * @return current runtime action
     */
    private String resolveCurrentAction(
            FlowExecution execution) {

        if (execution == null
                || execution.getStatus() == null) {

            return null;
        }

        return switch (
                execution.getStatus()
                ) {

            case WAITING_FOR_INPUT ->
                    "WAIT_FOR_INPUT";

            case WAITING_FOR_AI ->
                    "WAIT_FOR_AI";

            case WAITING_FOR_API ->
                    "WAIT_FOR_API";

            case WAITING_FOR_TIMER ->
                    "WAIT";

            case TRANSFERRED ->
                    "TRANSFER";

            case COMPLETED ->
                    "END";

            default ->
                    "CONTINUE";
        };
    }

    // =========================================================
    // NODE LOOKUP
    // =========================================================

    /**
     * Finds the current node for an execution.
     *
     * @param execution Flow execution
     * @return current Flow node
     */
    private FlowNode findCurrentNode(
            FlowExecution execution) {

        if (execution == null
                || execution.getCurrentNodeId() == null) {

            log.error(
                    "Current Flow node ID is missing. " +
                            "executionPublicId={}",
                    execution == null
                            ? null
                            : execution.getPublicId()
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
    // VALIDATION
    // =========================================================

    /**
     * Validates Flow start request.
     *
     * @param request start request
     */
    private void validateStartRequest(
            StartFlowExecutionRequest request) {

        if (request == null) {

            throw new IllegalArgumentException(
                    FlowMessages.EXECUTION_FAILED
            );
        }

        if (request.getFlowPublicId() == null
                || request.getFlowPublicId().isBlank()) {

            throw new IllegalArgumentException(
                    FlowMessages.NOT_FOUND
            );
        }
    }

    /**
     * Validates execution public ID.
     *
     * @param executionPublicId execution identifier
     */
    private void validateExecutionPublicId(
            String executionPublicId) {

        if (executionPublicId == null
                || executionPublicId.isBlank()) {

            throw new IllegalArgumentException(
                    FlowMessages.EXECUTION_NOT_FOUND
            );
        }
    }
}
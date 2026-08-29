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
import com.infinitio.aivoiceplatform.flow.service.FlowExecutionService;
import com.infinitio.aivoiceplatform.flow.service.FlowNodeExecutionService;
import com.infinitio.aivoiceplatform.flow.service.FlowResultService;
import com.infinitio.aivoiceplatform.flow.service.FlowTransitionService;
import com.infinitio.aivoiceplatform.flow.validator.FlowValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class FlowExecutionServiceImpl
        implements FlowExecutionService {

    private final FlowExecutionRepository executionRepository;

    private final FlowNodeRepository nodeRepository;

    private final FlowValidator flowValidator;

    private final FlowContextService flowContextService;

    private final FlowNodeExecutionService nodeExecutionService;

    private final FlowTransitionService transitionService;

    private final FlowResultService resultService;

    /**
     * Current authenticated user service.
     *
     * Used to populate audit information such as
     * createdBy for newly created FlowExecution records.
     */
    private final CurrentUserService currentUserService;


    // =========================================================
    // START FLOW EXECUTION
    // =========================================================

    @Override
    public FlowExecutionResult start(
            StartFlowExecutionRequest request) {

        log.info(
                "Starting Flow execution. " +
                        "flowPublicId={}, callPublicId={}, " +
                        "conversationPublicId={}",
                request.getFlowPublicId(),
                request.getCallPublicId(),
                request.getConversationPublicId()
        );

        /*
         * Validate flow.
         */
        Flow flow =
                flowValidator.validateAndGet(
                        request.getFlowPublicId()
                );

        if (flow.getStatus() != FlowStatus.ACTIVE) {

            throw new IllegalStateException(
                    FlowMessages.FLOW_NOT_ACTIVE
            );
        }

        /*
         * Find START node.
         */
        FlowNode startNode =
                nodeRepository
                        .findByFlowIdAndNodeType(
                                flow.getId(),
                                FlowNodeType.START
                        )
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        FlowMessages.START_NODE_REQUIRED
                                )
                        );

        /*
         * Create runtime context.
         */
        Map<String, Object> context =
                request.getContext() == null
                        ? new HashMap<>()
                        : new HashMap<>(
                        request.getContext()
                );

        /*
         * Resolve current authenticated user.
         *
         * FlowExecution extends the common audit entity and
         * createdBy is NOT NULL in the database.
         */
        Long currentUserId =
                currentUserService.getCurrentUserId();

        if (currentUserId == null) {

            log.error(
                    "Unable to start Flow execution because " +
                            "current authenticated user ID is null. " +
                            "flowPublicId={}",
                    request.getFlowPublicId()
            );

            throw new IllegalStateException(
                    "Authenticated user is required to start flow execution."
            );
        }

        log.debug(
                "Creating Flow execution. flowPublicId={}, " +
                        "createdBy={}, startNodeId={}",
                request.getFlowPublicId(),
                currentUserId,
                startNode.getId()
        );

        /*
         * Create execution.
         */
        FlowExecution execution =
                FlowExecution.builder()
                        .flow(flow)
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
                        "createdBy={}",
                savedExecution.getPublicId(),
                request.getFlowPublicId(),
                currentUserId
        );

        /*
         * Execute START node.
         */
        return executeCurrentNode(
                savedExecution,
                startNode,
                context
        );
    }


    // =========================================================
    // CONTINUE EXECUTION
    // =========================================================

    @Override
    public FlowExecutionResult continueExecution(
            ContinueFlowExecutionRequest request) {

        log.info(
                "Continuing Flow execution. execution={}",
                request.getExecutionPublicId()
        );

        FlowExecution execution =
                executionRepository
                        .findByPublicId(
                                request.getExecutionPublicId()
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        FlowMessages.EXECUTION_NOT_FOUND
                                )
                        );

        /*
         * Do not continue a completed execution.
         */
        if (execution.getStatus()
                == FlowExecutionStatus.COMPLETED) {

            throw new IllegalStateException(
                    "Flow execution is already completed."
            );
        }

        /*
         * Do not continue a cancelled execution.
         */
        if (execution.getStatus()
                == FlowExecutionStatus.CANCELLED) {

            throw new IllegalStateException(
                    "Flow execution is cancelled."
            );
        }

        /*
         * Do not continue a transferred execution.
         */
        if (execution.getStatus()
                == FlowExecutionStatus.TRANSFERRED) {

            throw new IllegalStateException(
                    "Flow execution has already been transferred."
            );
        }

        /*
         * Restore runtime context.
         */
        Map<String, Object> context =
                flowContextService.readContext(
                        execution.getContextData()
                );

        if (request.getContext() != null) {

            context.putAll(
                    request.getContext()
            );
        }

        /*
         * Store user input.
         */
        processUserInput(
                request.getUserInput(),
                context
        );

        /*
         * Find current node.
         */
        if (execution.getCurrentNodeId() == null) {

            throw new ResourceNotFoundException(
                    FlowMessages.NODE_NOT_FOUND
            );
        }

        FlowNode currentNode =
                nodeRepository
                        .findById(
                                execution.getCurrentNodeId()
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        FlowMessages.NODE_NOT_FOUND
                                )
                        );

        /*
         * If execution was waiting for an external
         * input/result, continue from the next node.
         */
        if (isWaitingStatus(
                execution.getStatus()
        )) {

            execution.setStatus(
                    FlowExecutionStatus.RUNNING
            );

            execution.setContextData(
                    flowContextService.writeContext(
                            context
                    )
            );

            executionRepository.save(
                    execution
            );

            FlowNode nextNode =
                    transitionService.getNextNode(
                            currentNode,
                            context
                    );

            return executeCurrentNode(
                    execution,
                    nextNode,
                    context
            );
        }

        /*
         * Normal continuation.
         */
        execution.setStatus(
                FlowExecutionStatus.RUNNING
        );

        execution.setContextData(
                flowContextService.writeContext(
                        context
                )
        );

        executionRepository.save(
                execution
        );

        return executeCurrentNode(
                execution,
                currentNode,
                context
        );
    }


    // =========================================================
    // GET EXECUTION
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public FlowExecutionResult getExecution(
            String executionPublicId) {

        FlowExecution execution =
                executionRepository
                        .findByPublicId(
                                executionPublicId
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        FlowMessages.EXECUTION_NOT_FOUND
                                )
                        );

        if (execution.getCurrentNodeId() == null) {

            throw new ResourceNotFoundException(
                    FlowMessages.NODE_NOT_FOUND
            );
        }

        FlowNode currentNode =
                nodeRepository
                        .findById(
                                execution.getCurrentNodeId()
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        FlowMessages.NODE_NOT_FOUND
                                )
                        );

        Map<String, Object> context =
                flowContextService.readContext(
                        execution.getContextData()
                );

        FlowNodeExecutionResult nodeResult =
                FlowNodeExecutionResult.builder()
                        .build();

        return resultService.buildResult(
                execution,
                currentNode,
                nodeResult
        );
    }


    // =========================================================
    // CONTINUE WITH API RESPONSE
    // =========================================================

    @Override
    public FlowExecutionResult continueWithApiResponse(
            ContinueApiResponseRequest request) {

        log.info(
                "Continuing Flow execution with API response. " +
                        "execution={}",
                request.getExecutionPublicId()
        );

        FlowExecution execution =
                executionRepository
                        .findByPublicId(
                                request.getExecutionPublicId()
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        FlowMessages.EXECUTION_NOT_FOUND
                                )
                        );

        /*
         * API response is valid only while waiting
         * for an API response.
         */
        if (execution.getStatus()
                != FlowExecutionStatus.WAITING_FOR_API) {

            throw new IllegalStateException(
                    "Flow execution is not waiting for an API response."
            );
        }

        /*
         * Restore existing context.
         */
        Map<String, Object> context =
                flowContextService.readContext(
                        execution.getContextData()
                );

        /*
         * Get API response.
         */
        Object response =
                request.getResponse();

        /*
         * Store response in the configured
         * waiting variable.
         */
        Object waitingVariable =
                context.get(
                        "_waitingApiVariable"
                );

        if (waitingVariable != null
                && !String.valueOf(
                waitingVariable
        ).isBlank()) {

            context.put(
                    String.valueOf(
                            waitingVariable
                    ),
                    response
            );
        }

        /*
         * Store raw API response.
         */
        context.put(
                "lastApiResponse",
                response
        );

        /*
         * Merge additional context.
         */
        if (request.getContext() != null) {

            context.putAll(
                    request.getContext()
            );
        }

        /*
         * Remove API waiting state.
         */
        context.remove(
                "_waitingApiVariable"
        );

        context.remove(
                "_apiRequest"
        );

        execution.setStatus(
                FlowExecutionStatus.RUNNING
        );

        execution.setContextData(
                flowContextService.writeContext(
                        context
                )
        );

        executionRepository.save(
                execution
        );

        /*
         * Find current API node.
         */
        if (execution.getCurrentNodeId() == null) {

            throw new IllegalStateException(
                    FlowMessages.NODE_NOT_FOUND
            );
        }

        FlowNode currentNode =
                nodeRepository
                        .findById(
                                execution.getCurrentNodeId()
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        FlowMessages.NODE_NOT_FOUND
                                )
                        );

        /*
         * Move to next node.
         */
        FlowNode nextNode =
                transitionService.getNextNode(
                        currentNode,
                        context
                );

        return executeCurrentNode(
                execution,
                nextNode,
                context
        );
    }


    // =========================================================
    // CONTINUE WITH AI RESPONSE
    // =========================================================

    @Override
    public FlowExecutionResult continueWithAiResponse(
            ContinueAiResponseRequest request) {

        log.info(
                "Continuing Flow execution with AI response. " +
                        "execution={}",
                request.getExecutionPublicId()
        );

        FlowExecution execution =
                executionRepository
                        .findByPublicId(
                                request.getExecutionPublicId()
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        FlowMessages.EXECUTION_NOT_FOUND
                                )
                        );

        /*
         * AI response is valid only while waiting
         * for an AI response.
         */
        if (execution.getStatus()
                != FlowExecutionStatus.WAITING_FOR_AI) {

            throw new IllegalStateException(
                    "Flow execution is not waiting for an AI response."
            );
        }

        /*
         * Restore existing context.
         */
        Map<String, Object> context =
                flowContextService.readContext(
                        execution.getContextData()
                );

        /*
         * Get AI response.
         */
        String response =
                request.getResponse();

        /*
         * Get waiting variable configured
         * by the AI node.
         */
        Object waitingVariable =
                context.get(
                        "_waitingAiVariable"
                );

        if (waitingVariable != null
                && !String.valueOf(
                waitingVariable
        ).isBlank()) {

            context.put(
                    String.valueOf(
                            waitingVariable
                    ),
                    response
            );
        }

        /*
         * Store raw AI response.
         */
        context.put(
                "lastAiResponse",
                response
        );

        /*
         * Merge additional context.
         */
        if (request.getContext() != null) {

            context.putAll(
                    request.getContext()
            );
        }

        /*
         * Remove AI waiting state.
         */
        context.remove(
                "_waitingAiVariable"
        );

        context.remove(
                "_aiPrompt"
        );

        execution.setStatus(
                FlowExecutionStatus.RUNNING
        );

        execution.setContextData(
                flowContextService.writeContext(
                        context
                )
        );

        executionRepository.save(
                execution
        );

        /*
         * Find current AI node.
         */
        if (execution.getCurrentNodeId() == null) {

            throw new IllegalStateException(
                    FlowMessages.NODE_NOT_FOUND
            );
        }

        FlowNode currentNode =
                nodeRepository
                        .findById(
                                execution.getCurrentNodeId()
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        FlowMessages.NODE_NOT_FOUND
                                )
                        );

        /*
         * Move to next node.
         */
        FlowNode nextNode =
                transitionService.getNextNode(
                        currentNode,
                        context
                );

        return executeCurrentNode(
                execution,
                nextNode,
                context
        );
    }


    // =========================================================
    // CANCEL EXECUTION
    // =========================================================

    @Override
    public void cancel(
            String executionPublicId) {

        log.info(
                "Cancelling Flow execution. execution={}",
                executionPublicId
        );

        FlowExecution execution =
                executionRepository
                        .findByPublicId(
                                executionPublicId
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        FlowMessages.EXECUTION_NOT_FOUND
                                )
                        );

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
                "Flow execution cancelled successfully. execution={}",
                executionPublicId
        );
    }


    // =========================================================
    // NODE EXECUTION
    // =========================================================

    /*
     * Executes one node and decides whether
     * the flow should stop or continue.
     */
    private FlowExecutionResult executeCurrentNode(
            FlowExecution execution,
            FlowNode node,
            Map<String, Object> context) {

        if (node == null) {

            throw new ResourceNotFoundException(
                    FlowMessages.NODE_NOT_FOUND
            );
        }

        /*
         * Keep current node synchronized with
         * the node actually being executed.
         */
        execution.setCurrentNodeId(
                node.getId()
        );

        execution.setContextData(
                flowContextService.writeContext(
                        context
                )
        );

        executionRepository.save(
                execution
        );

        log.debug(
                "Executing Flow node. " +
                        "executionPublicId={}, nodeKey={}, nodeType={}",
                execution.getPublicId(),
                node.getNodeKey(),
                node.getNodeType()
        );

        FlowNodeExecutionResult nodeResult =
                nodeExecutionService.execute(
                        execution,
                        node,
                        context
                );

        /*
         * Persist any context changes made by
         * the node handler.
         */
        execution.setContextData(
                flowContextService.writeContext(
                        context
                )
        );

        /*
         * Node requires external input/result.
         */
        if (nodeResult.isWaiting()
                || nodeResult.isTransferred()
                || nodeResult.isCompleted()) {

            executionRepository.save(
                    execution
            );

            return resultService.buildResult(
                    execution,
                    node,
                    nodeResult
            );
        }

        /*
         * Node completed immediately.
         * Find the next node.
         */
        FlowNode nextNode =
                transitionService.getNextNode(
                        node,
                        context
                );

        executionRepository.save(
                execution
        );

        return executeCurrentNode(
                execution,
                nextNode,
                context
        );
    }


    // =========================================================
    // USER INPUT
    // =========================================================

    /*
     * Store user input into the flow context.
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

        /*
         * InputNodeHandler stores the variable
         * under this key.
         */
        Object waitingVariable =
                context.get(
                        "_waitingVariable"
                );

        if (waitingVariable != null
                && !String.valueOf(
                waitingVariable
        ).isBlank()) {

            context.put(
                    String.valueOf(
                            waitingVariable
                    ),
                    userInput
            );
        }
    }


    // =========================================================
    // WAITING STATUS
    // =========================================================

    private boolean isWaitingStatus(
            FlowExecutionStatus status) {

        return status
                == FlowExecutionStatus.WAITING_FOR_INPUT

                || status
                == FlowExecutionStatus.WAITING_FOR_AI

                || status
                == FlowExecutionStatus.WAITING_FOR_API

                || status
                == FlowExecutionStatus.WAITING_FOR_TIMER;
    }
}
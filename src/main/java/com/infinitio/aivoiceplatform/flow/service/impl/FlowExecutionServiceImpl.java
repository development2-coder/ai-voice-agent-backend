package com.infinitio.aivoiceplatform.flow.service.impl;

import com.infinitio.aivoiceplatform.exception.ResourceNotFoundException;
import com.infinitio.aivoiceplatform.flow.constant.FlowExecutionStatus;
import com.infinitio.aivoiceplatform.flow.constant.FlowMessages;
import com.infinitio.aivoiceplatform.flow.constant.FlowNodeType;
import com.infinitio.aivoiceplatform.flow.constant.FlowStatus;
import com.infinitio.aivoiceplatform.flow.dto.request.ContinueFlowExecutionRequest;
import com.infinitio.aivoiceplatform.flow.dto.request.StartFlowExecutionRequest;
import com.infinitio.aivoiceplatform.flow.dto.response.FlowExecutionResult;
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
import com.infinitio.aivoiceplatform.flow.dto.response.FlowNodeExecutionResult;
import com.infinitio.aivoiceplatform.flow.validator.FlowValidator;
import com.infinitio.aivoiceplatform.flow.dto.request.ContinueApiResponseRequest;
import com.infinitio.aivoiceplatform.flow.dto.request.ContinueAiResponseRequest;
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

    @Override
    public FlowExecutionResult start(
            StartFlowExecutionRequest request) {

        log.info(
                "Starting flow execution. flow={}",
                request.getFlowPublicId()
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
                        .build();

        FlowExecution savedExecution =
                executionRepository.save(
                        execution
                );

        log.info(
                "Flow execution created. execution={}",
                savedExecution.getPublicId()
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

    @Override
    public FlowExecutionResult continueExecution(
            ContinueFlowExecutionRequest request) {

        log.info(
                "Continuing flow execution. execution={}",
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
         * Do not continue a completed/cancelled execution.
         */
        validateExecutionState(
                execution
        );

        /*
         * Restore context.
         */
        Map<String, Object> context =
                flowContextService.readContext(
                        execution.getContextData()
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
         * Process user input.
         */
        if (request.getUserInput() != null
                && !request.getUserInput().isBlank()) {

            processUserInput(
                    request.getUserInput(),
                    context
            );
        }

        /*
         * Find current node.
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
         * If execution was waiting for external
         * input/AI result, the current node has
         * already completed its waiting action.
         *
         * Therefore continue from the next node.
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
         * Normal execution.
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

        Map<String, Object> context =
                flowContextService.readContext(
                        execution.getContextData()
                );

        FlowNode node = null;

        if (execution.getCurrentNodeId() != null) {

            node =
                    nodeRepository
                            .findById(
                                    execution.getCurrentNodeId()
                            )
                            .orElse(null);
        }

        return FlowExecutionResult.builder()
                .executionPublicId(
                        execution.getPublicId()
                )
                .status(
                        execution.getStatus()
                )
                .currentNodeKey(
                        node != null
                                ? node.getNodeKey()
                                : null
                )
                .currentNodeType(
                        node != null
                                ? node.getNodeType().name()
                                : null
                )
                .waitingForInput(
                        execution.getStatus()
                                == FlowExecutionStatus.WAITING_FOR_INPUT
                )
                .waitingForAi(
                        execution.getStatus()
                                == FlowExecutionStatus.WAITING_FOR_AI
                )
                .waitingForApi(
                        execution.getStatus()
                                == FlowExecutionStatus.WAITING_FOR_API
                )
                .completed(
                        execution.getStatus()
                                == FlowExecutionStatus.COMPLETED
                )
                .transferred(
                        execution.getStatus()
                                == FlowExecutionStatus.TRANSFERRED
                )
                .context(
                        context
                )
                .build();
    }

    @Override
    public void cancel(
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

        execution.setStatus(
                FlowExecutionStatus.CANCELLED
        );

        execution.setCompletedAt(
                LocalDateTime.now()
        );

        executionRepository.save(
                execution
        );

        log.info(
                "Flow execution cancelled. execution={}",
                executionPublicId
        );
    }

    /*
     * Executes one node and decides whether
     * the flow should stop or continue.
     */
    private FlowExecutionResult executeCurrentNode(
            FlowExecution execution,
            FlowNode node,
            Map<String, Object> context) {

        FlowNodeExecutionResult nodeResult =
                nodeExecutionService.execute(
                        execution,
                        node,
                        context
                );

        /*
         * Node requires external input/result.
         */
        if (nodeResult.isWaiting()
                || nodeResult.isTransferred()
                || nodeResult.isCompleted()) {

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

        return executeCurrentNode(
                execution,
                nextNode,
                context
        );
    }

    /*
     * Store user input into the flow context.
     */
    private void processUserInput(
            String userInput,
            Map<String, Object> context) {

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

    private boolean isWaitingStatus(
            FlowExecutionStatus status) {

        return status
                == FlowExecutionStatus.WAITING_FOR_INPUT

                || status
                == FlowExecutionStatus.WAITING_FOR_AI

                || status
                == FlowExecutionStatus.WAITING_FOR_API;
    }

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

    @Override
    public FlowExecutionResult continueWithApiResponse(
            ContinueApiResponseRequest request) {

        log.info(
                "Continuing flow execution with API response. execution={}",
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
         * API response is only valid when the flow
         * is waiting for an API response.
         */
        if (execution.getStatus()
                != FlowExecutionStatus.WAITING_FOR_API) {

            throw new IllegalStateException(
                    "Flow execution is not waiting for an API response."
            );
        }

        /*
         * Restore existing flow context.
         */
        Map<String, Object> context =
                flowContextService.readContext(
                        execution.getContextData()
                );

        /*
         * Store the API response in the variable
         * configured by the API node.
         *
         * Example:
         *
         * _waitingApiVariable = appointmentResponse
         *
         * response = {
         *     "appointmentId": 123,
         *     "status": "CONFIRMED"
         * }
         *
         * Result:
         *
         * appointmentResponse = {...}
         */
        Object response =
                request.getResponse();

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
         * Store the raw API response as well.
         * This is useful for debugging and for
         * subsequent flow nodes.
         */
        context.put(
                "lastApiResponse",
                response
        );

        /*
         * Merge any additional context supplied
         * by the integration layer.
         */
        if (request.getContext() != null) {

            context.putAll(
                    request.getContext()
            );
        }

        /*
         * API request has completed.
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
         * Find the node that produced the API request.
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
         * API node has completed.
         * Move to the next node.
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

    @Override
    public FlowExecutionResult continueWithAiResponse(
            ContinueAiResponseRequest request) {

        log.info(
                "Continuing flow execution with AI response. execution={}",
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
         * AI response is valid only when the flow
         * is waiting for an AI response.
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
         * Get the variable configured in the AI node.
         *
         * Example:
         *
         * _waitingAiVariable = aiResponse
         */
        Object waitingVariable =
                context.get(
                        "_waitingAiVariable"
                );

        /*
         * Store AI response in the configured
         * flow variable.
         */
        String aiResponse =
                request.getResponse();

        if (waitingVariable != null
                && !String.valueOf(
                waitingVariable
        ).isBlank()) {

            context.put(
                    String.valueOf(
                            waitingVariable
                    ),
                    aiResponse
            );
        }

        /*
         * Also keep the latest AI response available
         * to subsequent nodes.
         */
        context.put(
                "lastAiResponse",
                aiResponse
        );

        /*
         * Merge additional context if supplied by
         * the AI integration service.
         */
        if (request.getContext() != null) {

            context.putAll(
                    request.getContext()
            );
        }

        /*
         * Remove temporary AI execution variables.
         */
        context.remove(
                "_waitingAiVariable"
        );

        context.remove(
                "_aiPrompt"
        );

        /*
         * AI processing is completed.
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

        /*
         * Find the AI node that initiated the
         * waiting state.
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
         * AI node has completed.
         * Find and execute the next node.
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
}
package com.infinitio.aivoiceplatform.flow.service.impl;

import com.infinitio.aivoiceplatform.auth.service.CurrentUserService;
import com.infinitio.aivoiceplatform.exception.ResourceNotFoundException;
import com.infinitio.aivoiceplatform.flow.constant.FlowExecutionStatus;
import com.infinitio.aivoiceplatform.flow.constant.FlowMessages;
import com.infinitio.aivoiceplatform.flow.constant.FlowNodeType;
import com.infinitio.aivoiceplatform.flow.constant.FlowStatus;
import com.infinitio.aivoiceplatform.flow.dto.request.StartFlowExecutionRequest;
import com.infinitio.aivoiceplatform.flow.dto.response.FlowExecutionResult;
import com.infinitio.aivoiceplatform.flow.entity.Flow;
import com.infinitio.aivoiceplatform.flow.entity.FlowExecution;
import com.infinitio.aivoiceplatform.flow.entity.FlowNode;
import com.infinitio.aivoiceplatform.flow.repository.FlowExecutionRepository;
import com.infinitio.aivoiceplatform.flow.repository.FlowNodeRepository;
import com.infinitio.aivoiceplatform.flow.service.FlowContextService;
import com.infinitio.aivoiceplatform.flow.service.FlowExecutionInitializationService;
import com.infinitio.aivoiceplatform.flow.validator.FlowValidator;
import com.infinitio.aivoiceplatform.user.constant.UserConstants;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class FlowExecutionInitializationServiceImpl
        implements FlowExecutionInitializationService {

    private static final Integer NOT_DELETED = 0;

    private final FlowExecutionRepository executionRepository;

    private final FlowNodeRepository nodeRepository;

    private final FlowValidator flowValidator;

    private final FlowContextService flowContextService;

    private final CurrentUserService currentUserService;

    /**
     * Creates the Flow Execution in an independent transaction.
     *
     * <p>
     * The transaction commits before Flow runtime execution starts.
     * This makes the execution public ID immediately visible to
     * concurrent Voice Gateway/STT requests.
     * </p>
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public FlowExecutionResult initialize(
            StartFlowExecutionRequest request) {

        validateStartRequest(
                request
        );

        log.info(
                "Initializing Flow execution. " +
                        "flowPublicId={}, callPublicId={}",
                request.getFlowPublicId(),
                request.getCallPublicId()
        );

        Flow flow =
                flowValidator.validateAndGet(
                        request.getFlowPublicId()
                );

        if (flow.getStatus() != FlowStatus.ACTIVE) {

            throw new IllegalStateException(
                    FlowMessages.FLOW_NOT_ACTIVE
            );
        }

        FlowNode startNode =
                nodeRepository
                        .findByFlowIdAndNodeTypeAndIsDeleted(
                                flow.getId(),
                                FlowNodeType.START,
                                NOT_DELETED
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        FlowMessages.START_NODE_REQUIRED
                                )
                        );

        Map<String, Object> context =
                request.getContext() == null
                        ? new HashMap<>()
                        : new HashMap<>(
                        request.getContext()
                );

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

        Long currentUserId =
                resolveExecutionUserId();

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

        /*
         * IMPORTANT:
         *
         * saveAndFlush forces INSERT before this transaction
         * completes. The REQUIRES_NEW transaction then commits,
         * making the execution visible to STT requests.
         */
        FlowExecution savedExecution =
                executionRepository.saveAndFlush(
                        execution
                );

        log.info(
                "Flow execution initialized and committed. " +
                        "executionPublicId={}, flowPublicId={}, startNode={}",
                savedExecution.getPublicId(),
                request.getFlowPublicId(),
                startNode.getNodeKey()
        );

        return FlowExecutionResult.builder()
                .executionPublicId(
                        savedExecution.getPublicId()
                )
                .status(
                        FlowExecutionStatus.RUNNING
                )
                .currentNodeKey(
                        startNode.getNodeKey()
                )
                .currentNodeType(
                        startNode.getNodeType().name()
                )
                .action(
                        "CONTINUE"
                )
                .waitingForInput(false)
                .waitingForAi(false)
                .waitingForApi(false)
                .waitingForTimer(false)
                .completed(false)
                .transferred(false)
                .context(context)
                .build();
    }

    private Long resolveExecutionUserId() {

        try {

            if (currentUserService.isAuthenticated()) {

                Long currentUserId =
                        currentUserService.getCurrentUserId();

                if (currentUserId != null) {
                    return currentUserId;
                }
            }

        } catch (Exception exception) {

            log.debug(
                    "Unable to resolve authenticated user. " +
                            "Using system user.",
                    exception
            );
        }

        return UserConstants.SYSTEM_USER_ID;
    }

    private void validateStartRequest(
            StartFlowExecutionRequest request) {

        if (request == null
                || request.getFlowPublicId() == null
                || request.getFlowPublicId().isBlank()) {

            throw new IllegalArgumentException(
                    FlowMessages.EXECUTION_FAILED
            );
        }
    }
}
package com.infinitio.aivoiceplatform.callsession.service.impl;

import com.infinitio.aivoiceplatform.auth.service.CurrentUserService;
import com.infinitio.aivoiceplatform.callsession.constant.CallSessionMessages;
import com.infinitio.aivoiceplatform.callsession.dto.request.CreateCallSessionRequestDto;
import com.infinitio.aivoiceplatform.callsession.dto.response.CallSessionResponseDto;
import com.infinitio.aivoiceplatform.callsession.entity.CallSession;
import com.infinitio.aivoiceplatform.callsession.mapper.CallSessionMapper;
import com.infinitio.aivoiceplatform.callsession.repository.CallSessionRepository;
import com.infinitio.aivoiceplatform.callsession.service.CallSessionCreateService;
import com.infinitio.aivoiceplatform.exception.BadRequestException;
import com.infinitio.aivoiceplatform.exception.ConflictException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implements call session creation business logic.
 *
 * <p>
 * This service is responsible for creating the persistent
 * CallSession runtime state.
 * </p>
 *
 * <p>
 * During runtime creation, the current flow node may
 * initially be {@code null}. The Flow Execution module
 * resolves the START node and updates the CallSession
 * with the actual current node after execution begins.
 * </p>
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CallSessionCreateServiceImpl
        implements CallSessionCreateService {

    private final CallSessionRepository
            callSessionRepository;

    private final CallSessionMapper
            callSessionMapper;

    private final CurrentUserService
            currentUserService;

    /**
     * Creates a Call Session using the currently
     * authenticated user as the audit owner.
     *
     * @param request call session creation request
     * @return created call session
     */
    @Override
    public CallSessionResponseDto createCallSession(
            CreateCallSessionRequestDto request) {

        log.info(
                "Creating CallSession using current user. "
                        + "callId={}",
                request != null
                        ? request.getCallId()
                        : null
        );

        Long currentUserId =
                currentUserService.getCurrentUserId();

        return createCallSessionInternal(
                request,
                currentUserId
        );
    }

    /**
     * Creates a Call Session using a supplied audit user.
     *
     * <p>
     * This method is used by system-driven workflows such
     * as AI Dialer and scheduler execution.
     * </p>
     *
     * @param request call session creation request
     * @param createdBy audit user ID
     * @return created call session
     */
    @Override
    public CallSessionResponseDto createCallSession(
            CreateCallSessionRequestDto request,
            Long createdBy) {

        log.info(
                "Creating system-driven CallSession. "
                        + "callId={}, createdBy={}",
                request != null
                        ? request.getCallId()
                        : null,
                createdBy
        );

        return createCallSessionInternal(
                request,
                createdBy
        );
    }

    /**
     * Performs the common CallSession creation logic.
     *
     * @param request call session creation request
     * @param createdBy audit user ID
     * @return created call session
     */
    private CallSessionResponseDto createCallSessionInternal(
            CreateCallSessionRequestDto request,
            Long createdBy) {

        log.info(
                "Creating CallSession. "
                        + "callId={}, tenantId={}, agentId={}, "
                        + "flowPublicId={}, flowNodeId={}",
                request != null
                        ? request.getCallId()
                        : null,
                request != null
                        ? request.getTenantId()
                        : null,
                request != null
                        ? request.getAgentId()
                        : null,
                request != null
                        ? request.getFlowPublicId()
                        : null,
                request != null
                        ? request.getFlowNodeId()
                        : null
        );

        /*
         * ---------------------------------------------------------
         * STEP 1: Validate request.
         * ---------------------------------------------------------
         */
        validateRequest(
                request,
                createdBy
        );

        /*
         * ---------------------------------------------------------
         * STEP 2: Prevent duplicate CallSession.
         * ---------------------------------------------------------
         *
         * A CallSession is unique for a Call.
         */
        if (callSessionRepository
                .existsByCallId(
                        request.getCallId()
                )) {

            log.warn(
                    "CallSession already exists. "
                            + "callId={}",
                    request.getCallId()
            );

            throw new ConflictException(
                    CallSessionMessages
                            .CALL_SESSION_ALREADY_EXISTS
            );
        }

        /*
         * ---------------------------------------------------------
         * STEP 3: Map request to entity.
         * ---------------------------------------------------------
         *
         * flowNodeId may be null here.
         *
         * The Flow Execution module will determine the START
         * node and CallSessionMapper.updateFromExecution()
         * will populate the actual current node.
         */
        CallSession callSession =
                callSessionMapper.toEntity(
                        request
                );

        /*
         * ---------------------------------------------------------
         * STEP 4: Set audit information.
         * ---------------------------------------------------------
         */
        callSession.setCreatedBy(
                createdBy
        );

        /*
         * ---------------------------------------------------------
         * STEP 5: Persist CallSession.
         * ---------------------------------------------------------
         */
        CallSession savedCallSession =
                callSessionRepository.save(
                        callSession
                );

        log.info(
                "CallSession created successfully. "
                        + "callId={}, tenantId={}, agentId={}, "
                        + "flowPublicId={}, flowNodeId={}",
                savedCallSession.getCallId(),
                savedCallSession.getTenantId(),
                savedCallSession.getAgentId(),
                request.getFlowPublicId(),
                savedCallSession.getFlowNodeId()
        );

        return callSessionMapper.toResponse(
                savedCallSession
        );
    }

    /**
     * Validates CallSession creation request.
     *
     * <p>
     * The initial flow node is intentionally not validated
     * here because runtime Flow Execution resolves the START
     * node after the CallSession is created.
     * </p>
     *
     * @param request call session creation request
     * @param createdBy audit user ID
     */
    private void validateRequest(
            CreateCallSessionRequestDto request,
            Long createdBy) {

        /*
         * ---------------------------------------------------------
         * REQUEST
         * ---------------------------------------------------------
         */
        if (request == null) {

            throw new BadRequestException(
                    CallSessionMessages
                            .CALL_SESSION_REQUEST_REQUIRED
            );
        }

        /*
         * ---------------------------------------------------------
         * CALL
         * ---------------------------------------------------------
         */
        if (request.getCallId() == null
                || request.getCallId().isBlank()) {

            throw new BadRequestException(
                    CallSessionMessages
                            .CALL_ID_REQUIRED
            );
        }

        /*
         * ---------------------------------------------------------
         * TENANT
         * ---------------------------------------------------------
         */
        if (request.getTenantId() == null
                || request.getTenantId().isBlank()) {

            throw new BadRequestException(
                    CallSessionMessages
                            .TENANT_ID_REQUIRED
            );
        }

        /*
         * ---------------------------------------------------------
         * AGENT
         * ---------------------------------------------------------
         */
        if (request.getAgentId() == null
                || request.getAgentId().isBlank()) {

            throw new BadRequestException(
                    CallSessionMessages
                            .AGENT_ID_REQUIRED
            );
        }

        /*
         * ---------------------------------------------------------
         * AGENT VERSION
         * ---------------------------------------------------------
         */
        if (request.getAgentVersion() == null) {

            throw new BadRequestException(
                    CallSessionMessages
                            .AGENT_VERSION_REQUIRED
            );
        }

        if (request.getAgentVersion() <= 0) {

            throw new BadRequestException(
                    CallSessionMessages
                            .AGENT_VERSION_INVALID
            );
        }

        /*
         * ---------------------------------------------------------
         * FLOW
         * ---------------------------------------------------------
         */
        if (request.getFlowPublicId() == null
                || request.getFlowPublicId().isBlank()) {

            throw new BadRequestException(
                    "Flow public ID is required."
            );
        }

        /*
         * ---------------------------------------------------------
         * FLOW NODE
         * ---------------------------------------------------------
         *
         * IMPORTANT:
         *
         * Do NOT validate flowNodeId here.
         *
         * For a new runtime session:
         *
         * flowNodeId = null
         *
         * is valid because FlowExecutionService resolves
         * the START node.
         *
         * After execution:
         *
         * CallSessionMapper.updateFromExecution()
         *
         * sets:
         *
         * flowNodeId = execution.currentNodeKey
         */
        if (request.getFlowNodeId() != null
                && request.getFlowNodeId().isBlank()) {

            log.debug(
                    "Blank flowNodeId received. "
                            + "Treating it as null. callId={}",
                    request.getCallId()
            );

            request.setFlowNodeId(
                    null
            );
        }

        /*
         * ---------------------------------------------------------
         * AUDIT USER
         * ---------------------------------------------------------
         */
        if (createdBy == null) {

            /*
             * There is no CREATED_BY_REQUIRED constant
             * currently defined in CallSessionMessages.
             *
             * Therefore do not invent one.
             */
            throw new BadRequestException(
                    "Created by user is required."
            );
        }
    }
}
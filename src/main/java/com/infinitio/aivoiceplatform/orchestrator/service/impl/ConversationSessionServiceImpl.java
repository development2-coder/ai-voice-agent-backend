package com.infinitio.aivoiceplatform.orchestrator.service.impl;

import com.infinitio.aivoiceplatform.auth.service.CurrentUserService;
import com.infinitio.aivoiceplatform.callsession.constant.CallSessionStatus;
import com.infinitio.aivoiceplatform.callsession.dto.request.UpdateCallSessionRequestDto;
import com.infinitio.aivoiceplatform.callsession.dto.request.UpdateFlowStateRequestDto;
import com.infinitio.aivoiceplatform.callsession.dto.response.CallSessionResponseDto;
import com.infinitio.aivoiceplatform.callsession.service.CallSessionFlowRuntimeService;
import com.infinitio.aivoiceplatform.callsession.service.CallSessionFlowService;
import com.infinitio.aivoiceplatform.callsession.service.CallSessionGetService;
import com.infinitio.aivoiceplatform.callsession.service.CallSessionRuntimeService;
import com.infinitio.aivoiceplatform.callsession.service.CallSessionUpdateService;
import com.infinitio.aivoiceplatform.exception.BadRequestException;
import com.infinitio.aivoiceplatform.exception.ResourceNotFoundException;
import com.infinitio.aivoiceplatform.flow.dto.response.FlowExecutionResult;
import com.infinitio.aivoiceplatform.flow.service.FlowExecutionService;
import com.infinitio.aivoiceplatform.orchestrator.constant.ConversationOrchestratorConstants;
import com.infinitio.aivoiceplatform.orchestrator.constant.ConversationOrchestratorMessages;
import com.infinitio.aivoiceplatform.orchestrator.dto.request.EndConversationRequestDto;
import com.infinitio.aivoiceplatform.orchestrator.dto.request.StartConversationRequestDto;
import com.infinitio.aivoiceplatform.orchestrator.dto.response.ConversationOrchestratorResponseDto;
import com.infinitio.aivoiceplatform.orchestrator.dto.response.ConversationRuntimeConfigurationResponseDto;
import com.infinitio.aivoiceplatform.orchestrator.service.ConversationResponseService;
import com.infinitio.aivoiceplatform.orchestrator.service.ConversationSessionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

/**
 * Default implementation of Conversation Session Service.
 *
 * <p>
 * Coordinates Call Session creation, Flow execution startup,
 * Call Session synchronization and conversation termination.
 * </p>
 *
 * <p>
 * Live telephony execution must not depend on an authenticated
 * browser user. When a Call Session already exists, the persisted
 * Call Session is treated as the runtime source of truth.
 * </p>
 *
 * <p>
 * Authentication is still used when a Call Session must be created
 * from an authenticated API request. A real-time provider callback
 * should normally operate on an already-created Call Session.
 * </p>
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ConversationSessionServiceImpl
        implements ConversationSessionService {

    /**
     * Runtime context key for Call ID.
     */
    private static final String CALL_ID =
            "callId";

    /**
     * Runtime context key for conversation language.
     */
    private static final String LANGUAGE =
            "language";

    /**
     * Runtime context key for tenant public ID.
     */
    private static final String TENANT_ID =
            "tenantId";

    /**
     * Runtime context key for agent public ID.
     */
    private static final String AGENT_ID =
            "agentId";

    /**
     * Runtime context key for Flow public ID.
     */
    private static final String FLOW_PUBLIC_ID =
            "flowPublicId";

    /**
     * Runtime context key for resolved Agent configuration.
     */
    private static final String RUNTIME_CONFIGURATION =
            "runtimeConfiguration";

    /**
     * Runtime configuration key for STT provider.
     */
    private static final String STT_PROVIDER =
            "sttProvider";

    /**
     * Runtime configuration key for STT model.
     */
    private static final String STT_MODEL =
            "sttModel";

    /**
     * Runtime configuration key for LLM provider.
     */
    private static final String LLM_PROVIDER =
            "llmProvider";

    /**
     * Runtime configuration key for LLM model.
     */
    private static final String LLM_MODEL =
            "llmModel";

    /**
     * Runtime configuration key for TTS provider.
     */
    private static final String TTS_PROVIDER =
            "ttsProvider";

    /**
     * Runtime configuration key for TTS model.
     */
    private static final String TTS_MODEL =
            "ttsModel";

    /**
     * Runtime configuration key for language.
     */
    private static final String CONFIGURATION_LANGUAGE =
            "language";

    /**
     * Runtime configuration key for voice.
     */
    private static final String VOICE =
            "voice";

    /**
     * Runtime configuration key for system prompt.
     */
    private static final String SYSTEM_PROMPT =
            "systemPrompt";

    /**
     * Runtime configuration key for LLM temperature.
     */
    private static final String TEMPERATURE =
            "temperature";

    /**
     * Runtime configuration key for maximum LLM tokens.
     */
    private static final String MAX_TOKENS =
            "maxTokens";

    private final CallSessionRuntimeService
            callSessionRuntimeService;

    private final CallSessionGetService
            callSessionGetService;

    private final CallSessionUpdateService
            callSessionUpdateService;

    private final CallSessionFlowService
            callSessionFlowService;

    private final CallSessionFlowRuntimeService
            callSessionFlowRuntimeService;

    private final FlowExecutionService
            flowExecutionService;

    private final CurrentUserService
            currentUserService;

    private final ConversationResponseService
            conversationResponseService;

    /**
     * {@inheritDoc}
     */
    @Override
    public ConversationOrchestratorResponseDto startConversation(
            StartConversationRequestDto request,
            ConversationRuntimeConfigurationResponseDto
                    runtimeConfiguration) {

        validateStartRequest(
                request
        );

        log.info(
                "Starting conversation session. " +
                        "callId={}, tenantId={}, agentId={}, " +
                        "flowPublicId={}, agentVersion={}, language={}",
                request.getCallId(),
                request.getTenantId(),
                request.getAgentId(),
                request.getFlowPublicId(),
                request.getAgentVersion(),
                request.getLanguage()
        );

        /*
         * First try to find the existing Call Session.
         *
         * This is the normal path for a live telephony call.
         * No authenticated browser user is required here.
         */
        CallSessionResponseDto session =
                findExistingSession(
                        request.getCallId()
                );

        if (session == null) {

            /*
             * A missing Call Session means this is most likely an
             * API/Swagger-driven runtime invocation rather than a
             * normal provider callback.
             *
             * Only in this case do we try to resolve an authenticated
             * user for Call Session creation.
             */
            Long currentUserId =
                    resolveAuthenticatedUserId();

            if (currentUserId == null) {

                log.error(
                        "Call Session does not exist and no " +
                                "authenticated user is available. " +
                                "callId={}",
                        request.getCallId()
                );

                throw new IllegalStateException(
                        ConversationOrchestratorMessages
                                .CONVERSATION_START_FAILED
                );
            }

            session =
                    createSession(
                            request,
                            currentUserId
                    );
        }

        /*
         * The Call Session is now the runtime source of truth.
         */
        validateActiveSession(
                session
        );

        /*
         * Verify that the Flow bound to the Call Session is
         * consistent with the requested Flow.
         */
        validateFlowBinding(
                request,
                session
        );

        /*
         * If a Flow Execution already exists, do not create another
         * execution for the same active conversation.
         */
        if (hasFlowExecution(
                session
        )) {

            log.info(
                    "Existing Flow Execution found. " +
                            "Reusing conversation. callId={}, " +
                            "flowExecutionPublicId={}",
                    request.getCallId(),
                    session.getFlowExecutionPublicId()
            );

            FlowExecutionResult execution =
                    getFlowExecution(
                            session
                    );

            synchronizeSession(
                    request.getCallId(),
                    execution,
                    resolveLanguage(
                            session.getLanguage()
                    )
            );

            return conversationResponseService
                    .buildResponse(
                            request.getCallId(),
                            null,
                            execution
                    );
        }

        /*
         * No Flow Execution exists.
         *
         * Start the exact Flow that is bound to the Call Session.
         */
        String flowPublicId =
                resolveFlowPublicId(
                        request,
                        session
                );

        String language =
                resolveLanguage(
                        session.getLanguage()
                );

        log.info(
                "Starting Flow Execution. " +
                        "callId={}, flowPublicId={}, language={}",
                request.getCallId(),
                flowPublicId,
                language
        );

        /*
         * Pass the trusted Agent runtime configuration into the
         * Flow runtime context.
         */
        CallSessionResponseDto flowSession =
                callSessionFlowRuntimeService.startFlow(
                        request.getCallId(),
                        flowPublicId,
                        language,
                        buildInitialContext(
                                request,
                                session,
                                runtimeConfiguration
                        )
                );

        if (flowSession == null) {

            log.error(
                    "Flow runtime returned null Call Session. " +
                            "callId={}, flowPublicId={}",
                    request.getCallId(),
                    flowPublicId
            );

            throw new IllegalStateException(
                    ConversationOrchestratorMessages
                            .FLOW_EXECUTION_RESULT_EMPTY
            );
        }

        log.info(
                "Flow Execution started successfully. " +
                        "callId={}, flowExecutionPublicId={}",
                request.getCallId(),
                flowSession.getFlowExecutionPublicId()
        );

        FlowExecutionResult execution =
                getFlowExecution(
                        flowSession
                );

        synchronizeSession(
                request.getCallId(),
                execution,
                language
        );

        log.info(
                "Conversation session started successfully. " +
                        "callId={}, flowExecutionPublicId={}, " +
                        "currentNode={}",
                request.getCallId(),
                execution.getExecutionPublicId(),
                execution.getCurrentNodeKey()
        );

        return conversationResponseService
                .buildResponse(
                        request.getCallId(),
                        null,
                        execution
                );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ConversationOrchestratorResponseDto endConversation(
            EndConversationRequestDto request) {

        validateEndRequest(
                request
        );

        log.info(
                "Ending conversation session. callId={}, reason={}",
                request.getCallId(),
                request.getReason()
        );

        CallSessionResponseDto session =
                getRequiredSession(
                        request.getCallId()
                );

        /*
         * Make the operation idempotent.
         */
        if (CallSessionStatus.ENDED.equals(
                session.getStatus()
        )) {

            log.info(
                    "Conversation is already ended. callId={}",
                    request.getCallId()
            );

            return conversationResponseService
                    .buildCompletedResponse(
                            request.getCallId()
                    );
        }

        /*
         * Cancel Flow Execution when one exists.
         */
        if (hasFlowExecution(
                session
        )) {

            try {

                log.info(
                        "Cancelling Flow Execution. " +
                                "callId={}, flowExecutionPublicId={}",
                        request.getCallId(),
                        session.getFlowExecutionPublicId()
                );

                flowExecutionService.cancel(
                        session.getFlowExecutionPublicId()
                );

            } catch (Exception exception) {

                log.warn(
                        "Unable to cancel Flow Execution while " +
                                "ending conversation. callId={}, " +
                                "flowExecutionPublicId={}",
                        request.getCallId(),
                        session.getFlowExecutionPublicId(),
                        exception
                );
            }
        }

        /*
         * Use the existing Call Session update service for
         * conversation termination.
         */
        callSessionUpdateService.endCallSession(
                request.getCallId()
        );

        log.info(
                "Conversation session ended successfully. callId={}",
                request.getCallId()
        );

        return conversationResponseService
                .buildCompletedResponse(
                        request.getCallId()
                );
    }

    // =========================================================
    // SESSION
    // =========================================================

    /**
     * Attempts to find an existing Call Session.
     *
     * <p>
     * This method deliberately does not require an authenticated
     * user because it is used by the live telephony runtime.
     * </p>
     *
     * @param callId call identifier
     * @return existing Call Session or null
     */
    private CallSessionResponseDto findExistingSession(
            String callId) {

        try {

            CallSessionResponseDto session =
                    callSessionGetService.getCallSession(
                            callId
                    );

            if (session == null) {

                log.warn(
                        "Call Session lookup returned null. callId={}",
                        callId
                );

                return null;
            }

            log.info(
                    "Existing Call Session found. " +
                            "callId={}, sessionPublicId={}, " +
                            "status={}, flowPublicId={}, " +
                            "flowExecutionPublicId={}",
                    callId,
                    session.getCallId(),
                    session.getStatus(),
                    session.getFlowPublicId(),
                    session.getFlowExecutionPublicId()
            );

            return session;

        } catch (ResourceNotFoundException exception) {

            log.info(
                    "Call Session not found. callId={}",
                    callId
            );

            return null;
        }
    }

    /**
     * Creates a Call Session for an authenticated API invocation.
     *
     * @param request start request
     * @param currentUserId authenticated user ID
     * @return created Call Session
     */
    private CallSessionResponseDto createSession(
            StartConversationRequestDto request,
            Long currentUserId) {

        log.info(
                "Creating Call Session. " +
                        "callId={}, tenantId={}, agentId={}, " +
                        "flowPublicId={}",
                request.getCallId(),
                request.getTenantId(),
                request.getAgentId(),
                request.getFlowPublicId()
        );

        CallSessionResponseDto session =
                callSessionRuntimeService.startSession(
                        request.getCallId(),
                        request.getTenantId(),
                        request.getAgentId(),
                        resolveAgentVersion(
                                request.getAgentVersion()
                        ),
                        request.getFlowPublicId(),
                        resolveLanguage(
                                request.getLanguage()
                        ),
                        currentUserId
                );

        if (session == null) {

            log.error(
                    "Call Session creation returned null. callId={}",
                    request.getCallId()
            );

            throw new IllegalStateException(
                    ConversationOrchestratorMessages
                            .CONVERSATION_START_FAILED
            );
        }

        log.info(
                "Call Session created successfully. " +
                        "callId={}, sessionPublicId={}, " +
                        "flowPublicId={}, status={}",
                request.getCallId(),
                session.getCallId(),
                session.getFlowPublicId(),
                session.getStatus()
        );

        return session;
    }

    /**
     * Resolves the authenticated user only when one is actually
     * available.
     *
     * <p>
     * Live provider callbacks are allowed to execute without an
     * authenticated user.
     * </p>
     *
     * @return authenticated user ID or null
     */
    private Long resolveAuthenticatedUserId() {

        try {

            if (!currentUserService.isAuthenticated()) {

                return null;
            }

            return currentUserService
                    .getCurrentUserId();

        } catch (Exception exception) {

            log.debug(
                    "Unable to resolve authenticated user " +
                            "for conversation runtime.",
                    exception
            );

            return null;
        }
    }

    /**
     * Retrieves the required Call Session.
     *
     * @param callId call identifier
     * @return Call Session
     */
    private CallSessionResponseDto getRequiredSession(
            String callId) {

        try {

            CallSessionResponseDto session =
                    callSessionGetService.getCallSession(
                            callId
                    );

            if (session == null) {

                log.warn(
                        "Call Session lookup returned null. callId={}",
                        callId
                );

                throw new IllegalStateException(
                        ConversationOrchestratorMessages
                                .CONVERSATION_NOT_FOUND
                );
            }

            return session;

        } catch (ResourceNotFoundException exception) {

            log.warn(
                    "Call Session not found. callId={}",
                    callId
            );

            throw new IllegalStateException(
                    ConversationOrchestratorMessages
                            .CONVERSATION_NOT_FOUND
            );
        }
    }

    // =========================================================
    // FLOW
    // =========================================================

    /**
     * Validates that the requested Flow matches the Flow bound
     * to the Call Session.
     *
     * @param request start request
     * @param session Call Session
     */
    private void validateFlowBinding(
            StartConversationRequestDto request,
            CallSessionResponseDto session) {

        String requestedFlowPublicId =
                request.getFlowPublicId();

        String sessionFlowPublicId =
                session.getFlowPublicId();

        /*
         * The persisted Call Session is authoritative.
         *
         * If it contains a Flow ID, a different Flow requested by
         * another runtime caller must not replace it.
         */
        if (sessionFlowPublicId != null
                && !sessionFlowPublicId.isBlank()
                && !sessionFlowPublicId.equals(
                requestedFlowPublicId
        )) {

            log.error(
                    "Flow mismatch detected for Call Session. " +
                            "callId={}, requestedFlowPublicId={}, " +
                            "sessionFlowPublicId={}",
                    request.getCallId(),
                    requestedFlowPublicId,
                    sessionFlowPublicId
            );

            throw new BadRequestException(
                    ConversationOrchestratorMessages
                            .FLOW_PUBLIC_ID_REQUIRED
            );
        }
    }

    /**
     * Resolves the Flow ID used by the runtime.
     *
     * <p>
     * For an existing Call Session the persisted Flow is preferred.
     * This prevents the live provider callback from changing the
     * Flow assigned when the call was created.
     * </p>
     *
     * @param request start request
     * @param session Call Session
     * @return Flow public ID
     */
    private String resolveFlowPublicId(
            StartConversationRequestDto request,
            CallSessionResponseDto session) {

        if (session != null
                && session.getFlowPublicId() != null
                && !session.getFlowPublicId().isBlank()) {

            return session.getFlowPublicId();
        }

        return request.getFlowPublicId();
    }

    /**
     * Retrieves the current Flow Execution.
     *
     * @param session Call Session
     * @return Flow execution
     */
    private FlowExecutionResult getFlowExecution(
            CallSessionResponseDto session) {

        if (!hasFlowExecution(
                session
        )) {

            log.error(
                    "Flow Execution is missing from Call Session. " +
                            "callId={}, flowExecutionPublicId={}",
                    session == null
                            ? null
                            : session.getCallId(),
                    session == null
                            ? null
                            : session.getFlowExecutionPublicId()
            );

            throw new IllegalStateException(
                    ConversationOrchestratorMessages
                            .ACTIVE_FLOW_EXECUTION_NOT_FOUND
            );
        }

        FlowExecutionResult execution =
                flowExecutionService.getExecution(
                        session.getFlowExecutionPublicId()
                );

        if (execution == null) {

            log.error(
                    "Flow Execution lookup returned null. " +
                            "callId={}, flowExecutionPublicId={}",
                    session.getCallId(),
                    session.getFlowExecutionPublicId()
            );

            throw new IllegalStateException(
                    ConversationOrchestratorMessages
                            .FLOW_EXECUTION_RESULT_EMPTY
            );
        }

        return execution;
    }

    // =========================================================
    // SESSION SYNCHRONIZATION
    // =========================================================

    /**
     * Synchronizes Call Session state with Flow Execution state.
     *
     * @param callId call identifier
     * @param execution Flow execution
     * @param language conversation language
     */
    private void synchronizeSession(
            String callId,
            FlowExecutionResult execution,
            String language) {

        if (execution == null) {

            log.warn(
                    "Skipping Call Session synchronization because " +
                            "Flow Execution is null. callId={}",
                    callId
            );

            return;
        }

        callSessionUpdateService.updateCallSession(
                callId,
                UpdateCallSessionRequestDto.builder()
                        .language(
                                resolveLanguage(
                                        language
                                )
                        )
                        .flowExecutionPublicId(
                                execution.getExecutionPublicId()
                        )
                        .build()
        );

        if (execution.getCurrentNodeKey() != null
                && !execution.getCurrentNodeKey().isBlank()) {

            callSessionFlowService.updateFlowState(
                    callId,
                    UpdateFlowStateRequestDto.builder()
                            .flowNodeId(
                                    execution.getCurrentNodeKey()
                            )
                            .build()
            );
        }

        log.debug(
                "Call Session synchronized with Flow Execution. " +
                        "callId={}, executionPublicId={}, " +
                        "currentNode={}, status={}",
                callId,
                execution.getExecutionPublicId(),
                execution.getCurrentNodeKey(),
                execution.getStatus()
        );
    }

    // =========================================================
    // CONTEXT
    // =========================================================

    /**
     * Builds the initial Flow runtime context.
     *
     * <p>
     * The context contains trusted Call Session information and
     * the resolved Agent Configuration required by Flow nodes.
     * </p>
     *
     * <p>
     * Caller-provided context is accepted for additional runtime
     * variables, but trusted runtime configuration values are
     * written afterwards so that external input cannot override
     * the Agent configuration resolved by the backend.
     * </p>
     *
     * @param request conversation start request
     * @param session Call Session
     * @param runtimeConfiguration resolved Agent runtime configuration
     * @return initial Flow context
     */
    private Map<String, Object> buildInitialContext(
            StartConversationRequestDto request,
            CallSessionResponseDto session,
            ConversationRuntimeConfigurationResponseDto
                    runtimeConfiguration) {

        Map<String, Object> context =
                new HashMap<>();

        /*
         * Preserve caller-provided custom context.
         *
         * Trusted runtime values are added after this block.
         */
        if (request.getContext() != null) {

            context.putAll(
                    request.getContext()
            );
        }

        /*
         * Call information.
         */
        context.put(
                CALL_ID,
                request.getCallId()
        );

        /*
         * Language is taken from the persisted Call Session.
         *
         * The Call Session is the runtime source of truth for the
         * active conversation.
         */
        context.put(
                LANGUAGE,
                resolveLanguage(
                        session.getLanguage()
                )
        );

        /*
         * Flow information.
         */
        if (session.getFlowPublicId() != null
                && !session.getFlowPublicId().isBlank()) {

            context.put(
                    FLOW_PUBLIC_ID,
                    session.getFlowPublicId()
            );
        }

        /*
         * Tenant information.
         */
        if (session.getTenantId() != null) {

            context.put(
                    TENANT_ID,
                    session.getTenantId()
            );
        }

        /*
         * Agent information.
         */
        if (session.getAgentId() != null) {

            context.put(
                    AGENT_ID,
                    session.getAgentId()
            );
        }

        /*
         * Add the resolved Agent Configuration.
         *
         * This configuration was resolved by the backend before
         * the conversation reached this service.
         */
        if (runtimeConfiguration != null) {

            Map<String, Object> configuration =
                    new HashMap<>();

            configuration.put(
                    STT_PROVIDER,
                    runtimeConfiguration.getSttProvider()
            );

            configuration.put(
                    STT_MODEL,
                    runtimeConfiguration.getSttModel()
            );

            configuration.put(
                    LLM_PROVIDER,
                    runtimeConfiguration.getLlmProvider()
            );

            configuration.put(
                    LLM_MODEL,
                    runtimeConfiguration.getLlmModel()
            );

            configuration.put(
                    TTS_PROVIDER,
                    runtimeConfiguration.getTtsProvider()
            );

            configuration.put(
                    TTS_MODEL,
                    runtimeConfiguration.getTtsModel()
            );

            configuration.put(
                    CONFIGURATION_LANGUAGE,
                    runtimeConfiguration.getLanguage()
            );

            configuration.put(
                    VOICE,
                    runtimeConfiguration.getVoice()
            );

            configuration.put(
                    SYSTEM_PROMPT,
                    runtimeConfiguration.getSystemPrompt()
            );

            configuration.put(
                    TEMPERATURE,
                    runtimeConfiguration.getTemperature()
            );

            configuration.put(
                    MAX_TOKENS,
                    runtimeConfiguration.getMaxTokens()
            );

            /*
             * The complete resolved configuration is stored under
             * one runtime context object.
             */
            context.put(
                    RUNTIME_CONFIGURATION,
                    configuration
            );

            log.debug(
                    "Agent runtime configuration added to Flow context. " +
                            "callId={}, tenantId={}, agentId={}, " +
                            "flowPublicId={}, sttProvider={}, sttModel={}, " +
                            "llmProvider={}, llmModel={}, ttsProvider={}, " +
                            "ttsModel={}, language={}, voice={}",
                    request.getCallId(),
                    runtimeConfiguration.getTenantId(),
                    runtimeConfiguration.getAgentId(),
                    runtimeConfiguration.getFlowPublicId(),
                    runtimeConfiguration.getSttProvider(),
                    runtimeConfiguration.getSttModel(),
                    runtimeConfiguration.getLlmProvider(),
                    runtimeConfiguration.getLlmModel(),
                    runtimeConfiguration.getTtsProvider(),
                    runtimeConfiguration.getTtsModel(),
                    runtimeConfiguration.getLanguage(),
                    runtimeConfiguration.getVoice()
            );
        } else {

            /*
             * This should normally not happen because the
             * Conversation Orchestrator resolves the configuration
             * before starting the conversation.
             */
            log.warn(
                    "No Agent runtime configuration was provided " +
                            "while building Flow context. callId={}, " +
                            "agentId={}, flowPublicId={}",
                    request.getCallId(),
                    request.getAgentId(),
                    request.getFlowPublicId()
            );
        }

        return context;
    }

    // =========================================================
    // VALIDATION
    // =========================================================

    /**
     * Validates an active Call Session.
     *
     * @param session Call Session
     */
    private void validateActiveSession(
            CallSessionResponseDto session) {

        if (session == null) {

            throw new IllegalStateException(
                    ConversationOrchestratorMessages
                            .CONVERSATION_NOT_FOUND
            );
        }

        if (CallSessionStatus.ENDED.equals(
                session.getStatus()
        )) {

            log.warn(
                    "Conversation is already ended. callId={}",
                    session.getCallId()
            );

            throw new BadRequestException(
                    ConversationOrchestratorMessages
                            .CONVERSATION_ALREADY_ENDED
            );
        }
    }

    /**
     * Checks whether a Flow Execution exists.
     *
     * @param session Call Session
     * @return true when Flow Execution exists
     */
    private boolean hasFlowExecution(
            CallSessionResponseDto session) {

        return session != null
                && session.getFlowExecutionPublicId() != null
                && !session.getFlowExecutionPublicId().isBlank();
    }

    /**
     * Resolves agent version.
     *
     * @param agentVersion requested version
     * @return valid agent version
     */
    private Integer resolveAgentVersion(
            Integer agentVersion) {

        return agentVersion == null
                ? 1
                : agentVersion;
    }

    /**
     * Resolves conversation language.
     *
     * @param language requested language
     * @return resolved language
     */
    private String resolveLanguage(
            String language) {

        return language == null
                || language.isBlank()
                ? ConversationOrchestratorConstants
                .DEFAULT_LANGUAGE
                : language;
    }

    /**
     * Validates start request.
     *
     * @param request start request
     */
    private void validateStartRequest(
            StartConversationRequestDto request) {

        if (request == null) {

            throw new BadRequestException(
                    ConversationOrchestratorMessages
                            .CONVERSATION_START_FAILED
            );
        }

        if (isBlank(
                request.getCallId()
        )) {

            throw new BadRequestException(
                    ConversationOrchestratorMessages
                            .CALL_ID_REQUIRED
            );
        }

        if (isBlank(
                request.getTenantId()
        )) {

            throw new BadRequestException(
                    ConversationOrchestratorMessages
                            .TENANT_ID_REQUIRED
            );
        }

        if (isBlank(
                request.getAgentId()
        )) {

            throw new BadRequestException(
                    ConversationOrchestratorMessages
                            .AGENT_ID_REQUIRED
            );
        }

        if (isBlank(
                request.getFlowPublicId()
        )) {

            throw new BadRequestException(
                    ConversationOrchestratorMessages
                            .FLOW_PUBLIC_ID_REQUIRED
            );
        }
    }

    /**
     * Validates end request.
     *
     * @param request end request
     */
    private void validateEndRequest(
            EndConversationRequestDto request) {

        if (request == null
                || isBlank(
                request.getCallId()
        )) {

            throw new BadRequestException(
                    ConversationOrchestratorMessages
                            .CALL_ID_REQUIRED
            );
        }
    }

    /**
     * Checks whether a string is blank.
     *
     * @param value string
     * @return true when blank
     */
    private boolean isBlank(
            String value) {

        return value == null
                || value.isBlank();
    }
}
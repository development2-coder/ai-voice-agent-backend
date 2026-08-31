package com.infinitio.aivoiceplatform.orchestrator.service.impl;

import com.infinitio.aivoiceplatform.orchestrator.dto.request.BargeInRequestDto;
import com.infinitio.aivoiceplatform.orchestrator.dto.request.EndConversationRequestDto;
import com.infinitio.aivoiceplatform.orchestrator.dto.request.ProcessAudioRequestDto;
import com.infinitio.aivoiceplatform.orchestrator.dto.request.ProcessDtmfRequestDto;
import com.infinitio.aivoiceplatform.orchestrator.dto.request.ProcessTranscriptRequestDto;
import com.infinitio.aivoiceplatform.orchestrator.dto.request.StartConversationRequestDto;
import com.infinitio.aivoiceplatform.orchestrator.dto.response.ConversationOrchestratorResponseDto;
import com.infinitio.aivoiceplatform.orchestrator.dto.response.ConversationRuntimeConfigurationResponseDto;
import com.infinitio.aivoiceplatform.orchestrator.service.ConversationAiService;
import com.infinitio.aivoiceplatform.orchestrator.service.ConversationInputService;
import com.infinitio.aivoiceplatform.orchestrator.service.ConversationOrchestratorService;
import com.infinitio.aivoiceplatform.orchestrator.service.ConversationResponseService;
import com.infinitio.aivoiceplatform.orchestrator.service.ConversationRuntimeConfigurationService;
import com.infinitio.aivoiceplatform.orchestrator.service.ConversationSessionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Main Conversation Orchestrator implementation.
 *
 * <p>
 * This class coordinates the conversation runtime lifecycle.
 * Business logic is delegated to dedicated conversation services.
 * </p>
 *
 * <p>
 * The orchestrator validates and resolves the tenant-specific
 * runtime configuration before starting a conversation.
 * Runtime behaviour is determined by the configured Tenant,
 * Agent, Agent Configuration and Flow.
 * </p>
 *
 * <p>
 * The orchestrator does not directly implement STT, LLM, TTS,
 * Flow execution or Call Session persistence logic.
 * Those responsibilities remain delegated to their respective
 * services.
 * </p>
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ConversationOrchestratorServiceImpl
        implements ConversationOrchestratorService {

    private final ConversationSessionService
            conversationSessionService;

    private final ConversationInputService
            conversationInputService;

    private final ConversationAiService
            conversationAiService;

    private final ConversationResponseService
            conversationResponseService;

    private final ConversationRuntimeConfigurationService
            conversationRuntimeConfigurationService;

    /**
     * Starts a conversation.
     *
     * <p>
     * The runtime configuration is resolved and validated before
     * the conversation session is created or reused.
     * </p>
     *
     * <p>
     * The resolved configuration is currently used to establish
     * the trusted runtime context and to guarantee that the
     * requested tenant, agent and flow are valid.
     * The existing ConversationSessionService remains responsible
     * for starting or resuming the Call Session and Flow Execution.
     * </p>
     *
     * @param request conversation start request
     * @return conversation response
     */
    @Override
    public ConversationOrchestratorResponseDto start(
            StartConversationRequestDto request) {

        log.info(
                "Starting conversation orchestration. " +
                        "callId={}, tenantId={}, agentId={}, " +
                        "agentVersion={}, flowPublicId={}, language={}",
                request == null
                        ? null
                        : request.getCallId(),
                request == null
                        ? null
                        : request.getTenantId(),
                request == null
                        ? null
                        : request.getAgentId(),
                request == null
                        ? null
                        : request.getAgentVersion(),
                request == null
                        ? null
                        : request.getFlowPublicId(),
                request == null
                        ? null
                        : request.getLanguage()
        );

        validateStartRequest(
                request
        );

        /*
         * Resolve and validate the complete tenant-specific
         * runtime configuration.
         *
         * This prevents an Agent or Flow belonging to another
         * Tenant from entering the conversation runtime.
         */
        ConversationRuntimeConfigurationResponseDto
                runtimeConfiguration =
                conversationRuntimeConfigurationService
                        .resolveRuntimeConfiguration(
                                request.getTenantId(),
                                request.getAgentId(),
                                request.getAgentVersion(),
                                request.getFlowPublicId()
                        );

        log.info(
                "Conversation runtime configuration resolved. " +
                        "callId={}, tenantId={}, agentId={}, " +
                        "agentVersion={}, flowPublicId={}, language={}",
                request.getCallId(),
                runtimeConfiguration.getTenantId(),
                runtimeConfiguration.getAgentId(),
                runtimeConfiguration.getAgentVersion(),
                runtimeConfiguration.getFlowPublicId(),
                runtimeConfiguration.getLanguage()
        );

        /*
         * The request has already passed runtime configuration
         * validation. Keep ConversationSessionService as the
         * existing session/Flow runtime boundary.
         */
        ConversationOrchestratorResponseDto response =
                conversationSessionService
                        .startConversation(
                                request
                        );

        log.info(
                "Conversation orchestration started successfully. " +
                        "callId={}, tenantId={}, agentId={}, " +
                        "flowPublicId={}",
                request.getCallId(),
                runtimeConfiguration.getTenantId(),
                runtimeConfiguration.getAgentId(),
                runtimeConfiguration.getFlowPublicId()
        );

        return response;
    }

    /**
     * Processes caller audio.
     *
     * <p>
     * Audio processing is delegated to the conversation input
     * service. The input service is responsible for forwarding
     * audio into the configured STT/runtime pipeline.
     * </p>
     *
     * @param request audio request
     * @return conversation response
     */
    @Override
    public ConversationOrchestratorResponseDto processAudio(
            ProcessAudioRequestDto request) {

        log.info(
                "Delegating conversation audio processing. " +
                        "callId={}",
                request == null
                        ? null
                        : request.getCallId()
        );

        validateRequest(
                request,
                "Audio processing request is required."
        );

        ConversationOrchestratorResponseDto response =
                conversationInputService
                        .processAudio(
                                request
                        );

        log.debug(
                "Conversation audio processing completed. " +
                        "callId={}",
                request.getCallId()
        );

        return response;
    }

    /**
     * Processes caller transcript.
     *
     * <p>
     * Transcript processing is delegated to the conversation
     * input service. AI processing is performed when the current
     * Flow reaches an AI waiting state.
     * </p>
     *
     * @param request transcript request
     * @return conversation response
     */
    @Override
    public ConversationOrchestratorResponseDto processTranscript(
            ProcessTranscriptRequestDto request) {

        log.info(
                "Delegating conversation transcript processing. " +
                        "callId={}",
                request == null
                        ? null
                        : request.getCallId()
        );

        validateRequest(
                request,
                "Transcript processing request is required."
        );

        ConversationOrchestratorResponseDto response =
                conversationInputService
                        .processTranscript(
                                request
                        );

        log.debug(
                "Conversation transcript processing completed. " +
                        "callId={}",
                request.getCallId()
        );

        return response;
    }

    /**
     * Processes DTMF input.
     *
     * <p>
     * DTMF processing is delegated to the conversation input
     * service so that DTMF follows the same conversation and
     * Flow execution lifecycle.
     * </p>
     *
     * @param request DTMF request
     * @return conversation response
     */
    @Override
    public ConversationOrchestratorResponseDto processDtmf(
            ProcessDtmfRequestDto request) {

        log.info(
                "Delegating DTMF processing. callId={}",
                request == null
                        ? null
                        : request.getCallId()
        );

        validateRequest(
                request,
                "DTMF processing request is required."
        );

        ConversationOrchestratorResponseDto response =
                conversationInputService
                        .processDtmf(
                                request
                        );

        log.debug(
                "DTMF processing completed. callId={}",
                request.getCallId()
        );

        return response;
    }

    /**
     * Processes caller barge-in.
     *
     * <p>
     * Barge-in processing is delegated to the input service.
     * Transport-level audio interruption remains the
     * responsibility of the Voice Gateway.
     * </p>
     *
     * @param request barge-in request
     * @return conversation response
     */
    @Override
    public ConversationOrchestratorResponseDto processBargeIn(
            BargeInRequestDto request) {

        log.info(
                "Delegating conversation barge-in. callId={}",
                request == null
                        ? null
                        : request.getCallId()
        );

        validateRequest(
                request,
                "Barge-in request is required."
        );

        ConversationOrchestratorResponseDto response =
                conversationInputService
                        .processBargeIn(
                                request
                        );

        log.debug(
                "Conversation barge-in processing completed. " +
                        "callId={}",
                request.getCallId()
        );

        return response;
    }

    /**
     * Ends the conversation.
     *
     * <p>
     * Conversation termination is delegated to the session
     * service so that Call Session and Flow Execution state
     * are finalized consistently.
     * </p>
     *
     * @param request end conversation request
     * @return conversation response
     */
    @Override
    public ConversationOrchestratorResponseDto end(
            EndConversationRequestDto request) {

        log.info(
                "Delegating conversation termination. " +
                        "callId={}, reason={}",
                request == null
                        ? null
                        : request.getCallId(),
                request == null
                        ? null
                        : request.getReason()
        );

        validateRequest(
                request,
                "End conversation request is required."
        );

        ConversationOrchestratorResponseDto response =
                conversationSessionService
                        .endConversation(
                                request
                        );

        log.info(
                "Conversation termination completed. callId={}",
                request.getCallId()
        );

        return response;
    }

    // =========================================================
    // START VALIDATION
    // =========================================================

    /**
     * Validates the conversation start request.
     *
     * <p>
     * This method performs only basic request validation.
     * Tenant, Agent, Agent Configuration and Flow validation
     * is delegated to ConversationRuntimeConfigurationService.
     * </p>
     *
     * @param request start request
     */
    private void validateStartRequest(
            StartConversationRequestDto request) {

        if (request == null) {

            log.warn(
                    "Conversation start request is null."
            );

            throw new IllegalArgumentException(
                    "Conversation start request is required."
            );
        }

        if (request.getCallId() == null
                || request.getCallId().isBlank()) {

            log.warn(
                    "Conversation start request has no call ID."
            );

            throw new IllegalArgumentException(
                    "Call ID is required."
            );
        }

        if (request.getTenantId() == null
                || request.getTenantId().isBlank()) {

            log.warn(
                    "Conversation start request has no tenant ID. " +
                            "callId={}",
                    request.getCallId()
            );

            throw new IllegalArgumentException(
                    "Tenant ID is required."
            );
        }

        if (request.getAgentId() == null
                || request.getAgentId().isBlank()) {

            log.warn(
                    "Conversation start request has no agent ID. " +
                            "callId={}",
                    request.getCallId()
            );

            throw new IllegalArgumentException(
                    "Agent ID is required."
            );
        }

        if (request.getFlowPublicId() == null
                || request.getFlowPublicId().isBlank()) {

            log.warn(
                    "Conversation start request has no Flow ID. " +
                            "callId={}",
                    request.getCallId()
            );

            throw new IllegalArgumentException(
                    "Flow ID is required."
            );
        }

        if (request.getAgentVersion() != null
                && request.getAgentVersion() < 1) {

            log.warn(
                    "Conversation start request contains invalid " +
                            "agent version. callId={}, agentVersion={}",
                    request.getCallId(),
                    request.getAgentVersion()
            );

            throw new IllegalArgumentException(
                    "Agent version must be greater than zero."
            );
        }
    }

    // =========================================================
    // COMMON VALIDATION
    // =========================================================

    /**
     * Performs null validation for delegated requests.
     *
     * @param request request object
     * @param message validation message
     */
    private void validateRequest(
            Object request,
            String message) {

        if (request == null) {

            log.warn(
                    "Conversation request validation failed. " +
                            "message={}",
                    message
            );

            throw new IllegalArgumentException(
                    message
            );
        }
    }
}
package com.infinitio.aivoiceplatform.orchestrator.service.impl;

import com.infinitio.aivoiceplatform.call.entity.Call;
import com.infinitio.aivoiceplatform.call.repository.CallRepository;
import com.infinitio.aivoiceplatform.callsession.constant.CallSessionStatus;
import com.infinitio.aivoiceplatform.callsession.dto.request.AddConversationMessageRequestDto;
import com.infinitio.aivoiceplatform.callsession.dto.response.CallSessionResponseDto;
import com.infinitio.aivoiceplatform.callsession.service.CallSessionConversationService;
import com.infinitio.aivoiceplatform.callsession.service.CallSessionGetService;
import com.infinitio.aivoiceplatform.exception.BadRequestException;
import com.infinitio.aivoiceplatform.exception.ResourceNotFoundException;
import com.infinitio.aivoiceplatform.flow.constant.FlowExecutionContextKeys;
import com.infinitio.aivoiceplatform.flow.dto.request.ContinueFlowExecutionRequest;
import com.infinitio.aivoiceplatform.flow.dto.response.FlowExecutionResult;
import com.infinitio.aivoiceplatform.flow.service.FlowExecutionService;
import com.infinitio.aivoiceplatform.orchestrator.constant.ConversationOrchestratorConstants;
import com.infinitio.aivoiceplatform.orchestrator.constant.ConversationOrchestratorMessages;
import com.infinitio.aivoiceplatform.orchestrator.dto.request.BargeInRequestDto;
import com.infinitio.aivoiceplatform.orchestrator.dto.request.EndConversationRequestDto;
import com.infinitio.aivoiceplatform.orchestrator.dto.request.ProcessAudioRequestDto;
import com.infinitio.aivoiceplatform.orchestrator.dto.request.ProcessDtmfRequestDto;
import com.infinitio.aivoiceplatform.orchestrator.dto.request.ProcessTranscriptRequestDto;
import com.infinitio.aivoiceplatform.orchestrator.dto.response.ConversationOrchestratorResponseDto;
import com.infinitio.aivoiceplatform.orchestrator.service.ConversationAiService;
import com.infinitio.aivoiceplatform.orchestrator.service.ConversationInputService;
import com.infinitio.aivoiceplatform.orchestrator.service.ConversationResponseService;
import com.infinitio.aivoiceplatform.orchestrator.service.ConversationSessionService;
import com.infinitio.aivoiceplatform.stt.dto.runtime.SttTranscriptionRequest;
import com.infinitio.aivoiceplatform.stt.dto.runtime.SttTranscriptionResponse;
import com.infinitio.aivoiceplatform.stt.service.SttRuntimeService;
import com.infinitio.aivoiceplatform.telephony.dto.request.HangupCallRequestDto;
import com.infinitio.aivoiceplatform.telephony.service.TelephonyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.concurrent.TimeUnit;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * Default implementation of Conversation Input Service.
 *
 * <p>
 * Handles caller audio, STT transcription, transcript processing,
 * DTMF input and barge-in events.
 * </p>
 *
 * <p>
 * Flow execution remains responsible for deciding which node
 * executes next. This service only supplies the caller input
 * to the Flow runtime.
 * </p>
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ConversationInputServiceImpl
        implements ConversationInputService {

    private static final String LANGUAGE =
            "language";

    private static final String DTMF =
            "dtmf";

    private final CallSessionGetService
            callSessionGetService;

    private final CallSessionConversationService
            callSessionConversationService;

    /**
     * Handles conversation session lifecycle operations.
     */
    private final ConversationSessionService
            conversationSessionService;

    /**
     * Provides access to the persisted Call entity.
     */
    private final CallRepository
            callRepository;

    /**
     * Performs provider-independent telephony operations.
     */
    private final TelephonyService
            telephonyService;

    private final FlowExecutionService
            flowExecutionService;

    private final SttRuntimeService
            sttRuntimeService;

    private final ConversationAiService
            conversationAiService;

    private final ConversationResponseService
            conversationResponseService;

    /**
     * {@inheritDoc}
     */
    @Override
    public ConversationOrchestratorResponseDto processAudio(
            ProcessAudioRequestDto request) {

        validateAudioRequest(
                request
        );

        log.info(
                "Processing caller audio. callId={}, contentType={}, " +
                        "audioSizeBytes={}, finalTranscript={}",
                request.getCallId(),
                request.getContentType(),
                request.getAudioBase64().length(),
                request.isFinalTranscript()
        );

        byte[] audio =
                decodeAudio(
                        request
                );

        String language =
                resolveLanguage(
                        request.getLanguage()
                );

        long startTime =
                System.currentTimeMillis();

        SttTranscriptionResponse transcription =
                sttRuntimeService.transcribe(
                        SttTranscriptionRequest.builder()
                                .callId(
                                        request.getCallId()
                                )
                                .audio(
                                        audio
                                )
                                .contentType(
                                        request.getContentType()
                                )
                                .fileName(
                                        request.getFileName()
                                )
                                .language(
                                        language
                                )
                                .finalTranscript(
                                        request.isFinalTranscript()
                                )
                                .build()
                );

        long latencyMs =
                System.currentTimeMillis()
                        - startTime;

        if (transcription == null) {

            log.error(
                    "STT returned null response. callId={}, latencyMs={}",
                    request.getCallId(),
                    latencyMs
            );

            throw new IllegalStateException(
                    ConversationOrchestratorMessages
                            .STT_RESPONSE_EMPTY
            );
        }

        log.info(
                "STT processing completed. callId={}, provider={}, " +
                        "language={}, finalTranscript={}, transcriptLength={}, " +
                        "latencyMs={}",
                request.getCallId(),
                transcription.getProvider(),
                transcription.getLanguage(),
                transcription.isFinalTranscript(),
                transcription.getTranscript() == null
                        ? 0
                        : transcription.getTranscript().length(),
                latencyMs
        );

        /*
         * Interim STT result.
         *
         * Do not continue the Flow until the transcription
         * becomes final.
         */
        if (!transcription.isFinalTranscript()) {

            log.debug(
                    "Interim STT transcript received. " +
                            "Waiting for final transcript. callId={}",
                    request.getCallId()
            );

            return ConversationOrchestratorResponseDto.builder()
                    .callId(
                            request.getCallId()
                    )
                    .transcript(
                            transcription.getTranscript()
                    )
                    .action(
                            ConversationOrchestratorConstants
                                    .ACTION_LISTEN
                    )
                    .waitingForUser(
                            true
                    )
                    .build();
        }

        return processTranscript(
                ProcessTranscriptRequestDto.builder()
                        .callId(
                                request.getCallId()
                        )
                        .transcript(
                                transcription.getTranscript()
                        )
                        .language(
                                transcription.getLanguage()
                        )
                        .finalTranscript(
                                true
                        )
                        .build()
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ConversationOrchestratorResponseDto processTranscript(
            ProcessTranscriptRequestDto request) {

        validateTranscriptRequest(
                request
        );

        log.info(
                "Processing caller transcript. callId={}, language={}, " +
                        "finalTranscript={}, transcriptLength={}",
                request.getCallId(),
                request.getLanguage(),
                request.isFinalTranscript(),
                request.getTranscript().length()
        );

        CallSessionResponseDto session =
                getRequiredSession(
                        request.getCallId()
                );

        validateActiveSession(
                session
        );

        /*
         * Interim transcript must not advance the Flow.
         */
        if (!request.isFinalTranscript()) {

            log.debug(
                    "Ignoring interim transcript for Flow continuation. " +
                            "callId={}",
                    request.getCallId()
            );

            return ConversationOrchestratorResponseDto.builder()
                    .callId(
                            request.getCallId()
                    )
                    .transcript(
                            request.getTranscript()
                    )
                    .action(
                            ConversationOrchestratorConstants
                                    .ACTION_LISTEN
                    )
                    .waitingForUser(
                            true
                    )
                    .build();
        }

        /*
         * Store caller's final message.
         */
        /*
         * The Flow Execution is started asynchronously when the call
         * begins. The first STT transcript can arrive before that
         * asynchronous startup has completed.
         *
         * Wait briefly for the Flow Execution before processing the
         * caller transcript.
         */
        session =
                waitForFlowExecution(
                        request.getCallId(),
                        session
                );

        String executionPublicId =
                session.getFlowExecutionPublicId();

        if (executionPublicId == null
                || executionPublicId.isBlank()) {

            log.error(
                    "Active Flow Execution is missing after startup wait. " +
                            "callId={}, sessionPublicId={}",
                    request.getCallId(),
                    session.getCallId()
            );

            throw new IllegalStateException(
                    ConversationOrchestratorMessages
                            .ACTIVE_FLOW_EXECUTION_NOT_FOUND
            );
        }

        /*
         * Store caller's final message only after a valid Flow Execution
         * is available. This prevents an unprocessed transcript from
         * being persisted during the startup race.
         */
        callSessionConversationService
                .addConversationMessage(
                        request.getCallId(),
                        AddConversationMessageRequestDto.builder()
                                .role(
                                        ConversationOrchestratorConstants
                                                .ROLE_USER
                                )
                                .text(
                                        request.getTranscript()
                                )
                                .build()
                );

        log.debug(
                "Caller transcript stored. callId={}",
                request.getCallId()
        );

        if (executionPublicId == null
                || executionPublicId.isBlank()) {

            log.error(
                    "Active Flow Execution is missing. " +
                            "callId={}, sessionPublicId={}",
                    request.getCallId(),
                    session.getCallId()
            );

            throw new IllegalStateException(
                    ConversationOrchestratorMessages
                            .ACTIVE_FLOW_EXECUTION_NOT_FOUND
            );
        }

        /*
         * Pass caller input to the Flow Engine.
         *
         * The Flow Engine decides what node should execute next.
         */
        Map<String, Object> flowContext =
                request.getContext() == null
                        ? new HashMap<>()
                        : new HashMap<>(
                        request.getContext()
                );

        String language =
                resolveLanguage(
                        request.getLanguage()
                );

        flowContext.put(
                LANGUAGE,
                language
        );

        /*
         * Check whether the customer explicitly requested
         * conversation termination.
         */
        boolean endConversation =
                isConversationEndRequested(
                        request.getTranscript()
                );

        flowContext.put(
                FlowExecutionContextKeys.END_CONVERSATION,
                endConversation
        );

        /*
         * Customer requested conversation termination.
         *
         * IMPORTANT:
         * Do not continue Flow execution after this point.
         *
         * First terminate the application-level conversation.
         * Then request provider-level telephony hangup.
         */
        if (endConversation) {

            log.info(
                    "Customer requested conversation termination. " +
                            "callId={}, transcript={}",
                    request.getCallId(),
                    request.getTranscript()
            );

            ConversationOrchestratorResponseDto endResponse =
                    conversationSessionService
                            .endConversation(
                                    EndConversationRequestDto.builder()
                                            .callId(
                                                    request.getCallId()
                                            )
                                            .reason(
                                                    "CUSTOMER_CLOSING"
                                            )
                                            .build()
                            );

            requestProviderHangup(
                    request.getCallId()
            );

            return endResponse;
        }

        log.debug(
                "Conversation termination state resolved. " +
                        "callId={}, endConversation={}",
                request.getCallId(),
                endConversation
        );

        /*
         * Normal conversation path.
         *
         * Continue the Flow only when the customer has not
         * requested conversation termination.
         */
        FlowExecutionResult execution =
                flowExecutionService.continueExecution(
                        ContinueFlowExecutionRequest.builder()
                                .executionPublicId(
                                        executionPublicId
                                )
                                .userInput(
                                        request.getTranscript()
                                )
                                .context(
                                        flowContext
                                )
                                .build()
                );

        if (execution == null) {

            log.error(
                    "Flow continuation returned null. " +
                            "callId={}, executionPublicId={}",
                    request.getCallId(),
                    executionPublicId
            );

            throw new IllegalStateException(
                    ConversationOrchestratorMessages
                            .FLOW_EXECUTION_RESULT_EMPTY
            );
        }

        log.info(
                "Flow continued from caller transcript. " +
                        "callId={}, executionPublicId={}, node={}, " +
                        "status={}, waitingForAi={}, waitingForInput={}, " +
                        "completed={}, transferred={}",
                request.getCallId(),
                execution.getExecutionPublicId(),
                execution.getCurrentNodeKey(),
                execution.getStatus(),
                execution.isWaitingForAi(),
                execution.isWaitingForInput(),
                execution.isCompleted(),
                execution.isTransferred()
        );

        /*
         * AI processing is delegated to ConversationAiService.
         *
         * This is important because the input service must not
         * contain LLM implementation logic.
         */
        if (execution.isWaitingForAi()) {

            execution =
                    conversationAiService
                            .processAiWaitingState(
                                    request.getCallId(),
                                    execution
                            );
        }

        /*
         * The Flow itself reached its terminal END state.
         *
         * End the application conversation and then request
         * provider-level hangup.
         */
        if (execution.isCompleted()) {

            log.info(
                    "Flow reached terminal state. Ending call. " +
                            "callId={}, executionPublicId={}",
                    request.getCallId(),
                    execution.getExecutionPublicId()
            );

            ConversationOrchestratorResponseDto endResponse =
                    conversationSessionService
                            .endConversation(
                                    EndConversationRequestDto.builder()
                                            .callId(
                                                    request.getCallId()
                                            )
                                            .reason(
                                                    "FLOW_COMPLETED"
                                            )
                                            .build()
                            );

            requestProviderHangup(
                    request.getCallId()
            );

            return endResponse;
        }

        return conversationResponseService
                .buildResponse(
                        request.getCallId(),
                        request.getTranscript(),
                        execution
                );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ConversationOrchestratorResponseDto processDtmf(
            ProcessDtmfRequestDto request) {

        validateDtmfRequest(
                request
        );

        log.info(
                "Processing DTMF input. callId={}, digitLength={}",
                request.getCallId(),
                request.getDigit().length()
        );

        CallSessionResponseDto session =
                getRequiredSession(
                        request.getCallId()
                );

        validateActiveSession(
                session
        );

        String executionPublicId =
                session.getFlowExecutionPublicId();

        if (executionPublicId == null
                || executionPublicId.isBlank()) {

            log.error(
                    "Cannot process DTMF because Flow Execution is missing. " +
                            "callId={}",
                    request.getCallId()
            );

            throw new IllegalStateException(
                    ConversationOrchestratorMessages
                            .ACTIVE_FLOW_EXECUTION_NOT_FOUND
            );
        }

        Map<String, Object> context =
                new HashMap<>();

        context.put(
                DTMF,
                request.getDigit()
        );

        context.put(
                LANGUAGE,
                resolveLanguage(
                        session.getLanguage()
                )
        );

        FlowExecutionResult execution =
                flowExecutionService.continueExecution(
                        ContinueFlowExecutionRequest.builder()
                                .executionPublicId(
                                        executionPublicId
                                )
                                .context(
                                        context
                                )
                                .build()
                );

        if (execution == null) {

            log.error(
                    "Flow continuation returned null for DTMF. " +
                            "callId={}, executionPublicId={}",
                    request.getCallId(),
                    executionPublicId
            );

            throw new IllegalStateException(
                    ConversationOrchestratorMessages
                            .FLOW_EXECUTION_RESULT_EMPTY
            );
        }

        log.info(
                "DTMF processing completed. callId={}, " +
                        "executionPublicId={}, node={}, status={}",
                request.getCallId(),
                execution.getExecutionPublicId(),
                execution.getCurrentNodeKey(),
                execution.getStatus()
        );

        if (execution.isWaitingForAi()) {

            execution =
                    conversationAiService
                            .processAiWaitingState(
                                    request.getCallId(),
                                    execution
                            );
        }

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
    public ConversationOrchestratorResponseDto processBargeIn(
            BargeInRequestDto request) {

        validateBargeInRequest(
                request
        );

        log.info(
                "Processing caller barge-in. callId={}",
                request.getCallId()
        );

        CallSessionResponseDto session =
                getRequiredSession(
                        request.getCallId()
                );

        validateActiveSession(
                session
        );

        /*
         * The actual audio interruption is performed by the
         * Voice Gateway / streaming layer.
         *
         * The orchestrator simply changes the next expected
         * interaction back to caller input.
         */
        log.info(
                "Caller barge-in accepted. " +
                        "callId={}, flowExecutionPublicId={}",
                request.getCallId(),
                session.getFlowExecutionPublicId()
        );

        return ConversationOrchestratorResponseDto.builder()
                .callId(
                        request.getCallId()
                )
                .action(
                        ConversationOrchestratorConstants
                                .ACTION_LISTEN
                )
                .waitingForUser(
                        true
                )
                .build();
    }

    // =========================================================
    // CONVERSATION TERMINATION
    // =========================================================

    /**
     * Determines whether the customer requested conversation
     * termination.
     *
     * <p>
     * Only explicit short closing expressions are treated as
     * conversation termination requests.
     * </p>
     *
     * <p>
     * For example:
     * </p>
     *
     * <ul>
     *     <li>ok - ends conversation</li>
     *     <li>okay - ends conversation</li>
     *     <li>thanks - ends conversation</li>
     *     <li>thank you - ends conversation</li>
     *     <li>bye - ends conversation</li>
     *     <li>goodbye - ends conversation</li>
     *     <li>ok tell me about loans - continues conversation</li>
     * </ul>
     *
     * @param transcript final customer transcript
     * @return true when the customer requested the conversation to end
     */
    private boolean isConversationEndRequested(
            String transcript) {

        if (transcript == null
                || transcript.isBlank()) {

            return false;
        }

        String normalized =
                transcript
                        .trim()
                        .toLowerCase(
                                java.util.Locale.ROOT
                        )
                        .replaceAll(
                                "[^\\p{L}\\p{N}\\s]",
                                " "
                        )
                        .replaceAll(
                                "\\s+",
                                " "
                        )
                        .trim();

        return normalized.equals("bye")
                || normalized.equals("goodbye")
                || normalized.equals("good bye")

                || normalized.equals("thanks")
                || normalized.equals("thank you")
                || normalized.equals("thankyou")
                || normalized.equals("thanks a lot")
                || normalized.equals("thank you very much")
                || normalized.equals("thank you so much")

                || normalized.equals("ok")
                || normalized.equals("okay")

                || normalized.equals("ok thanks")
                || normalized.equals("okay thanks")
                || normalized.equals("ok thank you")
                || normalized.equals("okay thank you")

                || normalized.equals("thanks bye")
                || normalized.equals("thank you bye")

                || normalized.equals("that is all")
                || normalized.equals("thats all")

                || normalized.contains("goodbye")
                || normalized.contains("good bye")
                || normalized.contains("thank you goodbye")
                || normalized.contains("thanks goodbye")

                || normalized.equals("धन्यवाद")
                || normalized.equals("ओके")
                || normalized.equals("ठीक है")
                || normalized.equals("बरं")
                || normalized.equals("एवढेच")
                || normalized.equals("झाले")

                || normalized.equals("no")
                || normalized.equals("no thanks")
                || normalized.equals("no thank you")

                || normalized.equals("नाही")
                || normalized.equals("नको")
                || normalized.equals("नको धन्यवाद");
    }

    /**
     * Requests provider-level hangup for a customer-requested
     * conversation closing.
     *
     * <p>
     * The application conversation is ended first. This method
     * then resolves the persisted Call entity and asks the
     * configured telephony provider to terminate the live call.
     * </p>
     *
     * <p>
     * A provider hangup failure is logged but does not re-open
     * the application conversation because the session has already
     * been terminated.
     * </p>
     *
     * @param callId application Call public identifier
     */
    private void requestProviderHangup(
            String callId) {

        try {

            Call call =
                    callRepository
                            .findByPublicId(
                                    callId
                            )
                            .orElse(null);

            if (call == null) {

                log.warn(
                        "Cannot request provider hangup because Call " +
                                "was not found. callId={}",
                        callId
                );

                return;
            }

            String provider =
                    call.getProvider();

            String providerCallId =
                    call.getProviderCallId();

            if (provider == null
                    || provider.isBlank()) {

                log.warn(
                        "Provider hangup skipped because provider is " +
                                "missing. callId={}",
                        callId
                );

                return;
            }

            if (providerCallId == null
                    || providerCallId.isBlank()) {

                log.warn(
                        "Provider hangup skipped because providerCallId " +
                                "is missing. callId={}, provider={}",
                        callId,
                        provider
                );

                return;
            }

            log.info(
                    "Requesting provider hangup. " +
                            "callId={}, provider={}, providerCallId={}",
                    callId,
                    provider,
                    providerCallId
            );

            telephonyService.hangupCall(
                    provider,
                    HangupCallRequestDto.builder()
                            .providerCallId(
                                    providerCallId
                            )
                            .build()
            );

            log.info(
                    "Provider hangup request completed successfully. " +
                            "callId={}, provider={}, providerCallId={}",
                    callId,
                    provider,
                    providerCallId
            );

        } catch (Exception exception) {

            /*
             * ConversationSessionService has already completed the
             * application-level conversation. Therefore a provider
             * hangup exception should not undo that state.
             */
            log.error(
                    "Unable to request provider hangup after customer " +
                            "conversation termination. callId={}",
                    callId,
                    exception
            );
        }
    }

    // =========================================================
    // VALIDATION
    // =========================================================

    /**
     * Validates audio request.
     *
     * @param request audio request
     */
    private void validateAudioRequest(
            ProcessAudioRequestDto request) {

        if (request == null) {

            throw new BadRequestException(
                    ConversationOrchestratorMessages
                            .AUDIO_REQUIRED
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
                request.getAudioBase64()
        )) {

            throw new BadRequestException(
                    ConversationOrchestratorMessages
                            .AUDIO_REQUIRED
            );
        }
    }

    /**
     * Validates transcript request.
     *
     * @param request transcript request
     */
    private void validateTranscriptRequest(
            ProcessTranscriptRequestDto request) {

        if (request == null) {

            throw new BadRequestException(
                    ConversationOrchestratorMessages
                            .TRANSCRIPT_REQUIRED
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
                request.getTranscript()
        )) {

            throw new BadRequestException(
                    ConversationOrchestratorMessages
                            .TRANSCRIPT_REQUIRED
            );
        }
    }

    /**
     * Validates DTMF request.
     *
     * @param request DTMF request
     */
    private void validateDtmfRequest(
            ProcessDtmfRequestDto request) {

        if (request == null) {

            throw new BadRequestException(
                    ConversationOrchestratorMessages
                            .DTMF_DIGIT_REQUIRED
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
                request.getDigit()
        )) {

            throw new BadRequestException(
                    ConversationOrchestratorMessages
                            .DTMF_DIGIT_REQUIRED
            );
        }

        if (request.getDigit().length()
                > ConversationOrchestratorConstants
                .MAX_DTMF_INPUT_LENGTH) {

            throw new BadRequestException(
                    ConversationOrchestratorMessages
                            .INVALID_DTMF
            );
        }
    }

    /**
     * Validates barge-in request.
     *
     * @param request barge-in request
     */
    private void validateBargeInRequest(
            BargeInRequestDto request) {

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
     * Waits briefly for the asynchronously-created Flow Execution.
     *
     * <p>
     * This is intentionally bounded so a genuinely failed Flow startup
     * still returns the existing error instead of blocking indefinitely.
     *
     * @param callId call public identifier
     * @param session current call session
     * @return latest call session containing the Flow Execution when available
     */
    private CallSessionResponseDto waitForFlowExecution(
            String callId,
            CallSessionResponseDto session) {

        if (session.getFlowExecutionPublicId() != null
                && !session.getFlowExecutionPublicId().isBlank()) {

            return session;
        }

        final long timeoutMs = 3000L;
        final long pollIntervalMs = 100L;

        final long deadline =
                System.currentTimeMillis()
                        + timeoutMs;

        CallSessionResponseDto latestSession =
                session;

        while (System.currentTimeMillis() < deadline) {

            try {

                TimeUnit.MILLISECONDS.sleep(
                        pollIntervalMs
                );

            } catch (InterruptedException exception) {

                Thread.currentThread().interrupt();

                log.warn(
                        "Interrupted while waiting for Flow Execution. " +
                                "callId={}",
                        callId
                );

                return latestSession;
            }

            latestSession =
                    getRequiredSession(
                            callId
                    );

            if (latestSession.getFlowExecutionPublicId() != null
                    && !latestSession.getFlowExecutionPublicId().isBlank()) {

                log.info(
                        "Flow Execution became available after startup wait. " +
                                "callId={}, flowExecutionPublicId={}",
                        callId,
                        latestSession.getFlowExecutionPublicId()
                );

                return latestSession;
            }
        }

        return latestSession;
    }

    // =========================================================
    // SESSION
    // =========================================================

    /**
     * Retrieves a required Call Session.
     *
     * @param callId call public identifier
     * @return call session
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

    /**
     * Validates that the Call Session is active.
     *
     * @param session call session
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

    // =========================================================
    // AUDIO
    // =========================================================

    /**
     * Decodes Base64 caller audio.
     *
     * @param request audio request
     * @return decoded audio
     */
    private byte[] decodeAudio(
            ProcessAudioRequestDto request) {

        try {

            return Base64.getDecoder()
                    .decode(
                            request.getAudioBase64()
                    );

        } catch (IllegalArgumentException exception) {

            log.warn(
                    "Invalid Base64 audio received. callId={}",
                    request.getCallId()
            );

            throw new BadRequestException(
                    ConversationOrchestratorMessages
                            .INVALID_AUDIO
            );
        }
    }

    /**
     * Resolves language.
     *
     * @param language requested language
     * @return resolved language
     */
    private String resolveLanguage(
            String language) {

        return isBlank(
                language
        )
                ? ConversationOrchestratorConstants
                .DEFAULT_LANGUAGE
                : language;
    }

    /**
     * Checks whether a value is blank.
     *
     * @param value value to check
     * @return true when blank
     */
    private boolean isBlank(
            String value) {

        return value == null
                || value.isBlank();
    }
}
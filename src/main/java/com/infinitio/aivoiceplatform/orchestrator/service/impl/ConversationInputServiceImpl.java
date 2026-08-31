package com.infinitio.aivoiceplatform.orchestrator.service.impl;

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
import com.infinitio.aivoiceplatform.orchestrator.dto.request.ProcessAudioRequestDto;
import com.infinitio.aivoiceplatform.orchestrator.dto.request.ProcessDtmfRequestDto;
import com.infinitio.aivoiceplatform.orchestrator.dto.request.ProcessTranscriptRequestDto;
import com.infinitio.aivoiceplatform.orchestrator.dto.response.ConversationOrchestratorResponseDto;
import com.infinitio.aivoiceplatform.orchestrator.service.ConversationAiService;
import com.infinitio.aivoiceplatform.orchestrator.service.ConversationInputService;
import com.infinitio.aivoiceplatform.orchestrator.service.ConversationResponseService;
import com.infinitio.aivoiceplatform.stt.dto.runtime.SttTranscriptionRequest;
import com.infinitio.aivoiceplatform.stt.dto.runtime.SttTranscriptionResponse;
import com.infinitio.aivoiceplatform.stt.service.SttRuntimeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

        String executionPublicId =
                session.getFlowExecutionPublicId();

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
                                        request.getContext()
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
    // VALIDATION
    // =========================================================

    /**
     * Validates audio request.
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

    // =========================================================
    // SESSION
    // =========================================================

    /**
     * Retrieves a required Call Session.
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
     */
    private boolean isBlank(
            String value) {

        return value == null
                || value.isBlank();
    }
}
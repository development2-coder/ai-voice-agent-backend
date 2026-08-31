package com.infinitio.aivoiceplatform.orchestrator.service.impl;

import com.infinitio.aivoiceplatform.flow.dto.response.FlowExecutionResult;
import com.infinitio.aivoiceplatform.orchestrator.constant.ConversationOrchestratorConstants;
import com.infinitio.aivoiceplatform.orchestrator.constant.ConversationOrchestratorMessages;
import com.infinitio.aivoiceplatform.orchestrator.dto.response.ConversationOrchestratorResponseDto;
import com.infinitio.aivoiceplatform.orchestrator.service.ConversationResponseService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Default implementation of Conversation Response Service.
 *
 * <p>
 * Converts Flow execution results into responses that can be
 * consumed by the Voice Gateway or Conversation API.
 * </p>
 *
 * <p>
 * This class does not execute Flow nodes, LLM, STT or TTS.
 * It only transforms runtime state into the orchestrator response.
 * </p>
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Slf4j
@Service
public class ConversationResponseServiceImpl
        implements ConversationResponseService {

    /**
     * Flow context key containing generated TTS audio.
     */
    private static final String TTS_AUDIO_BASE64 =
            "ttsAudioBase64";

    /**
     * Flow context key containing generated TTS audio URL.
     */
    private static final String TTS_AUDIO_URL =
            "ttsAudioUrl";

    /**
     * Flow context key containing generated TTS file name.
     */
    private static final String TTS_AUDIO_FILE_NAME =
            "ttsAudioFileName";

    /**
     * Flow context key containing generated TTS content type.
     */
    private static final String TTS_AUDIO_CONTENT_TYPE =
            "ttsAudioContentType";

    /**
     * {@inheritDoc}
     */
    @Override
    public ConversationOrchestratorResponseDto buildResponse(
            String callId,
            String transcript,
            FlowExecutionResult execution) {

        if (execution == null) {

            log.error(
                    "Cannot build conversation response because " +
                            "Flow Execution is null. callId={}",
                    callId
            );

            throw new IllegalStateException(
                    ConversationOrchestratorMessages
                            .FLOW_EXECUTION_RESULT_EMPTY
            );
        }

        log.debug(
                "Building conversation response. " +
                        "callId={}, executionPublicId={}, " +
                        "currentNode={}, status={}",
                callId,
                execution.getExecutionPublicId(),
                execution.getCurrentNodeKey(),
                execution.getStatus()
        );

        ConversationOrchestratorResponseDto response =
                ConversationOrchestratorResponseDto.builder()
                        .callId(
                                callId
                        )
                        .flowExecutionPublicId(
                                execution.getExecutionPublicId()
                        )
                        .currentNodePublicId(
                                execution.getCurrentNodeKey()
                        )
                        .currentNodeType(
                                execution.getCurrentNodeType()
                        )
                        .flowExecutionStatus(
                                execution.getStatus() == null
                                        ? null
                                        : execution.getStatus().name()
                        )
                        .action(
                                resolveAction(
                                        execution
                                )
                        )
                        .transcript(
                                transcript
                        )
                        .responseText(
                                execution.getOutputText()
                        )
                        .waitingForUser(
                                execution.isWaitingForInput()
                        )
                        .waitingForAi(
                                execution.isWaitingForAi()
                        )
                        .waitingForApi(
                                execution.isWaitingForApi()
                        )
                        .waitingForTimer(
                                execution.isWaitingForTimer()
                        )
                        .transferred(
                                execution.isTransferred()
                        )
                        .completed(
                                execution.isCompleted()
                        )
                        .context(
                                execution.getContext()
                        )
                        .build();

        populateAudioResponse(
                response,
                execution.getContext()
        );

        log.debug(
                "Conversation response built successfully. " +
                        "callId={}, action={}, waitingForUser={}, " +
                        "waitingForAi={}, completed={}, transferred={}",
                callId,
                response.getAction(),
                response.isWaitingForUser(),
                response.isWaitingForAi(),
                response.isCompleted(),
                response.isTransferred()
        );

        return response;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ConversationOrchestratorResponseDto buildCompletedResponse(
            String callId) {

        log.debug(
                "Building completed conversation response. callId={}",
                callId
        );

        return ConversationOrchestratorResponseDto.builder()
                .callId(
                        callId
                )
                .action(
                        ConversationOrchestratorConstants
                                .ACTION_END
                )
                .completed(
                        true
                )
                .waitingForUser(
                        false
                )
                .waitingForAi(
                        false
                )
                .waitingForApi(
                        false
                )
                .waitingForTimer(
                        false
                )
                .transferred(
                        false
                )
                .build();
    }

    /**
     * Resolves the runtime action that should be consumed by
     * the Voice Gateway.
     *
     * <p>
     * The order is important because terminal and waiting states
     * take priority over ordinary output.
     * </p>
     *
     * @param execution Flow execution result
     * @return runtime action
     */
    private String resolveAction(
            FlowExecutionResult execution) {

        if (execution.isCompleted()) {

            return ConversationOrchestratorConstants
                    .ACTION_END;
        }

        if (execution.isTransferred()) {

            return ConversationOrchestratorConstants
                    .ACTION_TRANSFER;
        }

        if (execution.isWaitingForApi()) {

            return ConversationOrchestratorConstants
                    .ACTION_WAIT_FOR_API;
        }

        if (execution.isWaitingForAi()) {

            return ConversationOrchestratorConstants
                    .ACTION_WAIT_FOR_AI;
        }

        if (execution.isWaitingForTimer()) {

            return ConversationOrchestratorConstants
                    .ACTION_WAIT_FOR_TIMER;
        }

        if (execution.isWaitingForInput()) {

            return ConversationOrchestratorConstants
                    .ACTION_LISTEN;
        }

        if (execution.getOutputText() != null
                && !execution.getOutputText().isBlank()) {

            return ConversationOrchestratorConstants
                    .ACTION_SPEAK;
        }

        return execution.getAction();
    }

    /**
     * Extracts TTS runtime information from Flow context.
     *
     * <p>
     * TTS is executed by the Flow TTS node. The response service
     * only exposes the resulting audio information.
     * </p>
     *
     * @param response conversation response
     * @param context Flow execution context
     */
    private void populateAudioResponse(
            ConversationOrchestratorResponseDto response,
            Map<String, Object> context) {

        if (response == null
                || context == null
                || context.isEmpty()) {

            return;
        }

        Object audioBase64 =
                context.get(
                        TTS_AUDIO_BASE64
                );

        Object audioUrl =
                context.get(
                        TTS_AUDIO_URL
                );

        Object audioFileName =
                context.get(
                        TTS_AUDIO_FILE_NAME
                );

        Object audioContentType =
                context.get(
                        TTS_AUDIO_CONTENT_TYPE
                );

        if (audioBase64 != null) {

            response.setAudioBase64(
                    String.valueOf(
                            audioBase64
                    )
            );
        }

        if (audioUrl != null) {

            response.setAudioUrl(
                    String.valueOf(
                            audioUrl
                    )
            );
        }

        if (audioFileName != null) {

            response.setAudioFileName(
                    String.valueOf(
                            audioFileName
                    )
            );
        }

        if (audioContentType != null) {

            response.setAudioContentType(
                    String.valueOf(
                            audioContentType
                    )
            );
        }

        if (audioBase64 != null
                || audioUrl != null) {

            log.debug(
                    "TTS audio information added to conversation response. " +
                            "audioBase64Present={}, audioUrlPresent={}, " +
                            "fileNamePresent={}, contentTypePresent={}",
                    audioBase64 != null,
                    audioUrl != null,
                    audioFileName != null,
                    audioContentType != null
            );
        }
    }
}
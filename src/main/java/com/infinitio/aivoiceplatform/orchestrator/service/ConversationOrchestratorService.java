package com.infinitio.aivoiceplatform.orchestrator.service;

import com.infinitio.aivoiceplatform.orchestrator.dto.request.BargeInRequestDto;
import com.infinitio.aivoiceplatform.orchestrator.dto.request.EndConversationRequestDto;
import com.infinitio.aivoiceplatform.orchestrator.dto.request.ProcessAudioRequestDto;
import com.infinitio.aivoiceplatform.orchestrator.dto.request.ProcessDtmfRequestDto;
import com.infinitio.aivoiceplatform.orchestrator.dto.request.ProcessTranscriptRequestDto;
import com.infinitio.aivoiceplatform.orchestrator.dto.request.StartConversationRequestDto;
import com.infinitio.aivoiceplatform.orchestrator.dto.response.ConversationOrchestratorResponseDto;

/**
 * Service responsible for orchestrating the runtime lifecycle
 * of an AI voice conversation.
 *
 * <p>
 * The Conversation Orchestrator coordinates call session state,
 * flow execution, speech-to-text processing, LLM processing,
 * text-to-speech processing and conversation lifecycle events.
 * </p>
 *
 * <p>
 * The Flow module remains responsible for determining the next
 * node and transition according to the agent flow configured by
 * the user.
 * </p>
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
public interface ConversationOrchestratorService {

    /**
     * Starts a new conversation.
     *
     * @param request conversation start request
     * @return conversation orchestration response
     */
    ConversationOrchestratorResponseDto start(
            StartConversationRequestDto request
    );

    /**
     * Processes caller audio.
     *
     * <p>
     * Audio is passed to the configured STT runtime and the
     * resulting final transcript is processed by the active flow.
     * </p>
     *
     * @param request caller audio request
     * @return conversation orchestration response
     */
    ConversationOrchestratorResponseDto processAudio(
            ProcessAudioRequestDto request
    );

    /**
     * Processes a caller transcript.
     *
     * <p>
     * This method is also used when transcript data is supplied
     * directly by a streaming STT runtime.
     * </p>
     *
     * @param request caller transcript request
     * @return conversation orchestration response
     */
    ConversationOrchestratorResponseDto processTranscript(
            ProcessTranscriptRequestDto request
    );

    /**
     * Processes DTMF input received during the conversation.
     *
     * @param request DTMF input request
     * @return conversation orchestration response
     */
    ConversationOrchestratorResponseDto processDtmf(
            ProcessDtmfRequestDto request
    );

    /**
     * Processes a caller barge-in event.
     *
     * <p>
     * The Voice Gateway is responsible for stopping active
     * audio playback. The orchestrator coordinates the resulting
     * conversation state.
     * </p>
     *
     * @param request barge-in request
     * @return conversation orchestration response
     */
    ConversationOrchestratorResponseDto processBargeIn(
            BargeInRequestDto request
    );

    /**
     * Ends the active conversation.
     *
     * @param request conversation termination request
     * @return conversation orchestration response
     */
    ConversationOrchestratorResponseDto end(
            EndConversationRequestDto request
    );
}
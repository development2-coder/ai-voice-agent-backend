package com.infinitio.aivoiceplatform.orchestrator.service;

import com.infinitio.aivoiceplatform.orchestrator.dto.request.BargeInRequestDto;
import com.infinitio.aivoiceplatform.orchestrator.dto.request.ProcessAudioRequestDto;
import com.infinitio.aivoiceplatform.orchestrator.dto.request.ProcessDtmfRequestDto;
import com.infinitio.aivoiceplatform.orchestrator.dto.request.ProcessTranscriptRequestDto;
import com.infinitio.aivoiceplatform.orchestrator.dto.response.ConversationOrchestratorResponseDto;

/**
 * Handles caller input for the Conversation Orchestrator.
 *
 * <p>
 * This service coordinates audio, speech-to-text, transcript,
 * DTMF and barge-in processing.
 * </p>
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
public interface ConversationInputService {

    /**
     * Processes caller audio.
     *
     * @param request audio request
     * @return conversation response
     */
    ConversationOrchestratorResponseDto processAudio(
            ProcessAudioRequestDto request
    );

    /**
     * Processes caller transcript.
     *
     * @param request transcript request
     * @return conversation response
     */
    ConversationOrchestratorResponseDto processTranscript(
            ProcessTranscriptRequestDto request
    );

    /**
     * Processes DTMF input.
     *
     * @param request DTMF request
     * @return conversation response
     */
    ConversationOrchestratorResponseDto processDtmf(
            ProcessDtmfRequestDto request
    );

    /**
     * Processes caller barge-in.
     *
     * @param request barge-in request
     * @return conversation response
     */
    ConversationOrchestratorResponseDto processBargeIn(
            BargeInRequestDto request
    );
}
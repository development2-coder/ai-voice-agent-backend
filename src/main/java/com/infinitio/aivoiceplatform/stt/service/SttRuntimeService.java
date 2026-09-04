package com.infinitio.aivoiceplatform.stt.service;

import com.infinitio.aivoiceplatform.stt.dto.runtime.SttTranscriptionRequest;
import com.infinitio.aivoiceplatform.stt.dto.runtime.SttTranscriptionResponse;
import com.infinitio.aivoiceplatform.stt.provider.SttStreamingListener;

/**
 * Defines runtime speech-to-text operations.
 *
 * <p>
 * This service is responsible for converting incoming call audio
 * into text using the configured speech-to-text provider.
 * </p>
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
public interface SttRuntimeService {

    /**
     * Transcribes audio received during an active call.
     *
     * @param request speech-to-text transcription request
     * @return speech-to-text transcription response
     */
    SttTranscriptionResponse transcribe(
            SttTranscriptionRequest request);

    /**
     * Starts a streaming STT session for an active call.
     *
     * @param callId application call identifier
     * @param language call language
     * @param sampleRate incoming audio sample rate
     * @param audioEncoding incoming audio encoding
     * @param listener asynchronous transcription listener
     */
    void startStreaming(
            String callId,
            String language,
            Integer sampleRate,
            String audioEncoding,
            SttStreamingListener listener
    );

    /**
     * Sends one incoming audio chunk to the active STT session.
     *
     * @param callId application call identifier
     * @param audio audio chunk
     */
    void streamAudio(
            String callId,
            byte[] audio
    );

    /**
     * Ends the current STT turn.
     *
     * @param callId application call identifier
     */
    void finishStreamingTurn(
            String callId
    );

    /**
     * Closes the active STT session.
     *
     * @param callId application call identifier
     */
    void stopStreaming(
            String callId
    );
}
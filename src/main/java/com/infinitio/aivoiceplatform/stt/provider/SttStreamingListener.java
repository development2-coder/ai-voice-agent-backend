package com.infinitio.aivoiceplatform.stt.provider;

import com.infinitio.aivoiceplatform.stt.dto.runtime.SttTranscriptionResponse;

/**
 * Listener for events produced by a streaming Speech-to-Text
 * provider.
 *
 * <p>
 * The listener receives partial and final transcription events
 * as well as optional speech activity events from the streaming
 * STT provider.
 * </p>
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
public interface SttStreamingListener {

    /**
     * Called when a partial transcript is received.
     *
     * @param callId call identifier
     * @param transcript partial transcript
     */
    void onPartialTranscript(
            String callId,
            String transcript);

    /**
     * Called when a final transcript is received.
     *
     * <p>
     * The complete STT response is supplied so that runtime
     * information such as the detected language is preserved
     * and can be propagated to the Flow execution context.
     * </p>
     *
     * @param response final STT response
     */
    void onFinalTranscript(
            SttTranscriptionResponse response);

    /**
     * Called when the provider detects the beginning of speech.
     *
     * <p>
     * This callback is used by the Voice Gateway to implement
     * barge-in. The Voice Gateway may wait for STT confirmation
     * before interrupting active TTS playback.
     * </p>
     *
     * @param callId call identifier
     */
    default void onSpeechStart(
            String callId) {
        // Optional callback.
    }

    /**
     * Called when the provider detects the end of speech.
     *
     * @param callId call identifier
     */
    default void onSpeechEnd(
            String callId) {
        // Optional callback.
    }

    /**
     * Called when the streaming session encounters an error.
     *
     * @param callId call identifier
     * @param exception streaming exception
     */
    default void onError(
            String callId,
            Throwable exception) {
        // Optional callback.
    }
}
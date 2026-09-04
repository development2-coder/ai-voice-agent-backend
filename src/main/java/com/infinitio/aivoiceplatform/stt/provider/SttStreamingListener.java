package com.infinitio.aivoiceplatform.stt.provider;

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
     * @param callId call identifier
     * @param transcript final transcript
     */
    void onFinalTranscript(
            String callId,
            String transcript);

    /**
     * Called when the provider detects the beginning of speech.
     *
     * <p>
     * This callback is used by the Voice Gateway to implement
     * barge-in. When the caller starts speaking while TTS audio
     * is being played, the active TTS stream can be interrupted
     * and a clear-audio instruction can be sent to the telephony
     * provider.
     * </p>
     *
     * <p>
     * The default implementation intentionally does nothing so
     * existing implementations remain backward compatible.
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
     * <p>
     * This callback is optional and can be used by the Voice
     * Gateway or conversation runtime when speech activity
     * information is required.
     * </p>
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
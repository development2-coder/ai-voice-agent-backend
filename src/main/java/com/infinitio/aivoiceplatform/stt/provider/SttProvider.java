package com.infinitio.aivoiceplatform.stt.provider;

import com.infinitio.aivoiceplatform.stt.dto.runtime.SttTranscriptionRequest;
import com.infinitio.aivoiceplatform.stt.dto.runtime.SttTranscriptionResponse;

/**
 * Defines the contract for speech-to-text providers.
 *
 * <p>
 * Provider-specific implementations expose both synchronous
 * transcription and long-lived streaming transcription.
 * </p>
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
public interface SttProvider {

    /**
     * Returns the unique provider code.
     *
     * @return provider code
     */
    String getProviderCode();

    /**
     * Transcribes a complete audio payload.
     *
     * @param request STT transcription request
     * @return STT transcription response
     */
    SttTranscriptionResponse transcribe(
            SttTranscriptionRequest request
    );

    /**
     * Opens a streaming STT connection.
     *
     * @param callId application call identifier
     * @param language requested language
     * @param sampleRate audio sample rate
     * @param audioEncoding audio encoding
     * @param listener streaming result listener
     * @return active streaming session
     */
    default SttStreamingSession openStreamingSession(
            String callId,
            String language,
            Integer sampleRate,
            String audioEncoding,
            SttStreamingListener listener) {

        throw new UnsupportedOperationException(
                "Streaming STT is not supported by provider: "
                        + getProviderCode()
        );
    }

    /**
     * Checks whether the provider is currently available.
     *
     * @return true when the provider is available
     */
    boolean isAvailable();
}
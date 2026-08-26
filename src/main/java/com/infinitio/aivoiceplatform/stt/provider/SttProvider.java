package com.infinitio.aivoiceplatform.stt.provider;

import com.infinitio.aivoiceplatform.stt.dto.runtime.SttTranscriptionRequest;
import com.infinitio.aivoiceplatform.stt.dto.runtime.SttTranscriptionResponse;

/**
 * Defines the contract for speech-to-text providers.
 *
 * <p>
 * Provider-specific implementations must implement this interface so that
 * the STT runtime layer remains independent of a particular STT provider.
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
     * Transcribes the supplied audio.
     *
     * @param request STT transcription request
     * @return STT transcription response
     */
    SttTranscriptionResponse transcribe(
            SttTranscriptionRequest request);

    /**
     * Checks whether the provider is currently available.
     *
     * @return true when the provider is available
     */
    boolean isAvailable();
}
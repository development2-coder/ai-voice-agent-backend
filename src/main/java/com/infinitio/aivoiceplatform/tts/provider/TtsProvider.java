package com.infinitio.aivoiceplatform.tts.provider;

import com.infinitio.aivoiceplatform.tts.dto.runtime.TtsSynthesisRequest;
import com.infinitio.aivoiceplatform.tts.dto.runtime.TtsSynthesisResponse;

/**
 * Defines the contract for text-to-speech providers.
 *
 * <p>
 * Provider-specific implementations must implement this interface so that
 * the TTS runtime layer remains independent of a particular TTS provider.
 * </p>
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
public interface TtsProvider {

    /**
     * Returns the unique provider code.
     *
     * @return provider code
     */
    String getProviderCode();

    /**
     * Synthesizes speech from the supplied text.
     *
     * @param request TTS synthesis request
     * @return TTS synthesis response
     */
    TtsSynthesisResponse synthesize(
            TtsSynthesisRequest request);

    /**
     * Checks whether the provider is currently available.
     *
     * @return true when the provider is available
     */
    boolean isAvailable();
}
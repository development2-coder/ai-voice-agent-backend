package com.infinitio.aivoiceplatform.tts.service;

import com.infinitio.aivoiceplatform.tts.dto.runtime.TtsSynthesisRequest;
import com.infinitio.aivoiceplatform.tts.dto.runtime.TtsSynthesisResponse;

/**
 * Defines runtime text-to-speech business operations.
 *
 * <p>
 * The runtime service is responsible for processing TTS synthesis requests
 * independently of the configured TTS provider.
 * </p>
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
public interface TtsRuntimeService {

    /**
     * Synthesizes speech from the supplied text.
     *
     * @param request TTS synthesis request
     * @return TTS synthesis response
     */
    TtsSynthesisResponse synthesize(
            TtsSynthesisRequest request);
}
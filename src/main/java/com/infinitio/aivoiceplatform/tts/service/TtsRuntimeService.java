package com.infinitio.aivoiceplatform.tts.service;

import com.infinitio.aivoiceplatform.tts.dto.runtime.TtsSynthesisRequest;
import com.infinitio.aivoiceplatform.tts.dto.runtime.TtsSynthesisResponse;
import com.infinitio.aivoiceplatform.tts.streaming.TtsAudioStreamListener;

/**
 * Runtime service for Text-to-Speech execution.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
public interface TtsRuntimeService {

    /**
     * Performs synchronous TTS synthesis.
     *
     * @param request TTS synthesis request
     * @return TTS synthesis response
     */
    TtsSynthesisResponse synthesize(
            TtsSynthesisRequest request);

    /**
     * Performs streaming TTS synthesis.
     *
     * <p>
     * Generated audio chunks are forwarded to the supplied listener
     * while synthesis is in progress.
     * </p>
     *
     * @param request TTS synthesis request
     * @param listener audio chunk listener
     * @return TTS synthesis response
     */
    TtsSynthesisResponse synthesizeStreaming(
            TtsSynthesisRequest request,
            TtsAudioStreamListener listener);
}
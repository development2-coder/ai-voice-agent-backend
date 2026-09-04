package com.infinitio.aivoiceplatform.tts.provider;

import com.infinitio.aivoiceplatform.tts.dto.runtime.TtsSynthesisRequest;
import com.infinitio.aivoiceplatform.tts.dto.runtime.TtsSynthesisResponse;
import com.infinitio.aivoiceplatform.tts.streaming.TtsAudioStreamListener;

/**
 * Provider abstraction for Text-to-Speech services.
 *
 * <p>
 * Provider-specific implementations such as Sarvam remain isolated
 * behind this interface. The runtime layer interacts only with this
 * provider abstraction.
 * </p>
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
public interface TtsProvider {

    /**
     * Returns the provider code.
     *
     * @return provider code
     */
    String getProviderCode();

    /**
     * Checks whether the TTS provider is currently available.
     *
     * <p>
     * Providers may override this method when they have a specific
     * availability or health-check mechanism. The default implementation
     * considers the provider available.
     * </p>
     *
     * @return {@code true} when the provider is available
     */
    default boolean isAvailable() {
        return true;
    }

    /**
     * Performs synchronous speech synthesis.
     *
     * @param request TTS synthesis request
     * @return TTS synthesis response
     */
    TtsSynthesisResponse synthesize(
            TtsSynthesisRequest request);

    /**
     * Performs streaming speech synthesis.
     *
     * <p>
     * Provider implementations can override this method to forward
     * audio chunks as they are generated. The default implementation
     * preserves backward compatibility by executing synchronous
     * synthesis and forwarding the complete audio as a single chunk.
     * </p>
     *
     * @param request TTS synthesis request
     * @param listener listener for generated audio chunks
     * @return TTS synthesis response
     */
    default TtsSynthesisResponse synthesizeStreaming(
            TtsSynthesisRequest request,
            TtsAudioStreamListener listener) {

        TtsSynthesisResponse response =
                synthesize(request);

        if (response != null
                && response.getAudioBytes() != null
                && response.getAudioBytes().length > 0
                && listener != null) {

            listener.onAudioChunk(
                    response.getAudioBytes(),
                    response.getContentType()
            );
        }

        return response;
    }
}
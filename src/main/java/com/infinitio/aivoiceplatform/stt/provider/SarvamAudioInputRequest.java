package com.infinitio.aivoiceplatform.stt.provider;

/**
 * Represents an audio input message sent to the Sarvam
 * realtime speech-to-text WebSocket.
 *
 * <p>
 * This class contains only the provider protocol payload.
 * Runtime configuration such as API keys, endpoints, models,
 * sample rates and timeouts must be provided through the
 * appropriate configuration classes and must not be hardcoded
 * here.
 * </p>
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
public record SarvamAudioInputRequest(

        /**
         * Sarvam realtime event name.
         */
        String event,

        /**
         * Base64 encoded audio data.
         */
        String audio) {
}
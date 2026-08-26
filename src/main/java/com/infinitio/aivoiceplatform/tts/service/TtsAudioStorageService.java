package com.infinitio.aivoiceplatform.tts.service;

import com.infinitio.aivoiceplatform.tts.dto.runtime.TtsAudioStorageResponse;

/**
 * Defines backend storage operations for generated TTS audio.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
public interface TtsAudioStorageService {

    /**
     * Stores generated TTS audio on the application server.
     *
     * @param audioBytes generated audio bytes
     * @param contentType audio content type
     * @param callId call identifier
     * @return stored audio information
     */
    TtsAudioStorageResponse store(
            byte[] audioBytes,
            String contentType,
            String callId
    );
}
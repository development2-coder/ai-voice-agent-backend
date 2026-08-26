package com.infinitio.aivoiceplatform.stt.service;

import com.infinitio.aivoiceplatform.stt.dto.runtime.SttTranscriptionRequest;
import com.infinitio.aivoiceplatform.stt.dto.runtime.SttTranscriptionResponse;

/**
 * Defines runtime speech-to-text operations.
 *
 * <p>
 * This service is responsible for converting incoming call audio
 * into text using the configured speech-to-text provider.
 * </p>
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
public interface SttRuntimeService {

    /**
     * Transcribes audio received during an active call.
     *
     * @param request speech-to-text transcription request
     * @return speech-to-text transcription response
     */
    SttTranscriptionResponse transcribe(
            SttTranscriptionRequest request);
}
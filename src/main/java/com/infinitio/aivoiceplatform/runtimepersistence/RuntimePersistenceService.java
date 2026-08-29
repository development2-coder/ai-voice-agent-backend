package com.infinitio.aivoiceplatform.runtimepersistence;

import com.infinitio.aivoiceplatform.llm.dto.runtime.LlmGenerationRequestDto;
import com.infinitio.aivoiceplatform.llm.dto.runtime.LlmGenerationResponseDto;
import com.infinitio.aivoiceplatform.stt.dto.runtime.SttTranscriptionRequest;
import com.infinitio.aivoiceplatform.stt.dto.runtime.SttTranscriptionResponse;
import com.infinitio.aivoiceplatform.tts.dto.runtime.TtsSynthesisRequest;
import com.infinitio.aivoiceplatform.tts.dto.runtime.TtsSynthesisResponse;

/**
 * Persists runtime STT, LLM and TTS interactions.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
public interface RuntimePersistenceService {

    /**
     * Persists an STT runtime response.
     *
     * @param request STT request
     * @param response STT response
     */
    void saveStt(
            SttTranscriptionRequest request,
            SttTranscriptionResponse response
    );

    /**
     * Persists an LLM runtime response.
     *
     * @param request LLM request
     * @param response LLM response
     */
    void saveLlm(
            LlmGenerationRequestDto request,
            LlmGenerationResponseDto response
    );

    /**
     * Persists a TTS runtime response.
     *
     * @param request TTS request
     * @param response TTS response
     */
    void saveTts(
            TtsSynthesisRequest request,
            TtsSynthesisResponse response
    );
}
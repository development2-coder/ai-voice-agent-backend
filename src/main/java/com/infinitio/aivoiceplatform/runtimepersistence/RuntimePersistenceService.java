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

    /**
     * Persists a conversation transcript message into the
     * complete call transcript artifact.
     *
     * <p>
     * This method is used by realtime streaming flows where
     * the complete STT request object is not available but a
     * final transcript message has already been produced.
     * </p>
     *
     * @param callPublicId call public identifier
     * @param speakerType speaker type
     * @param text transcript text
     * @param language detected language
     * @param source transcript source
     */
    void saveTranscriptMessage(
            String callPublicId,
            String speakerType,
            String text,
            String language,
            String source
    );
}
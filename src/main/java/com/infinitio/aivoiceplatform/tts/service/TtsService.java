package com.infinitio.aivoiceplatform.tts.service;

import com.infinitio.aivoiceplatform.common.dto.PageResponse;
import com.infinitio.aivoiceplatform.tts.dto.request.CreateTtsRequest;
import com.infinitio.aivoiceplatform.tts.dto.request.UpdateTtsRequest;
import com.infinitio.aivoiceplatform.tts.dto.response.TtsResponse;

/**
 * Service interface for TTS.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
public interface TtsService {

    TtsResponse create(CreateTtsRequest request);

    TtsResponse update(UpdateTtsRequest request);

    TtsResponse getByPublicId(String publicId);

    PageResponse<TtsResponse> getAll(int page, int size);

    void delete(String publicId);

    void activate(String publicId);

    void deactivate(String publicId);
}
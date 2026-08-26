package com.infinitio.aivoiceplatform.voice.service;

import com.infinitio.aivoiceplatform.common.dto.PageResponse;
import com.infinitio.aivoiceplatform.voice.dto.request.CreateVoiceRequest;
import com.infinitio.aivoiceplatform.voice.dto.request.UpdateVoiceRequest;
import com.infinitio.aivoiceplatform.voice.dto.response.VoiceResponse;

/**
 * Service interface for Voice.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
public interface VoiceService {

    VoiceResponse create(CreateVoiceRequest request);

    VoiceResponse update(UpdateVoiceRequest request);

    VoiceResponse getByPublicId(String publicId);

    PageResponse<VoiceResponse> getAll(int page, int size);

    void delete(String publicId);

    void activate(String publicId);

    void deactivate(String publicId);
}
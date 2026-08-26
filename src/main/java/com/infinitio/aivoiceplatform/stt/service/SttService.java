package com.infinitio.aivoiceplatform.stt.service;

import com.infinitio.aivoiceplatform.common.dto.PageResponse;
import com.infinitio.aivoiceplatform.stt.dto.request.CreateSttRequest;
import com.infinitio.aivoiceplatform.stt.dto.request.UpdateSttRequest;
import com.infinitio.aivoiceplatform.stt.dto.response.SttResponse;

/**
 * Service interface for STT.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
public interface SttService {

    SttResponse create(CreateSttRequest request);

    SttResponse update(UpdateSttRequest request);

    SttResponse getByPublicId(String publicId);

    PageResponse<SttResponse> getAll(int page, int size);

    void delete(String publicId);

    void activate(String publicId);

    void deactivate(String publicId);
}
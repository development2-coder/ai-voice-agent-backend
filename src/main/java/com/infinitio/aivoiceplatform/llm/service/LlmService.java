package com.infinitio.aivoiceplatform.llm.service;

import com.infinitio.aivoiceplatform.common.dto.PageResponse;
import com.infinitio.aivoiceplatform.llm.dto.request.CreateLlmRequest;
import com.infinitio.aivoiceplatform.llm.dto.request.UpdateLlmRequest;
import com.infinitio.aivoiceplatform.llm.dto.response.LlmResponse;

/**
 * Service interface for LLM.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
public interface LlmService {

    LlmResponse create(CreateLlmRequest request);

    LlmResponse update(UpdateLlmRequest request);

    LlmResponse getByPublicId(String publicId);

    PageResponse<LlmResponse> getAll(int page, int size);

    void delete(String publicId);

    void activate(String publicId);

    void deactivate(String publicId);
}
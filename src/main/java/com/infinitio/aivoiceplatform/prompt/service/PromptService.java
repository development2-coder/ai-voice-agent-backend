package com.infinitio.aivoiceplatform.prompt.service;

import com.infinitio.aivoiceplatform.common.dto.PageResponse;
import com.infinitio.aivoiceplatform.prompt.dto.request.CreatePromptRequest;
import com.infinitio.aivoiceplatform.prompt.dto.request.UpdatePromptRequest;
import com.infinitio.aivoiceplatform.prompt.dto.response.PromptResponse;

/**
 * Service interface for Prompt.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
public interface PromptService {

    PromptResponse create(CreatePromptRequest request);

    PromptResponse update(UpdatePromptRequest request);

    PromptResponse getByPublicId(String publicId);

    PageResponse<PromptResponse> getAll(int page, int size);

    void delete(String publicId);

    void activate(String publicId);

    void deactivate(String publicId);
}
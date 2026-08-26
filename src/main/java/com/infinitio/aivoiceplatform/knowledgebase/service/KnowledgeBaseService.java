package com.infinitio.aivoiceplatform.knowledgebase.service;

import com.infinitio.aivoiceplatform.common.dto.PageResponse;
import com.infinitio.aivoiceplatform.knowledgebase.dto.request.CreateKnowledgeBaseRequest;
import com.infinitio.aivoiceplatform.knowledgebase.dto.request.UpdateKnowledgeBaseRequest;
import com.infinitio.aivoiceplatform.knowledgebase.dto.response.KnowledgeBaseResponse;

/**
 * Service interface for Knowledge Base.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
public interface KnowledgeBaseService {

    KnowledgeBaseResponse create(
            CreateKnowledgeBaseRequest request
    );

    KnowledgeBaseResponse update(
            UpdateKnowledgeBaseRequest request
    );

    KnowledgeBaseResponse getByPublicId(
            String publicId
    );

    PageResponse<KnowledgeBaseResponse> getAll(
            int page,
            int size
    );

    void delete(String publicId);

    void activate(String publicId);

    void deactivate(String publicId);
}
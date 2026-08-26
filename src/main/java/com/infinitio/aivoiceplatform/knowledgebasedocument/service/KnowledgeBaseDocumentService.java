package com.infinitio.aivoiceplatform.knowledgebasedocument.service;

import com.infinitio.aivoiceplatform.common.dto.PageResponse;
import com.infinitio.aivoiceplatform.knowledgebasedocument.dto.request.CreateKnowledgeBaseDocumentRequest;
import com.infinitio.aivoiceplatform.knowledgebasedocument.dto.request.UpdateKnowledgeBaseDocumentRequest;
import com.infinitio.aivoiceplatform.knowledgebasedocument.dto.response.KnowledgeBaseDocumentResponse;

/**
 * Service interface for Knowledge Base Document.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
public interface KnowledgeBaseDocumentService {

    KnowledgeBaseDocumentResponse create(
            CreateKnowledgeBaseDocumentRequest request
    );

    KnowledgeBaseDocumentResponse update(
            UpdateKnowledgeBaseDocumentRequest request
    );

    KnowledgeBaseDocumentResponse getByPublicId(
            String publicId
    );

    PageResponse<KnowledgeBaseDocumentResponse> getAll(
            int page,
            int size
    );

    void delete(String publicId);

    void activate(String publicId);

    void deactivate(String publicId);
}
package com.infinitio.aivoiceplatform.knowledgebasedocument.validator;

import com.infinitio.aivoiceplatform.exception.ConflictException;
import com.infinitio.aivoiceplatform.exception.ResourceNotFoundException;
import com.infinitio.aivoiceplatform.knowledgebasedocument.constant.KnowledgeBaseDocumentMessages;
import com.infinitio.aivoiceplatform.knowledgebasedocument.dto.request.CreateKnowledgeBaseDocumentRequest;
import com.infinitio.aivoiceplatform.knowledgebasedocument.dto.request.UpdateKnowledgeBaseDocumentRequest;
import com.infinitio.aivoiceplatform.knowledgebasedocument.entity.KnowledgeBaseDocument;
import com.infinitio.aivoiceplatform.knowledgebasedocument.repository.KnowledgeBaseDocumentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Validator for Knowledge Base Document.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Component
@RequiredArgsConstructor
public class KnowledgeBaseDocumentValidator {

    private final KnowledgeBaseDocumentRepository
            knowledgeBaseDocumentRepository;

    public void validateForCreate(
            CreateKnowledgeBaseDocumentRequest request) {

        if (knowledgeBaseDocumentRepository
                .existsByDocumentCode(
                        request.getDocumentCode())) {

            throw new ConflictException(
                    KnowledgeBaseDocumentMessages.CODE_ALREADY_EXISTS
            );
        }

        if (knowledgeBaseDocumentRepository
                .existsByDocumentName(
                        request.getDocumentName())) {

            throw new ConflictException(
                    KnowledgeBaseDocumentMessages.NAME_ALREADY_EXISTS
            );
        }
    }

    public void validateForUpdate(
            UpdateKnowledgeBaseDocumentRequest request) {

        KnowledgeBaseDocument existing =
                validateAndGet(request.getPublicId());

        if (!existing.getDocumentCode()
                .equals(request.getDocumentCode())
                && knowledgeBaseDocumentRepository
                .existsByDocumentCode(
                        request.getDocumentCode())) {

            throw new ConflictException(
                    KnowledgeBaseDocumentMessages.CODE_ALREADY_EXISTS
            );
        }

        if (!existing.getDocumentName()
                .equals(request.getDocumentName())
                && knowledgeBaseDocumentRepository
                .existsByDocumentName(
                        request.getDocumentName())) {

            throw new ConflictException(
                    KnowledgeBaseDocumentMessages.NAME_ALREADY_EXISTS
            );
        }
    }

    public KnowledgeBaseDocument validateAndGet(
            String publicId) {

        return knowledgeBaseDocumentRepository
                .findByPublicId(publicId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                KnowledgeBaseDocumentMessages.NOT_FOUND
                        )
                );
    }
}
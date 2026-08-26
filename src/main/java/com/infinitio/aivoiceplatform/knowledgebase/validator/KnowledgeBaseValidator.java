package com.infinitio.aivoiceplatform.knowledgebase.validator;

import com.infinitio.aivoiceplatform.exception.ConflictException;
import com.infinitio.aivoiceplatform.exception.ResourceNotFoundException;
import com.infinitio.aivoiceplatform.knowledgebase.constant.KnowledgeBaseMessages;
import com.infinitio.aivoiceplatform.knowledgebase.dto.request.CreateKnowledgeBaseRequest;
import com.infinitio.aivoiceplatform.knowledgebase.dto.request.UpdateKnowledgeBaseRequest;
import com.infinitio.aivoiceplatform.knowledgebase.entity.KnowledgeBase;
import com.infinitio.aivoiceplatform.knowledgebase.repository.KnowledgeBaseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Validator for Knowledge Base.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Component
@RequiredArgsConstructor
public class KnowledgeBaseValidator {

    private final KnowledgeBaseRepository knowledgeBaseRepository;

    public void validateForCreate(
            CreateKnowledgeBaseRequest request) {

        if (knowledgeBaseRepository.existsByKnowledgeBaseCode(
                request.getKnowledgeBaseCode())) {

            throw new ConflictException(
                    KnowledgeBaseMessages.CODE_ALREADY_EXISTS
            );
        }

        if (knowledgeBaseRepository.existsByKnowledgeBaseName(
                request.getKnowledgeBaseName())) {

            throw new ConflictException(
                    KnowledgeBaseMessages.NAME_ALREADY_EXISTS
            );
        }
    }

    public void validateForUpdate(
            UpdateKnowledgeBaseRequest request) {

        KnowledgeBase existing =
                validateAndGet(request.getPublicId());

        if (!existing.getKnowledgeBaseCode()
                .equals(request.getKnowledgeBaseCode())
                && knowledgeBaseRepository
                .existsByKnowledgeBaseCode(
                        request.getKnowledgeBaseCode())) {

            throw new ConflictException(
                    KnowledgeBaseMessages.CODE_ALREADY_EXISTS
            );
        }

        if (!existing.getKnowledgeBaseName()
                .equals(request.getKnowledgeBaseName())
                && knowledgeBaseRepository
                .existsByKnowledgeBaseName(
                        request.getKnowledgeBaseName())) {

            throw new ConflictException(
                    KnowledgeBaseMessages.NAME_ALREADY_EXISTS
            );
        }
    }

    public KnowledgeBase validateAndGet(
            String publicId) {

        return knowledgeBaseRepository
                .findByPublicId(publicId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                KnowledgeBaseMessages.NOT_FOUND
                        )
                );
    }
}
package com.infinitio.aivoiceplatform.llm.validator;

import com.infinitio.aivoiceplatform.exception.ConflictException;
import com.infinitio.aivoiceplatform.exception.ResourceNotFoundException;
import com.infinitio.aivoiceplatform.llm.constant.LlmMessages;
import com.infinitio.aivoiceplatform.llm.dto.request.CreateLlmRequest;
import com.infinitio.aivoiceplatform.llm.dto.request.UpdateLlmRequest;
import com.infinitio.aivoiceplatform.llm.entity.Llm;
import com.infinitio.aivoiceplatform.llm.repository.LlmRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Validator for LLM.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Component
@RequiredArgsConstructor
public class LlmValidator {

    private final LlmRepository llmRepository;

    public void validateForCreate(
            CreateLlmRequest request) {

        if (llmRepository.existsByLlmCode(
                request.getLlmCode())) {

            throw new ConflictException(
                    LlmMessages.CODE_ALREADY_EXISTS
            );
        }

        if (llmRepository.existsByLlmName(
                request.getLlmName())) {

            throw new ConflictException(
                    LlmMessages.NAME_ALREADY_EXISTS
            );
        }
    }

    public void validateForUpdate(
            UpdateLlmRequest request) {

        Llm existing =
                validateAndGet(request.getPublicId());

        if (!existing.getLlmCode()
                .equals(request.getLlmCode())
                && llmRepository.existsByLlmCode(
                request.getLlmCode())) {

            throw new ConflictException(
                    LlmMessages.CODE_ALREADY_EXISTS
            );
        }

        if (!existing.getLlmName()
                .equals(request.getLlmName())
                && llmRepository.existsByLlmName(
                request.getLlmName())) {

            throw new ConflictException(
                    LlmMessages.NAME_ALREADY_EXISTS
            );
        }
    }

    public Llm validateAndGet(String publicId) {

        return llmRepository
                .findByPublicId(publicId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                LlmMessages.NOT_FOUND
                        )
                );
    }
}
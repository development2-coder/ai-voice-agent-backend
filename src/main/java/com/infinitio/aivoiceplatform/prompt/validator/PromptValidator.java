package com.infinitio.aivoiceplatform.prompt.validator;

import com.infinitio.aivoiceplatform.exception.ConflictException;
import com.infinitio.aivoiceplatform.exception.ResourceNotFoundException;
import com.infinitio.aivoiceplatform.prompt.constant.PromptMessages;
import com.infinitio.aivoiceplatform.prompt.dto.request.CreatePromptRequest;
import com.infinitio.aivoiceplatform.prompt.dto.request.UpdatePromptRequest;
import com.infinitio.aivoiceplatform.prompt.entity.Prompt;
import com.infinitio.aivoiceplatform.prompt.repository.PromptRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Validator for Prompt.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Component
@RequiredArgsConstructor
public class PromptValidator {

    private final PromptRepository promptRepository;

    public void validateForCreate(CreatePromptRequest request) {

        if (promptRepository.existsByPromptCode(
                request.getPromptCode())) {

            throw new ConflictException(
                    PromptMessages.CODE_ALREADY_EXISTS
            );
        }

        if (promptRepository.existsByPromptName(
                request.getPromptName())) {

            throw new ConflictException(
                    PromptMessages.NAME_ALREADY_EXISTS
            );
        }
    }

    public void validateForUpdate(UpdatePromptRequest request) {

        Prompt existing =
                validateAndGet(request.getPublicId());

        if (!existing.getPromptCode()
                .equals(request.getPromptCode())
                && promptRepository.existsByPromptCode(
                request.getPromptCode())) {

            throw new ConflictException(
                    PromptMessages.CODE_ALREADY_EXISTS
            );
        }

        if (!existing.getPromptName()
                .equals(request.getPromptName())
                && promptRepository.existsByPromptName(
                request.getPromptName())) {

            throw new ConflictException(
                    PromptMessages.NAME_ALREADY_EXISTS
            );
        }
    }

    public Prompt validateAndGet(String publicId) {

        return promptRepository
                .findByPublicId(publicId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                PromptMessages.PROMPT_NOT_FOUND
                        ));
    }
}
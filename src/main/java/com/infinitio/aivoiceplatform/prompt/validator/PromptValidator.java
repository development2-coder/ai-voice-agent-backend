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

import static com.infinitio.aivoiceplatform.common.constants.ApplicationConstants.NOT_DELETED;

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


    /**
     * Validates Prompt creation request.
     *
     * @param request create request
     */
    public void validateForCreate(
            CreatePromptRequest request) {

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


    /**
     * Validates Prompt update request.
     *
     * @param request update request
     */
    public void validateForUpdate(
            UpdatePromptRequest request) {

        Prompt existing =
                validateAndGet(
                        request.getPublicId()
                );

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


    /**
     * Finds a non-deleted Prompt by public ID.
     *
     * @param publicId prompt public ID
     * @return Prompt
     */
    public Prompt validateAndGet(
            String publicId) {

        return promptRepository
                .findByPublicIdAndIsDeleted(
                        publicId,
                        NOT_DELETED
                )
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                PromptMessages.PROMPT_NOT_FOUND
                        )
                );
    }
}
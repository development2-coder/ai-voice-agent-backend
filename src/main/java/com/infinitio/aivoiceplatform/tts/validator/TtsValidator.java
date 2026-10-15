package com.infinitio.aivoiceplatform.tts.validator;

import com.infinitio.aivoiceplatform.exception.ConflictException;
import com.infinitio.aivoiceplatform.exception.ResourceNotFoundException;
import com.infinitio.aivoiceplatform.tts.constant.TtsMessages;
import com.infinitio.aivoiceplatform.tts.dto.request.CreateTtsRequest;
import com.infinitio.aivoiceplatform.tts.dto.request.UpdateTtsRequest;
import com.infinitio.aivoiceplatform.tts.entity.Tts;
import com.infinitio.aivoiceplatform.tts.repository.TtsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import static com.infinitio.aivoiceplatform.common.constants.ApplicationConstants.NOT_DELETED;

/**
 * Validator for TTS.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Component
@RequiredArgsConstructor
public class TtsValidator {

    private final TtsRepository ttsRepository;

    /**
     * Validates TTS creation request.
     *
     * @param request create TTS request
     */
    public void validateForCreate(
            CreateTtsRequest request) {

        if (ttsRepository.existsByTtsCode(
                request.getTtsCode()
        )) {

            throw new ConflictException(
                    TtsMessages.CODE_ALREADY_EXISTS
            );
        }

        if (ttsRepository.existsByTtsName(
                request.getTtsName()
        )) {

            throw new ConflictException(
                    TtsMessages.NAME_ALREADY_EXISTS
            );
        }
    }

    /**
     * Validates TTS update request.
     *
     * @param request update TTS request
     */
    public void validateForUpdate(
            UpdateTtsRequest request) {

        Tts existing =
                validateAndGet(
                        request.getPublicId()
                );

        if (!existing.getTtsCode()
                .equals(request.getTtsCode())
                && ttsRepository.existsByTtsCode(
                request.getTtsCode()
        )) {

            throw new ConflictException(
                    TtsMessages.CODE_ALREADY_EXISTS
            );
        }

        if (!existing.getTtsName()
                .equals(request.getTtsName())
                && ttsRepository.existsByTtsName(
                request.getTtsName()
        )) {

            throw new ConflictException(
                    TtsMessages.NAME_ALREADY_EXISTS
            );
        }
    }

    /**
     * Retrieves a non-deleted TTS configuration
     * by public identifier.
     *
     * @param publicId TTS public identifier
     * @return TTS entity
     */
    public Tts validateAndGet(
            String publicId) {

        return ttsRepository
                .findByPublicIdAndIsDeleted(
                        publicId,
                        NOT_DELETED
                )
                .orElseThrow(
                        () -> new ResourceNotFoundException(
                                TtsMessages.NOT_FOUND
                        )
                );
    }
}
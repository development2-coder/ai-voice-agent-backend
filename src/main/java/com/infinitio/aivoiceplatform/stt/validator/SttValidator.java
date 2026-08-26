package com.infinitio.aivoiceplatform.stt.validator;

import com.infinitio.aivoiceplatform.exception.ConflictException;
import com.infinitio.aivoiceplatform.exception.ResourceNotFoundException;
import com.infinitio.aivoiceplatform.stt.constant.SttMessages;
import com.infinitio.aivoiceplatform.stt.dto.request.CreateSttRequest;
import com.infinitio.aivoiceplatform.stt.dto.request.UpdateSttRequest;
import com.infinitio.aivoiceplatform.stt.entity.Stt;
import com.infinitio.aivoiceplatform.stt.repository.SttRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Validator for STT.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Component
@RequiredArgsConstructor
public class SttValidator {

    private final SttRepository sttRepository;

    public void validateForCreate(
            CreateSttRequest request) {

        if (sttRepository.existsBySttCode(
                request.getSttCode())) {

            throw new ConflictException(
                    SttMessages.CODE_ALREADY_EXISTS
            );
        }

        if (sttRepository.existsBySttName(
                request.getSttName())) {

            throw new ConflictException(
                    SttMessages.NAME_ALREADY_EXISTS
            );
        }
    }

    public void validateForUpdate(
            UpdateSttRequest request) {

        Stt existing =
                validateAndGet(request.getPublicId());

        if (!existing.getSttCode()
                .equals(request.getSttCode())
                && sttRepository.existsBySttCode(
                request.getSttCode())) {

            throw new ConflictException(
                    SttMessages.CODE_ALREADY_EXISTS
            );
        }

        if (!existing.getSttName()
                .equals(request.getSttName())
                && sttRepository.existsBySttName(
                request.getSttName())) {

            throw new ConflictException(
                    SttMessages.NAME_ALREADY_EXISTS
            );
        }
    }

    public Stt validateAndGet(String publicId) {

        return sttRepository
                .findByPublicId(publicId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                SttMessages.NOT_FOUND
                        )
                );
    }
}
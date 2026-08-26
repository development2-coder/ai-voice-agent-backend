package com.infinitio.aivoiceplatform.voice.validator;

import com.infinitio.aivoiceplatform.exception.ConflictException;
import com.infinitio.aivoiceplatform.exception.ResourceNotFoundException;
import com.infinitio.aivoiceplatform.voice.constant.VoiceMessages;
import com.infinitio.aivoiceplatform.voice.dto.request.CreateVoiceRequest;
import com.infinitio.aivoiceplatform.voice.dto.request.UpdateVoiceRequest;
import com.infinitio.aivoiceplatform.voice.entity.Voice;
import com.infinitio.aivoiceplatform.voice.repository.VoiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Validator for Voice.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Component
@RequiredArgsConstructor
public class VoiceValidator {

    private final VoiceRepository voiceRepository;

    public void validateForCreate(
            CreateVoiceRequest request) {

        if (voiceRepository.existsByVoiceCode(
                request.getVoiceCode())) {

            throw new ConflictException(
                    VoiceMessages.CODE_ALREADY_EXISTS
            );
        }

        if (voiceRepository.existsByVoiceName(
                request.getVoiceName())) {

            throw new ConflictException(
                    VoiceMessages.NAME_ALREADY_EXISTS
            );
        }
    }

    public void validateForUpdate(
            UpdateVoiceRequest request) {

        Voice existing =
                validateAndGet(request.getPublicId());

        if (!existing.getVoiceCode()
                .equals(request.getVoiceCode())
                && voiceRepository.existsByVoiceCode(
                request.getVoiceCode())) {

            throw new ConflictException(
                    VoiceMessages.CODE_ALREADY_EXISTS
            );
        }

        if (!existing.getVoiceName()
                .equals(request.getVoiceName())
                && voiceRepository.existsByVoiceName(
                request.getVoiceName())) {

            throw new ConflictException(
                    VoiceMessages.NAME_ALREADY_EXISTS
            );
        }
    }

    public Voice validateAndGet(String publicId) {

        return voiceRepository
                .findByPublicId(publicId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                VoiceMessages.NOT_FOUND
                        )
                );
    }
}
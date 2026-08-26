package com.infinitio.aivoiceplatform.callrecording.validator;

import com.infinitio.aivoiceplatform.callrecording.constant.CallRecordingMessages;
import com.infinitio.aivoiceplatform.callrecording.dto.request.CreateCallRecordingRequest;
import com.infinitio.aivoiceplatform.callrecording.dto.request.UpdateCallRecordingRequest;
import com.infinitio.aivoiceplatform.callrecording.entity.CallRecording;
import com.infinitio.aivoiceplatform.callrecording.repository.CallRecordingRepository;
import com.infinitio.aivoiceplatform.exception.ConflictException;
import com.infinitio.aivoiceplatform.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Validator for Call Recording.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Component
@RequiredArgsConstructor
public class CallRecordingValidator {

    private final CallRecordingRepository
            callRecordingRepository;

    public void validateForCreate(
            CreateCallRecordingRequest request) {

        if (callRecordingRepository.existsByFileUrl(
                request.getFileUrl())) {

            throw new ConflictException(
                    CallRecordingMessages.URL_ALREADY_EXISTS
            );
        }
    }

    public void validateForUpdate(
            UpdateCallRecordingRequest request) {

        CallRecording existing =
                validateAndGet(request.getPublicId());

        if (!existing.getFileUrl()
                .equals(request.getFileUrl())
                && callRecordingRepository
                .existsByFileUrl(
                        request.getFileUrl()
                )) {

            throw new ConflictException(
                    CallRecordingMessages.URL_ALREADY_EXISTS
            );
        }
    }

    public CallRecording validateAndGet(
            String publicId) {

        return callRecordingRepository
                .findByPublicId(publicId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                CallRecordingMessages.NOT_FOUND
                        )
                );
    }
}
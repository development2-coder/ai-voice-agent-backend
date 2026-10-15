package com.infinitio.aivoiceplatform.callrecording.validator;

import com.infinitio.aivoiceplatform.callrecording.constant.CallRecordingMessages;
import com.infinitio.aivoiceplatform.callrecording.dto.request.CreateCallRecordingRequest;
import com.infinitio.aivoiceplatform.callrecording.dto.request.UpdateCallRecordingRequest;
import com.infinitio.aivoiceplatform.callrecording.entity.CallRecording;
import com.infinitio.aivoiceplatform.callrecording.repository.CallRecordingRepository;
import com.infinitio.aivoiceplatform.exception.BadRequestException;
import com.infinitio.aivoiceplatform.exception.ConflictException;
import com.infinitio.aivoiceplatform.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Validates call recording requests and entity lookups.
 *
 * Tenant access is checked by CallRecordingServiceImpl after resolving
 * the recording and its associated Call.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Component
@RequiredArgsConstructor
public class CallRecordingValidator {

    private static final Integer NOT_DELETED = 0;

    private final CallRecordingRepository callRecordingRepository;

    /**
     * Validates recording creation input and duplicate URL.
     *
     * @param request creation request
     */
    public void validateForCreate(
            CreateCallRecordingRequest request) {

        if (request == null) {
            throw new BadRequestException(
                    "Call recording request is required."
            );
        }

        if (request.getCallPublicId() == null
                || request.getCallPublicId().isBlank()) {
            throw new BadRequestException(
                    "Call is required."
            );
        }

        if (request.getFileName() == null
                || request.getFileName().isBlank()) {
            throw new BadRequestException(
                    "Recording file name is required."
            );
        }

        if (request.getFileUrl() == null
                || request.getFileUrl().isBlank()) {
            throw new BadRequestException(
                    "Recording file URL is required."
            );
        }

        if (request.getFileType() == null
                || request.getFileType().isBlank()) {
            throw new BadRequestException(
                    "Recording file type is required."
            );
        }

        if (request.getDurationSeconds() != null
                && request.getDurationSeconds() < 0) {
            throw new BadRequestException(
                    "Recording duration cannot be negative."
            );
        }

        if (callRecordingRepository.existsByFileUrl(
                request.getFileUrl().trim()
        )) {
            throw new ConflictException(
                    CallRecordingMessages.URL_ALREADY_EXISTS
            );
        }

        request.setFileName(request.getFileName().trim());
        request.setFileUrl(request.getFileUrl().trim());
        request.setFileType(request.getFileType().trim());

        if (request.getStorageProvider() != null) {
            request.setStorageProvider(
                    request.getStorageProvider().trim()
            );
        }
    }

    /**
     * Validates update input and duplicate URL.
     *
     * @param request update request
     */
    public void validateForUpdate(
            UpdateCallRecordingRequest request) {

        if (request == null) {
            throw new BadRequestException(
                    "Call recording update request is required."
            );
        }

        if (request.getPublicId() == null
                || request.getPublicId().isBlank()) {
            throw new BadRequestException(
                    "Recording public ID is required."
            );
        }

        if (request.getCallPublicId() == null
                || request.getCallPublicId().isBlank()) {
            throw new BadRequestException(
                    "Call is required."
            );
        }

        if (request.getFileName() == null
                || request.getFileName().isBlank()) {
            throw new BadRequestException(
                    "Recording file name is required."
            );
        }

        if (request.getFileUrl() == null
                || request.getFileUrl().isBlank()) {
            throw new BadRequestException(
                    "Recording file URL is required."
            );
        }

        if (request.getFileType() == null
                || request.getFileType().isBlank()) {
            throw new BadRequestException(
                    "Recording file type is required."
            );
        }

        if (request.getDurationSeconds() != null
                && request.getDurationSeconds() < 0) {
            throw new BadRequestException(
                    "Recording duration cannot be negative."
            );
        }

        CallRecording existing =
                validateAndGet(request.getPublicId());

        String requestedUrl = request.getFileUrl().trim();

        if (!requestedUrl.equals(existing.getFileUrl())
                && callRecordingRepository.existsByFileUrl(
                requestedUrl
        )) {
            throw new ConflictException(
                    CallRecordingMessages.URL_ALREADY_EXISTS
            );
        }

        request.setFileName(request.getFileName().trim());
        request.setFileUrl(requestedUrl);
        request.setFileType(request.getFileType().trim());

        if (request.getStorageProvider() != null) {
            request.setStorageProvider(
                    request.getStorageProvider().trim()
            );
        }
    }

    /**
     * Resolves a non-deleted recording by public ID.
     *
     * @param publicId recording public ID
     * @return non-deleted recording
     */
    public CallRecording validateAndGet(String publicId) {

        if (publicId == null || publicId.isBlank()) {
            throw new BadRequestException(
                    "Recording public ID is required."
            );
        }

        return callRecordingRepository
                .findByPublicIdAndIsDeleted(
                        publicId,
                        NOT_DELETED
                )
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                CallRecordingMessages.NOT_FOUND
                        )
                );
    }
}
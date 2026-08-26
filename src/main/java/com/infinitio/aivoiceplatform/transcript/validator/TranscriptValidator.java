package com.infinitio.aivoiceplatform.transcript.validator;

import org.springframework.stereotype.Component;

import com.infinitio.aivoiceplatform.transcript.constant.TranscriptConstants;
import com.infinitio.aivoiceplatform.transcript.constant.TranscriptMessages;
import com.infinitio.aivoiceplatform.transcript.dto.request.CreateTranscriptRequest;
import com.infinitio.aivoiceplatform.transcript.dto.request.UpdateTranscriptRequest;
import com.infinitio.aivoiceplatform.exception.BadRequestException;

import lombok.extern.slf4j.Slf4j;

/**
 * Validator for Transcript requests.
 *
 * <p>
 * Validates transcript business rules that are not completely
 * covered by Jakarta Bean Validation.
 * </p>
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Slf4j
@Component
public class TranscriptValidator {

    /**
     * Validates transcript creation request.
     *
     * @param request transcript creation request
     */
    public void validateCreate(
            CreateTranscriptRequest request) {

        if (request == null) {

            throw new BadRequestException(
                    TranscriptMessages.NOT_FOUND
            );
        }

        if (request.getCallPublicId() == null
                || request.getCallPublicId().isBlank()) {

            throw new BadRequestException(
                    TranscriptMessages.CALL_ID_REQUIRED
            );
        }

        if (request.getSequenceNumber() == null) {

            throw new BadRequestException(
                    TranscriptMessages.SEQUENCE_NUMBER_REQUIRED
            );
        }

        if (request.getSequenceNumber()
                < TranscriptConstants.MIN_SEQUENCE_NUMBER) {

            throw new BadRequestException(
                    TranscriptMessages.SEQUENCE_NUMBER_INVALID
            );
        }

        if (request.getSpeakerType() == null
                || request.getSpeakerType().isBlank()) {

            throw new BadRequestException(
                    TranscriptMessages.SPEAKER_TYPE_REQUIRED
            );
        }

        if (request.getText() == null
                || request.getText().isBlank()) {

            throw new BadRequestException(
                    TranscriptMessages.TEXT_REQUIRED
            );
        }

        validateTimeRange(
                request.getStartedAt(),
                request.getEndedAt()
        );
    }

    /**
     * Validates transcript update request.
     *
     * @param request transcript update request
     */
    public void validateUpdate(
            UpdateTranscriptRequest request) {

        if (request == null) {

            throw new BadRequestException(
                    TranscriptMessages.NOT_FOUND
            );
        }

        if (request.getSequenceNumber() != null
                && request.getSequenceNumber()
                < TranscriptConstants.MIN_SEQUENCE_NUMBER) {

            throw new BadRequestException(
                    TranscriptMessages.SEQUENCE_NUMBER_INVALID
            );
        }

        if (request.getSpeakerType() != null
                && request.getSpeakerType().isBlank()) {

            throw new BadRequestException(
                    TranscriptMessages.SPEAKER_TYPE_REQUIRED
            );
        }

        if (request.getText() != null
                && request.getText().isBlank()) {

            throw new BadRequestException(
                    TranscriptMessages.TEXT_REQUIRED
            );
        }

        validateTimeRange(
                request.getStartedAt(),
                request.getEndedAt()
        );
    }

    /**
     * Validates transcript start and end timestamps.
     *
     * @param startedAt transcript start time
     * @param endedAt transcript end time
     */
    private void validateTimeRange(
            java.time.LocalDateTime startedAt,
            java.time.LocalDateTime endedAt) {

        if (startedAt == null
                && endedAt == null) {

            return;
        }

        if (startedAt == null) {

            throw new BadRequestException(
                    TranscriptMessages.STARTED_AT_INVALID
            );
        }

        if (endedAt == null) {

            throw new BadRequestException(
                    TranscriptMessages.ENDED_AT_INVALID
            );
        }

        if (endedAt.isBefore(startedAt)) {

            throw new BadRequestException(
                    TranscriptMessages.ENDED_AT_INVALID
            );
        }
    }
}
package com.infinitio.aivoiceplatform.transcript.service.impl;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.infinitio.aivoiceplatform.call.entity.Call;
import com.infinitio.aivoiceplatform.call.repository.CallRepository;
import com.infinitio.aivoiceplatform.exception.ConflictException;
import com.infinitio.aivoiceplatform.exception.ResourceNotFoundException;
import com.infinitio.aivoiceplatform.transcript.constant.TranscriptConstants;
import com.infinitio.aivoiceplatform.transcript.constant.TranscriptMessages;
import com.infinitio.aivoiceplatform.transcript.dto.request.CreateTranscriptRequest;
import com.infinitio.aivoiceplatform.transcript.dto.request.UpdateTranscriptRequest;
import com.infinitio.aivoiceplatform.transcript.dto.response.TranscriptResponse;
import com.infinitio.aivoiceplatform.transcript.entity.Transcript;
import com.infinitio.aivoiceplatform.transcript.mapper.TranscriptMapper;
import com.infinitio.aivoiceplatform.transcript.repository.TranscriptRepository;
import com.infinitio.aivoiceplatform.transcript.service.TranscriptService;
import com.infinitio.aivoiceplatform.transcript.validator.TranscriptValidator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service implementation for Transcript operations.
 *
 * <p>
 * Transcript records are persisted in MySQL and are associated
 * directly with a Call.
 * </p>
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class TranscriptServiceImpl
        implements TranscriptService {

    private static final Integer ACTIVE = 1;

    private static final Integer DELETED = 1;

    private static final Integer NOT_DELETED = 0;

    private final TranscriptRepository transcriptRepository;

    private final CallRepository callRepository;

    private final TranscriptMapper transcriptMapper;

    private final TranscriptValidator transcriptValidator;

    /**
     * {@inheritDoc}
     */
    @Override
    public TranscriptResponse create(
            CreateTranscriptRequest request) {

        log.info(
                "Creating transcript. callPublicId={}, sequenceNumber={}",
                request != null
                        ? request.getCallPublicId()
                        : null,
                request != null
                        ? request.getSequenceNumber()
                        : null
        );

        transcriptValidator.validateCreate(
                request
        );

        Call call =
                getCall(
                        request.getCallPublicId()
                );

        validateSequenceNumber(
                call.getId(),
                request.getSequenceNumber(),
                null
        );

        Transcript transcript =
                transcriptMapper.toEntity(
                        request,
                        call
                );

        Transcript savedTranscript =
                transcriptRepository.save(
                        transcript
                );

        log.info(
                "Transcript created successfully. publicId={}, callPublicId={}, sequenceNumber={}",
                savedTranscript.getPublicId(),
                call.getPublicId(),
                savedTranscript.getSequenceNumber()
        );

        return transcriptMapper.toResponse(
                savedTranscript
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public TranscriptResponse getByPublicId(
            String publicId) {

        log.debug(
                "Fetching transcript. publicId={}",
                publicId
        );

        Transcript transcript =
                getActiveTranscript(
                        publicId
                );

        return transcriptMapper.toResponse(
                transcript
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public List<TranscriptResponse> getByCallPublicId(
            String callPublicId) {

        log.debug(
                "Fetching transcripts by call. callPublicId={}",
                callPublicId
        );

        if (callPublicId == null
                || callPublicId.isBlank()) {

            throw new ResourceNotFoundException(
                    TranscriptMessages.CALL_NOT_FOUND
            );
        }

        Call call =
                getCall(
                        callPublicId
                );

        return transcriptRepository
                .findByCallIdOrderBySequenceNumberAsc(
                        call.getId(),
                        PageRequest.of(
                                TranscriptConstants.DEFAULT_PAGE,
                                TranscriptConstants.MAX_PAGE_SIZE
                        )
                )
                .getContent()
                .stream()
                .filter(transcript ->
                        NOT_DELETED.equals(
                                transcript.getIsDeleted()
                        )
                )
                .map(transcriptMapper::toResponse)
                .toList();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TranscriptResponse update(
            String publicId,
            UpdateTranscriptRequest request) {

        log.info(
                "Updating transcript. publicId={}",
                publicId
        );

        transcriptValidator.validateUpdate(
                request
        );

        Transcript transcript =
                getActiveTranscript(
                        publicId
                );

        if (request.getSequenceNumber() != null
                && !request.getSequenceNumber()
                .equals(transcript.getSequenceNumber())) {

            validateSequenceNumber(
                    transcript.getCall().getId(),
                    request.getSequenceNumber(),
                    transcript.getId()
            );
        }

        transcriptMapper.updateEntity(
                request,
                transcript
        );

        Transcript savedTranscript =
                transcriptRepository.save(
                        transcript
                );

        log.info(
                "Transcript updated successfully. publicId={}",
                savedTranscript.getPublicId()
        );

        return transcriptMapper.toResponse(
                savedTranscript
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void delete(
            String publicId) {

        log.info(
                "Deleting transcript. publicId={}",
                publicId
        );

        Transcript transcript =
                getActiveTranscript(
                        publicId
                );

        transcript.setIsDeleted(
                DELETED
        );

        transcript.setIsActive(
                0
        );

        transcriptRepository.save(
                transcript
        );

        log.info(
                "Transcript deleted successfully. publicId={}",
                publicId
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void activate(
            String publicId) {

        log.info(
                "Activating transcript. publicId={}",
                publicId
        );

        Transcript transcript =
                getTranscript(
                        publicId
                );

        transcript.setIsActive(
                ACTIVE
        );

        transcript.setIsDeleted(
                NOT_DELETED
        );

        transcriptRepository.save(
                transcript
        );

        log.info(
                "Transcript activated successfully. publicId={}",
                publicId
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deactivate(
            String publicId) {

        log.info(
                "Deactivating transcript. publicId={}",
                publicId
        );

        Transcript transcript =
                getActiveTranscript(
                        publicId
                );

        transcript.setIsActive(
                0
        );

        transcriptRepository.save(
                transcript
        );

        log.info(
                "Transcript deactivated successfully. publicId={}",
                publicId
        );
    }

    /**
     * Retrieves a Call using its public identifier.
     *
     * @param callPublicId call public identifier
     * @return call entity
     */
    private Call getCall(
            String callPublicId) {

        return callRepository
                .findByPublicId(
                        callPublicId
                )
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                TranscriptMessages.CALL_NOT_FOUND
                        )
                );
    }

    /**
     * Retrieves an existing transcript.
     *
     * @param publicId transcript public identifier
     * @return transcript entity
     */
    private Transcript getTranscript(
            String publicId) {

        return transcriptRepository
                .findByPublicId(
                        publicId
                )
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                TranscriptMessages.NOT_FOUND
                        )
                );
    }

    /**
     * Retrieves an active, non-deleted transcript.
     *
     * @param publicId transcript public identifier
     * @return transcript entity
     */
    private Transcript getActiveTranscript(
            String publicId) {

        Transcript transcript =
                getTranscript(
                        publicId
                );

        if (!NOT_DELETED.equals(
                transcript.getIsDeleted())) {

            throw new ResourceNotFoundException(
                    TranscriptMessages.NOT_FOUND
            );
        }

        if (!ACTIVE.equals(
                transcript.getIsActive())) {

            throw new ResourceNotFoundException(
                    TranscriptMessages.NOT_FOUND
            );
        }

        return transcript;
    }

    /**
     * Validates transcript sequence uniqueness within a call.
     *
     * @param callId database identifier of the call
     * @param sequenceNumber sequence number
     * @param transcriptId current transcript database identifier
     */
    private void validateSequenceNumber(
            Long callId,
            Integer sequenceNumber,
            Long transcriptId) {

        boolean exists =
                transcriptRepository
                        .existsByCallIdAndSequenceNumber(
                                callId,
                                sequenceNumber
                        );

        if (!exists) {
            return;
        }

        if (transcriptId != null) {

            Transcript existingTranscript =
                    transcriptRepository
                            .findByCallIdOrderBySequenceNumberAsc(
                                    callId,
                                    PageRequest.of(
                                            TranscriptConstants.DEFAULT_PAGE,
                                            TranscriptConstants.MAX_PAGE_SIZE
                                    )
                            )
                            .getContent()
                            .stream()
                            .filter(transcript ->
                                    sequenceNumber.equals(
                                            transcript.getSequenceNumber()
                                    )
                            )
                            .findFirst()
                            .orElse(null);

            if (existingTranscript == null
                    || transcriptId.equals(
                    existingTranscript.getId()
            )) {

                return;
            }
        }

        throw new ConflictException(
                TranscriptMessages
                        .SEQUENCE_NUMBER_ALREADY_EXISTS
        );
    }
}
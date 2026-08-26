package com.infinitio.aivoiceplatform.callrecording.service.impl;

import com.infinitio.aivoiceplatform.call.entity.Call;
import com.infinitio.aivoiceplatform.call.validator.CallValidator;
import com.infinitio.aivoiceplatform.callrecording.constant.CallRecordingMessages;
import com.infinitio.aivoiceplatform.callrecording.dto.request.CreateCallRecordingRequest;
import com.infinitio.aivoiceplatform.callrecording.dto.request.UpdateCallRecordingRequest;
import com.infinitio.aivoiceplatform.callrecording.dto.response.CallRecordingResponse;
import com.infinitio.aivoiceplatform.callrecording.entity.CallRecording;
import com.infinitio.aivoiceplatform.callrecording.mapper.CallRecordingMapper;
import com.infinitio.aivoiceplatform.callrecording.repository.CallRecordingRepository;
import com.infinitio.aivoiceplatform.callrecording.service.CallRecordingService;
import com.infinitio.aivoiceplatform.callrecording.validator.CallRecordingValidator;
import com.infinitio.aivoiceplatform.common.dto.PageResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service implementation for Call Recording.
 *
 * <p>
 * Handles normal CRUD operations as well as recording creation
 * received from the telephony provider webhook.
 * </p>
 *
 * <p>
 * Webhook recording creation is idempotent using the provider
 * recording URL. This prevents duplicate CallRecording records
 * when the provider retries the same webhook.
 * </p>
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CallRecordingServiceImpl
        implements CallRecordingService {

    private static final Integer NOT_DELETED = 0;

    private static final String DEFAULT_FILE_TYPE =
            "audio";

    private final CallRecordingRepository
            callRecordingRepository;

    private final CallRecordingMapper
            callRecordingMapper;

    private final CallRecordingValidator
            callRecordingValidator;

    private final CallValidator
            callValidator;

    /**
     * Creates a Call Recording through the normal application
     * API.
     *
     * @param request create request
     * @return created recording
     */
    @Override
    public CallRecordingResponse create(
            CreateCallRecordingRequest request) {

        log.info(
                "Creating Call Recording. "
                        + "callPublicId={}, fileName={}",
                request != null
                        ? request.getCallPublicId()
                        : null,
                request != null
                        ? request.getFileName()
                        : null
        );

        Call call =
                callValidator.validateAndGet(
                        request.getCallPublicId()
                );

        callRecordingValidator.validateForCreate(
                request
        );

        CallRecording recording =
                callRecordingMapper.toEntity(
                        request
                );

        recording.setCall(
                call
        );

        CallRecording savedRecording =
                callRecordingRepository.save(
                        recording
                );

        log.info(
                "Call Recording created successfully. "
                        + "publicId={}, callPublicId={}, fileUrl={}",
                savedRecording.getPublicId(),
                call.getPublicId(),
                savedRecording.getFileUrl()
        );

        return callRecordingMapper.toResponse(
                savedRecording
        );
    }

    /**
     * Creates a Call Recording from a telephony provider
     * webhook.
     *
     * <p>
     * This method is intentionally separate from the normal
     * create operation because provider webhooks already
     * contain the recording URL and do not require the complete
     * external CreateCallRecordingRequest DTO.
     * </p>
     *
     * <p>
     * If the same recording URL has already been persisted,
     * the existing record is returned instead of creating
     * another row.
     * </p>
     *
     * @param callPublicId platform Call public identifier
     * @param recordingUrl provider recording URL
     * @param durationSeconds recording duration
     * @param provider telephony provider
     * @return created or existing recording
     */
    @Override
    public CallRecordingResponse createFromWebhook(
            String callPublicId,
            String recordingUrl,
            Integer durationSeconds,
            String provider) {

        log.info(
                "Creating Call Recording from telephony webhook. "
                        + "callPublicId={}, provider={}, "
                        + "durationSeconds={}",
                callPublicId,
                provider,
                durationSeconds
        );

        /*
         * ---------------------------------------------------------
         * STEP 1: Validate required webhook values.
         * ---------------------------------------------------------
         */
        if (callPublicId == null
                || callPublicId.isBlank()) {

            log.warn(
                    "Ignoring recording webhook because "
                            + "callPublicId is missing."
            );

            throw new IllegalArgumentException(
                    "Call public ID is required."
            );
        }

        if (recordingUrl == null
                || recordingUrl.isBlank()) {

            log.warn(
                    "Ignoring recording webhook because "
                            + "recordingUrl is missing. "
                            + "callPublicId={}",
                    callPublicId
            );

            throw new IllegalArgumentException(
                    "Recording URL is required."
            );
        }

        /*
         * ---------------------------------------------------------
         * STEP 2: Find the platform Call.
         * ---------------------------------------------------------
         */
        Call call =
                callValidator.validateAndGet(
                        callPublicId
                );

        /*
         * ---------------------------------------------------------
         * STEP 3: Check duplicate recording URL.
         * ---------------------------------------------------------
         *
         * Exotel/provider webhooks may be delivered more than
         * once. Never create a second row for the same URL.
         */
        CallRecording existingRecording =
                callRecordingRepository
                        .findByFileUrl(
                                recordingUrl
                        )
                        .orElse(null);

        if (existingRecording != null) {

            log.info(
                    "Call Recording already exists. "
                            + "Returning existing recording. "
                            + "recordingPublicId={}, "
                            + "callPublicId={}, "
                            + "recordingUrl={}",
                    existingRecording.getPublicId(),
                    callPublicId,
                    recordingUrl
            );

            return callRecordingMapper.toResponse(
                    existingRecording
            );
        }

        /*
         * ---------------------------------------------------------
         * STEP 4: Build recording entity.
         * ---------------------------------------------------------
         */
        CallRecording recording =
                CallRecording.builder()
                        .call(call)
                        .fileName(
                                buildFileName(
                                        callPublicId
                                )
                        )
                        .fileUrl(
                                recordingUrl
                        )
                        .fileType(
                                DEFAULT_FILE_TYPE
                        )
                        .storageProvider(
                                provider
                        )
                        .durationSeconds(
                                durationSeconds
                        )
                        .description(
                                buildDescription(
                                        provider
                                )
                        )
                        .build();

        /*
         * ---------------------------------------------------------
         * STEP 5: Persist recording.
         * ---------------------------------------------------------
         */
        CallRecording savedRecording =
                callRecordingRepository.save(
                        recording
                );

        log.info(
                "Call Recording created from telephony webhook. "
                        + "recordingPublicId={}, "
                        + "callPublicId={}, "
                        + "provider={}, "
                        + "recordingUrl={}",
                savedRecording.getPublicId(),
                callPublicId,
                provider,
                recordingUrl
        );

        return callRecordingMapper.toResponse(
                savedRecording
        );
    }

    /**
     * Updates a Call Recording.
     *
     * @param request update request
     * @return updated recording
     */
    @Override
    public CallRecordingResponse update(
            UpdateCallRecordingRequest request) {

        log.info(
                "Updating Call Recording. publicId={}",
                request != null
                        ? request.getPublicId()
                        : null
        );

        callRecordingValidator.validateForUpdate(
                request
        );

        CallRecording recording =
                callRecordingValidator.validateAndGet(
                        request.getPublicId()
                );

        Call call =
                callValidator.validateAndGet(
                        request.getCallPublicId()
                );

        callRecordingMapper.updateEntity(
                request,
                recording
        );

        recording.setCall(
                call
        );

        CallRecording updatedRecording =
                callRecordingRepository.save(
                        recording
                );

        log.info(
                "Call Recording updated successfully. "
                        + "publicId={}, callPublicId={}",
                updatedRecording.getPublicId(),
                call.getPublicId()
        );

        return callRecordingMapper.toResponse(
                updatedRecording
        );
    }

    /**
     * Gets a Call Recording by public ID.
     *
     * @param publicId recording public identifier
     * @return recording response
     */
    @Override
    @Transactional(readOnly = true)
    public CallRecordingResponse getByPublicId(
            String publicId) {

        log.info(
                "Fetching Call Recording. publicId={}",
                publicId
        );

        CallRecording recording =
                callRecordingValidator.validateAndGet(
                        publicId
                );

        return callRecordingMapper.toResponse(
                recording
        );
    }

    /**
     * Gets all Call Recordings.
     *
     * @param page page number
     * @param size page size
     * @return paginated recordings
     */
    @Override
    @Transactional(readOnly = true)
    public PageResponse<CallRecordingResponse> getAll(
            int page,
            int size) {

        log.info(
                "Fetching Call Recordings. page={}, size={}",
                page,
                size
        );

        Page<CallRecording> result =
                callRecordingRepository.findAll(
                        PageRequest.of(
                                page,
                                size
                        )
                );

        return buildPageResponse(
                result
        );
    }

    /**
     * Gets recordings associated with a Call.
     *
     * @param callPublicId Call public identifier
     * @param page page number
     * @param size page size
     * @return paginated recordings
     */
    @Override
    @Transactional(readOnly = true)
    public PageResponse<CallRecordingResponse> getByCall(
            String callPublicId,
            int page,
            int size) {

        log.info(
                "Fetching recordings for Call. "
                        + "callPublicId={}, page={}, size={}",
                callPublicId,
                page,
                size
        );

        Call call =
                callValidator.validateAndGet(
                        callPublicId
                );

        Page<CallRecording> result =
                callRecordingRepository.findByCallId(
                        call.getId(),
                        PageRequest.of(
                                page,
                                size
                        )
                );

        return buildPageResponse(
                result
        );
    }

    /**
     * Builds paginated response.
     *
     * @param result recording page
     * @return page response
     */
    private PageResponse<CallRecordingResponse>
    buildPageResponse(
            Page<CallRecording> result) {

        return PageResponse
                .<CallRecordingResponse>builder()
                .content(
                        result.getContent()
                                .stream()
                                .map(
                                        callRecordingMapper
                                                ::toResponse
                                )
                                .toList()
                )
                .pageNumber(
                        result.getNumber()
                )
                .pageSize(
                        result.getSize()
                )
                .totalPages(
                        result.getTotalPages()
                )
                .totalElements(
                        result.getTotalElements()
                )
                .first(
                        result.isFirst()
                )
                .last(
                        result.isLast()
                )
                .build();
    }

    /**
     * Deletes a Call Recording.
     *
     * @param publicId recording public identifier
     */
    @Override
    public void delete(
            String publicId) {

        log.info(
                "Deleting Call Recording. publicId={}",
                publicId
        );

        CallRecording recording =
                callRecordingValidator.validateAndGet(
                        publicId
                );

        recording.markAsDeleted(
                1L
        );

        callRecordingRepository.save(
                recording
        );

        log.info(
                "Call Recording deleted successfully. "
                        + "publicId={}",
                publicId
        );
    }

    /**
     * Activates a Call Recording.
     *
     * @param publicId recording public identifier
     */
    @Override
    public void activate(
            String publicId) {

        log.info(
                "Activating Call Recording. publicId={}",
                publicId
        );

        CallRecording recording =
                callRecordingValidator.validateAndGet(
                        publicId
                );

        recording.activate(
                1L
        );

        callRecordingRepository.save(
                recording
        );

        log.info(
                "Call Recording activated successfully. "
                        + "publicId={}",
                publicId
        );
    }

    /**
     * Deactivates a Call Recording.
     *
     * @param publicId recording public identifier
     */
    @Override
    public void deactivate(
            String publicId) {

        log.info(
                "Deactivating Call Recording. publicId={}",
                publicId
        );

        CallRecording recording =
                callRecordingValidator.validateAndGet(
                        publicId
                );

        recording.deactivate(
                1L
        );

        callRecordingRepository.save(
                recording
        );

        log.info(
                "Call Recording deactivated successfully. "
                        + "publicId={}",
                publicId
        );
    }

    /**
     * Builds a deterministic file name for recordings received
     * through the telephony webhook.
     *
     * @param callPublicId Call public identifier
     * @return recording file name
     */
    private String buildFileName(
            String callPublicId) {

        return "call-recording-"
                + callPublicId
                + ".mp3";
    }

    /**
     * Builds a recording description.
     *
     * @param provider provider code
     * @return description
     */
    private String buildDescription(
            String provider) {

        if (provider == null
                || provider.isBlank()) {

            return "Recording received from telephony provider.";
        }

        return "Recording received from "
                + provider
                + " telephony provider.";
    }
}
package com.infinitio.aivoiceplatform.callrecording.service.impl;

import com.infinitio.aivoiceplatform.call.entity.Call;
import com.infinitio.aivoiceplatform.call.validator.CallValidator;
import com.infinitio.aivoiceplatform.callrecording.dto.request.CreateCallRecordingRequest;
import com.infinitio.aivoiceplatform.callrecording.dto.request.UpdateCallRecordingRequest;
import com.infinitio.aivoiceplatform.callrecording.dto.response.CallRecordingResponse;
import com.infinitio.aivoiceplatform.callrecording.entity.CallRecording;
import com.infinitio.aivoiceplatform.callrecording.mapper.CallRecordingMapper;
import com.infinitio.aivoiceplatform.callrecording.repository.CallRecordingRepository;
import com.infinitio.aivoiceplatform.callrecording.service.CallRecordingService;
import com.infinitio.aivoiceplatform.callrecording.service.CallRecordingStorageService;
import com.infinitio.aivoiceplatform.callrecording.validator.CallRecordingValidator;
import com.infinitio.aivoiceplatform.common.dto.PageResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;

/**
 * Service implementation for Call Recording.
 *
 * <p>
 * Handles CRUD operations and complete call recording
 * persistence received from the telephony provider.
 * </p>
 *
 * <p>
 * Provider recordings are downloaded to local storage.
 * The database stores the provider URL together with
 * the local file path.
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

    private static final String DEFAULT_FILE_TYPE =
            "mp3";

    private static final String LOCAL_STORAGE_PROVIDER =
            "LOCAL";

    private final CallRecordingRepository
            callRecordingRepository;

    private final CallRecordingMapper
            callRecordingMapper;

    private final CallRecordingValidator
            callRecordingValidator;

    private final CallValidator
            callValidator;

    private final CallRecordingStorageService
            callRecordingStorageService;

    /**
     * Creates a Call Recording through the normal API.
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
     * Creates a complete call recording from a telephony
     * provider webhook.
     *
     * <p>
     * The provider recording URL is downloaded and stored
     * locally. The local path is persisted in filePath.
     * </p>
     *
     * <p>
     * The method is idempotent using the provider recording URL.
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
                "Processing complete call recording. "
                        + "callPublicId={}, provider={}, "
                        + "durationSeconds={}",
                callPublicId,
                provider,
                durationSeconds
        );

        /*
         * ---------------------------------------------------------
         * STEP 1: Validate input.
         * ---------------------------------------------------------
         */

        if (callPublicId == null
                || callPublicId.isBlank()) {

            throw new IllegalArgumentException(
                    "Call public ID is required."
            );
        }

        if (recordingUrl == null
                || recordingUrl.isBlank()) {

            throw new IllegalArgumentException(
                    "Recording URL is required."
            );
        }

        /*
         * ---------------------------------------------------------
         * STEP 2: Find Call.
         * ---------------------------------------------------------
         */

        Call call =
                callValidator.validateAndGet(
                        callPublicId
                );

        /*
         * ---------------------------------------------------------
         * STEP 3: Check duplicate.
         * ---------------------------------------------------------
         *
         * Provider may retry the same webhook.
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
                            + "recordingPublicId={}, "
                            + "callPublicId={}",
                    existingRecording.getPublicId(),
                    callPublicId
            );

            /*
             * If the database record already has a local path,
             * the complete recording has already been stored.
             */
            if (existingRecording.getFilePath() != null
                    && !existingRecording
                    .getFilePath()
                    .isBlank()) {

                return callRecordingMapper.toResponse(
                        existingRecording
                );
            }

            /*
             * Existing DB row without local file.
             *
             * This can happen if an older implementation stored
             * only the provider URL.
             */
            try {

                CallRecordingStorageService.StoredCallRecording
                        storedRecording =
                        callRecordingStorageService.store(
                                recordingUrl,
                                callPublicId
                        );

                existingRecording.setFileName(
                        storedRecording.fileName()
                );

                existingRecording.setFilePath(
                        storedRecording.filePath()
                );

                existingRecording.setFileType(
                        storedRecording.fileType()
                );

                existingRecording.setStorageProvider(
                        LOCAL_STORAGE_PROVIDER
                );

                existingRecording.setDurationSeconds(
                        durationSeconds
                );

                CallRecording savedRecording =
                        callRecordingRepository.save(
                                existingRecording
                        );

                log.info(
                        "Existing CallRecording completed with "
                                + "local audio file. "
                                + "callPublicId={}, filePath={}",
                        callPublicId,
                        storedRecording.filePath()
                );

                return callRecordingMapper.toResponse(
                        savedRecording
                );

            } catch (IOException exception) {

                log.error(
                        "Unable to store existing call recording. "
                                + "callPublicId={}",
                        callPublicId,
                        exception
                );

                throw new IllegalStateException(
                        "Unable to store call recording.",
                        exception
                );
            }
        }

        /*
         * ---------------------------------------------------------
         * STEP 4: Download complete call recording.
         * ---------------------------------------------------------
         */

        CallRecordingStorageService.StoredCallRecording
                storedRecording;

        try {

            storedRecording =
                    callRecordingStorageService.store(
                            recordingUrl,
                            callPublicId
                    );

        } catch (IOException exception) {

            log.error(
                    "Unable to download complete call recording. "
                            + "callPublicId={}, provider={}",
                    callPublicId,
                    provider,
                    exception
            );

            throw new IllegalStateException(
                    "Unable to store complete call recording.",
                    exception
            );
        }

        /*
         * ---------------------------------------------------------
         * STEP 5: Build CallRecording.
         * ---------------------------------------------------------
         */

        CallRecording recording =
                CallRecording.builder()
                        .call(call)
                        .fileName(
                                storedRecording.fileName()
                        )
                        .fileUrl(
                                recordingUrl
                        )
                        .filePath(
                                storedRecording.filePath()
                        )
                        .fileType(
                                storedRecording.fileType()
                        )
                        .storageProvider(
                                LOCAL_STORAGE_PROVIDER
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
         * STEP 6: Save metadata.
         * ---------------------------------------------------------
         */

        CallRecording savedRecording =
                callRecordingRepository.save(
                        recording
                );

        log.info(
                "Complete call recording persisted successfully. "
                        + "recordingPublicId={}, "
                        + "callPublicId={}, "
                        + "filePath={}, "
                        + "durationSeconds={}",
                savedRecording.getPublicId(),
                callPublicId,
                savedRecording.getFilePath(),
                durationSeconds
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

        return callRecordingMapper.toResponse(
                updatedRecording
        );
    }

    /**
     * Gets a Call Recording by public ID.
     */
    @Override
    @Transactional(readOnly = true)
    public CallRecordingResponse getByPublicId(
            String publicId) {

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
     */
    @Override
    @Transactional(readOnly = true)
    public PageResponse<CallRecordingResponse> getAll(
            int page,
            int size) {

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
     */
    @Override
    @Transactional(readOnly = true)
    public PageResponse<CallRecordingResponse> getByCall(
            String callPublicId,
            int page,
            int size) {

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
     */
    @Override
    public void delete(
            String publicId) {

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
    }

    /**
     * Activates a Call Recording.
     */
    @Override
    public void activate(
            String publicId) {

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
    }

    /**
     * Deactivates a Call Recording.
     */
    @Override
    public void deactivate(
            String publicId) {

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
    }

    /**
     * Builds recording description.
     */
    private String buildDescription(
            String provider) {

        if (provider == null
                || provider.isBlank()) {

            return "Complete call recording.";
        }

        return "Complete call recording from "
                + provider
                + " telephony provider.";
    }
}
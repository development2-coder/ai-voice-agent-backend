package com.infinitio.aivoiceplatform.callrecording.service.impl;

import com.infinitio.aivoiceplatform.auth.service.TenantAccessService;
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
import com.infinitio.aivoiceplatform.exception.BadRequestException;
import com.infinitio.aivoiceplatform.exception.ConflictException;
import com.infinitio.aivoiceplatform.exception.ForbiddenException;
import com.infinitio.aivoiceplatform.exception.ResourceNotFoundException;
import com.infinitio.aivoiceplatform.user.entity.User;
import com.infinitio.aivoiceplatform.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.List;

/**
 * Service implementation for call recording management.
 *
 * User-facing operations validate access through the associated Call.
 * Provider webhook processing remains an internal runtime operation.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CallRecordingServiceImpl implements CallRecordingService {

    private static final Integer NOT_DELETED = 0;

    private static final String LOCAL_STORAGE_PROVIDER = "LOCAL";

    private final CallRecordingRepository callRecordingRepository;

    private final CallRecordingMapper callRecordingMapper;

    private final CallRecordingValidator callRecordingValidator;

    private final CallValidator callValidator;

    private final CallRecordingStorageService callRecordingStorageService;

    private final TenantAccessService tenantAccessService;

    private final UserRepository userRepository;

    /**
     * Creates a recording through the user-facing API.
     *
     * @param request creation request
     * @return created recording
     */
    @Override
    public CallRecordingResponse create(
            CreateCallRecordingRequest request) {

        callRecordingValidator.validateForCreate(request);

        Call call = callValidator.validateAndGet(
                request.getCallPublicId()
        );

        validateCallAccess(call);

        CallRecording recording =
                callRecordingMapper.toEntity(request);

        recording.setCall(call);
        recording.setCreatedBy(requireCurrentUserId());

        CallRecording savedRecording =
                callRecordingRepository.save(recording);

        log.info(
                "Call recording created. recordingPublicId={}, callPublicId={}",
                savedRecording.getPublicId(),
                call.getPublicId()
        );

        return buildResponse(savedRecording);
    }

    /**
     * Creates or completes a recording from a trusted telephony webhook.
     *
     * <p>This method intentionally does not use the logged-in user's tenant
     * because provider callbacks run in the telephony runtime context.
     * The callback endpoint itself must authenticate/verify the provider.</p>
     *
     * @param callPublicId platform call public ID
     * @param recordingUrl provider recording URL
     * @param durationSeconds recording duration
     * @param provider provider code
     * @return created or existing recording
     */
    /**
     * Creates or completes a recording from a trusted telephony webhook.
     *
     * <p>
     * Provider callbacks run in the telephony runtime context rather than
     * the logged-in user's tenant context. The callback endpoint must
     * independently authenticate or verify the telephony provider.
     * </p>
     *
     * <p>
     * An existing recording may be reused only when it belongs to the
     * same Call. A matching file URL alone is not sufficient.
     * </p>
     *
     * @param callPublicId platform call public ID
     * @param recordingUrl provider recording URL
     * @param durationSeconds recording duration
     * @param provider provider code
     * @return created or existing recording
     */
    @Override
    public CallRecordingResponse createFromWebhook(
            String callPublicId,
            String recordingUrl,
            Integer durationSeconds,
            String provider) {

        if (callPublicId == null || callPublicId.isBlank()) {
            throw new BadRequestException(
                    "Call public ID is required."
            );
        }

        if (recordingUrl == null || recordingUrl.isBlank()) {
            throw new BadRequestException(
                    "Recording URL is required."
            );
        }

        log.info(
                "Processing recording webhook. callPublicId={}, provider={}",
                callPublicId,
                provider
        );

        // Resolve the Call associated with this webhook.
        Call call = callValidator.validateAndGet(callPublicId);

        // Check whether the recording URL already exists.
        CallRecording existingRecording =
                callRecordingRepository.findByFileUrl(recordingUrl)
                        .orElse(null);

        if (existingRecording != null) {

            /*
             * Never reuse a recording solely because its URL matches.
             * The recording must belong to the Call from this webhook.
             */
            if (existingRecording.getCall() == null
                    || existingRecording.getCall().getId() == null
                    || !existingRecording.getCall().getId()
                    .equals(call.getId())) {

                log.warn(
                        "Recording URL is already associated with another call. "
                                + "incomingCallPublicId={}, existingRecordingPublicId={}",
                        callPublicId,
                        existingRecording.getPublicId()
                );

                throw new ConflictException(
                        "Recording URL is already associated with another call."
                );
            }

            /*
             * Return the existing recording only if its local file
             * has already been stored successfully.
             */
            if (existingRecording.getFilePath() != null
                    && !existingRecording.getFilePath().isBlank()) {

                log.info(
                        "Recording already stored. recordingPublicId={}, callPublicId={}",
                        existingRecording.getPublicId(),
                        callPublicId
                );

                return buildResponse(existingRecording);
            }
        }

        CallRecordingStorageService.StoredCallRecording storedRecording;

        try {
            storedRecording = callRecordingStorageService.store(
                    recordingUrl,
                    callPublicId
            );
        } catch (IOException exception) {

            log.error(
                    "Unable to store call recording. callPublicId={}, provider={}",
                    callPublicId,
                    provider,
                    exception
            );

            throw new IllegalStateException(
                    "Unable to store complete call recording.",
                    exception
            );
        }

        if (existingRecording != null) {

            // Complete the existing recording for the same Call.
            existingRecording.setFileName(storedRecording.fileName());
            existingRecording.setFilePath(storedRecording.filePath());
            existingRecording.setFileType(storedRecording.fileType());
            existingRecording.setStorageProvider(LOCAL_STORAGE_PROVIDER);
            existingRecording.setDurationSeconds(durationSeconds);

            CallRecording saved =
                    callRecordingRepository.save(existingRecording);

            log.info(
                    "Existing recording completed. recordingPublicId={}, callPublicId={}",
                    saved.getPublicId(),
                    callPublicId
            );

            return buildResponse(saved);
        }

        /*
         * The webhook does not invent a creator identity.
         * Preserve the creator associated with the original Call.
         */
        Long creatorId = call.getCreatedBy();

        if (creatorId == null) {
            log.error(
                    "Cannot determine recording creator. callPublicId={}",
                    callPublicId
            );

            throw new IllegalStateException(
                    "Unable to determine recording creator from the call."
            );
        }

        CallRecording recording = CallRecording.builder()
                .call(call)
                .fileName(storedRecording.fileName())
                .fileUrl(recordingUrl)
                .filePath(storedRecording.filePath())
                .fileType(storedRecording.fileType())
                .storageProvider(LOCAL_STORAGE_PROVIDER)
                .durationSeconds(durationSeconds)
                .description(buildDescription(provider))
                .createdBy(creatorId)
                .build();

        CallRecording saved = callRecordingRepository.save(recording);

        log.info(
                "Complete call recording saved. recordingPublicId={}, callPublicId={}, filePath={}",
                saved.getPublicId(),
                callPublicId,
                saved.getFilePath()
        );

        return buildResponse(saved);
    }

    /**
     * Updates recording metadata after authorizing both the existing
     * recording and the target Call.
     *
     * @param request update request
     * @return updated recording
     */
    @Override
    public CallRecordingResponse update(
            UpdateCallRecordingRequest request) {

        callRecordingValidator.validateForUpdate(request);

        CallRecording recording =
                callRecordingValidator.validateAndGet(
                        request.getPublicId()
                );

        validateCallAccess(recording.getCall());

        Call targetCall =
                callValidator.validateAndGet(
                        request.getCallPublicId()
                );

        validateCallAccess(targetCall);

        callRecordingMapper.updateEntity(request, recording);
        recording.setCall(targetCall);
        recording.setUpdatedBy(requireCurrentUserId());

        CallRecording saved =
                callRecordingRepository.save(recording);

        return buildResponse(saved);
    }

    /**
     * Retrieves recording metadata by public ID.
     *
     * @param publicId recording public ID
     * @return recording response
     */
    @Override
    @Transactional(readOnly = true)
    public CallRecordingResponse getByPublicId(String publicId) {

        CallRecording recording =
                callRecordingValidator.validateAndGet(publicId);

        validateCallAccess(recording.getCall());

        return buildResponse(recording);
    }

    /**
     * Lists non-deleted recordings accessible to the current user.
     *
     * @return accessible recordings
     */
    @Override
    @Transactional(readOnly = true)
    public PageResponse<CallRecordingResponse> getAll() {

        List<CallRecording> recordings;

        if (tenantAccessService.isSuperAdmin()) {
            recordings =
                    callRecordingRepository.findByIsDeleted(NOT_DELETED);
        } else {
            Long tenantId = requireCurrentTenantId();

            recordings =
                    callRecordingRepository.findAllAccessibleByTenant(
                            tenantId,
                            NOT_DELETED
                    );
        }

        return buildPageResponse(
                recordings.stream()
                        .map(this::buildResponse)
                        .toList()
        );
    }

    /**
     * Lists recordings belonging to an accessible Call.
     *
     * @param callPublicId call public ID
     * @return recordings for the call
     */
    @Override
    @Transactional(readOnly = true)
    public PageResponse<CallRecordingResponse> getByCall(
            String callPublicId) {

        Call call = callValidator.validateAndGet(callPublicId);
        validateCallAccess(call);

        List<CallRecording> recordings;

        if (tenantAccessService.isSuperAdmin()) {
            recordings =
                    callRecordingRepository.findByCallIdAndIsDeleted(
                            call.getId(),
                            NOT_DELETED
                    );
        } else {
            recordings =
                    callRecordingRepository.findByCallIdAccessibleByTenant(
                            call.getId(),
                            requireCurrentTenantId(),
                            NOT_DELETED
                    );
        }

        return buildPageResponse(
                recordings.stream()
                        .map(this::buildResponse)
                        .toList()
        );
    }

    /**
     * Soft-deletes an accessible recording.
     *
     * @param publicId recording public ID
     */
    @Override
    public void delete(String publicId) {

        CallRecording recording =
                callRecordingValidator.validateAndGet(publicId);

        validateCallAccess(recording.getCall());

        recording.markAsDeleted(requireCurrentUserId());
        callRecordingRepository.save(recording);
    }

    /**
     * Activates an accessible recording.
     *
     * @param publicId recording public ID
     */
    @Override
    public void activate(String publicId) {

        CallRecording recording =
                callRecordingValidator.validateAndGet(publicId);

        validateCallAccess(recording.getCall());

        recording.activate(requireCurrentUserId());
        callRecordingRepository.save(recording);
    }

    /**
     * Deactivates an accessible recording.
     *
     * @param publicId recording public ID
     */
    @Override
    public void deactivate(String publicId) {

        CallRecording recording =
                callRecordingValidator.validateAndGet(publicId);

        validateCallAccess(recording.getCall());

        recording.deactivate(requireCurrentUserId());
        callRecordingRepository.save(recording);
    }

    /**
     * Loads an accessible local audio file for browser playback.
     *
     * @param publicId recording public ID
     * @return local audio resource
     */
    @Override
    @Transactional(readOnly = true)
    public Resource getRecordingResource(String publicId) {

        CallRecording recording =
                callRecordingValidator.validateAndGet(publicId);

        validateCallAccess(recording.getCall());

        if (recording.getFilePath() == null
                || recording.getFilePath().isBlank()) {

            throw new ResourceNotFoundException(
                    "Local call recording file not found."
            );
        }

        return callRecordingStorageService.load(
                recording.getFilePath()
        );
    }

    /**
     * Validates access to the Call associated with a recording.
     *
     * Campaign calls are checked through Campaign -> Agent -> Tenant.
     * Direct calls are checked through the tenant of the call creator.
     *
     * @param call associated call
     */
    private void validateCallAccess(Call call) {

        if (call == null) {
            throw new ForbiddenException(
                    "Call information is unavailable."
            );
        }

        if (tenantAccessService.isSuperAdmin()) {
            return;
        }

        if (call.getCampaignContact() != null) {
            tenantAccessService.validateCampaignAccess(
                    call.getCampaignContact().getCampaign()
            );
            return;
        }

        Long creatorId = call.getCreatedBy();

        if (creatorId == null) {
            throw new ForbiddenException(
                    "The tenant for this direct call cannot be determined."
            );
        }

        User creator = userRepository.findById(creatorId)
                .orElseThrow(() -> new ForbiddenException(
                        "The tenant for this direct call cannot be determined."
                ));

        tenantAccessService.validateTenantAccess(creator.getTenant());
    }

    private Long requireCurrentUserId() {

        Long userId = tenantAccessService.getCurrentUserId();

        if (userId == null) {
            throw new ForbiddenException(
                    "Authenticated user information is unavailable."
            );
        }

        return userId;
    }

    private Long requireCurrentTenantId() {

        Long tenantId = tenantAccessService.getCurrentTenantId();

        if (tenantId == null) {
            throw new ForbiddenException(
                    "Current user's tenant is unavailable."
            );
        }

        return tenantId;
    }

    private PageResponse<CallRecordingResponse> buildPageResponse(
            List<CallRecordingResponse> recordings) {

        int totalElements = recordings.size();

        return PageResponse.<CallRecordingResponse>builder()
                .content(recordings)
                .pageNumber(0)
                .pageSize(totalElements)
                .totalPages(totalElements == 0 ? 0 : 1)
                .totalElements(totalElements)
                .first(true)
                .last(true)
                .build();
    }

    private CallRecordingResponse buildResponse(
            CallRecording recording) {

        CallRecordingResponse response =
                callRecordingMapper.toResponse(recording);

        response.setStreamUrl(
                "/api/v1/call-recordings/"
                        + recording.getPublicId()
                        + "/stream"
        );

        return response;
    }

    private String buildDescription(String provider) {

        if (provider == null || provider.isBlank()) {
            return "Complete call recording.";
        }

        return "Complete call recording from "
                + provider
                + " telephony provider.";
    }
}
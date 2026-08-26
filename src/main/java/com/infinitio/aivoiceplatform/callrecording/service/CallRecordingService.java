package com.infinitio.aivoiceplatform.callrecording.service;

import com.infinitio.aivoiceplatform.callrecording.dto.request.CreateCallRecordingRequest;
import com.infinitio.aivoiceplatform.callrecording.dto.request.UpdateCallRecordingRequest;
import com.infinitio.aivoiceplatform.callrecording.dto.response.CallRecordingResponse;
import com.infinitio.aivoiceplatform.common.dto.PageResponse;

/**
 * Service interface for Call Recording.
 *
 * <p>
 * Provides normal CRUD operations as well as an internal
 * webhook-driven operation used by the telephony runtime
 * when a provider supplies a recording URL.
 * </p>
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
public interface CallRecordingService {

    /**
     * Creates a Call Recording through the normal application API.
     *
     * @param request create request
     * @return created recording
     */
    CallRecordingResponse create(
            CreateCallRecordingRequest request
    );

    /**
     * Creates a Call Recording from a telephony provider webhook.
     *
     * <p>
     * This method is intended for internal runtime use.
     * It is idempotent with respect to the provider recording URL.
     * </p>
     *
     * @param callPublicId platform Call public identifier
     * @param recordingUrl provider recording URL
     * @param durationSeconds recording duration
     * @param provider telephony provider code
     * @return created or existing recording
     */
    CallRecordingResponse createFromWebhook(
            String callPublicId,
            String recordingUrl,
            Integer durationSeconds,
            String provider
    );

    /**
     * Updates a Call Recording.
     *
     * @param request update request
     * @return updated recording
     */
    CallRecordingResponse update(
            UpdateCallRecordingRequest request
    );

    /**
     * Gets a Call Recording by public ID.
     *
     * @param publicId recording public identifier
     * @return recording
     */
    CallRecordingResponse getByPublicId(
            String publicId
    );

    /**
     * Gets all Call Recordings.
     *
     * @param page page number
     * @param size page size
     * @return paginated recordings
     */
    PageResponse<CallRecordingResponse> getAll(
            int page,
            int size
    );

    /**
     * Gets recordings associated with a Call.
     *
     * @param callPublicId Call public identifier
     * @param page page number
     * @param size page size
     * @return paginated recordings
     */
    PageResponse<CallRecordingResponse> getByCall(
            String callPublicId,
            int page,
            int size
    );

    /**
     * Deletes a Call Recording.
     *
     * @param publicId recording public identifier
     */
    void delete(
            String publicId
    );

    /**
     * Activates a Call Recording.
     *
     * @param publicId recording public identifier
     */
    void activate(
            String publicId
    );

    /**
     * Deactivates a Call Recording.
     *
     * @param publicId recording public identifier
     */
    void deactivate(
            String publicId
    );
}
package com.infinitio.aivoiceplatform.transcript.service;

import java.util.List;

import com.infinitio.aivoiceplatform.transcript.dto.request.CreateTranscriptRequest;
import com.infinitio.aivoiceplatform.transcript.dto.request.UpdateTranscriptRequest;
import com.infinitio.aivoiceplatform.transcript.dto.response.TranscriptResponse;

/**
 * Service contract for transcript operations.
 *
 * <p>
 * Transcript records are associated directly with a call.
 * </p>
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
public interface TranscriptService {

    /**
     * Creates a transcript segment for a call.
     *
     * @param request transcript creation request
     * @return created transcript
     */
    TranscriptResponse create(
            CreateTranscriptRequest request
    );

    /**
     * Retrieves a transcript by public identifier.
     *
     * @param publicId transcript public identifier
     * @return transcript response
     */
    TranscriptResponse getByPublicId(
            String publicId
    );

    /**
     * Retrieves all transcripts belonging to a call.
     *
     * @param callPublicId call public identifier
     * @return transcripts ordered by sequence number
     */
    List<TranscriptResponse> getByCallPublicId(
            String callPublicId
    );

    /**
     * Updates a transcript.
     *
     * @param publicId transcript public identifier
     * @param request update request
     * @return updated transcript
     */
    TranscriptResponse update(
            String publicId,
            UpdateTranscriptRequest request
    );

    /**
     * Soft deletes a transcript.
     *
     * @param publicId transcript public identifier
     */
    void delete(
            String publicId
    );

    /**
     * Activates a transcript.
     *
     * @param publicId transcript public identifier
     */
    void activate(
            String publicId
    );

    /**
     * Deactivates a transcript.
     *
     * @param publicId transcript public identifier
     */
    void deactivate(
            String publicId
    );
}
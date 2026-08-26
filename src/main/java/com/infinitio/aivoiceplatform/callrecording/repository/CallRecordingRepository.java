package com.infinitio.aivoiceplatform.callrecording.repository;

import com.infinitio.aivoiceplatform.callrecording.entity.CallRecording;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for Call Recording persistence operations.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Repository
public interface CallRecordingRepository
        extends JpaRepository<CallRecording, Long> {

    /**
     * Finds a Call Recording by public ID.
     *
     * @param publicId recording public identifier
     * @return recording
     */
    Optional<CallRecording> findByPublicId(
            String publicId
    );

    /**
     * Checks whether a recording URL already exists.
     *
     * @param fileUrl recording URL
     * @return true when URL exists
     */
    boolean existsByFileUrl(
            String fileUrl
    );

    /**
     * Finds a recording by its provider URL.
     *
     * <p>
     * Used by telephony webhook processing to make recording
     * creation idempotent.
     * </p>
     *
     * @param fileUrl provider recording URL
     * @return existing recording
     */
    Optional<CallRecording> findByFileUrl(
            String fileUrl
    );

    /**
     * Finds recordings associated with a Call.
     *
     * @param callId Call database ID
     * @param pageable pagination configuration
     * @return paginated recordings
     */
    Page<CallRecording> findByCallId(
            Long callId,
            Pageable pageable
    );
}
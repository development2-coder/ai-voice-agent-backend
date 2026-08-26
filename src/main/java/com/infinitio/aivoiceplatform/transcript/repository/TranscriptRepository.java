package com.infinitio.aivoiceplatform.transcript.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.infinitio.aivoiceplatform.transcript.entity.Transcript;

/**
 * Repository for Transcript.
 *
 * <p>
 * Provides persistent database operations for call transcripts.
 * Transcript records are associated directly with a call.
 * </p>
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Repository
public interface TranscriptRepository
        extends JpaRepository<Transcript, Long> {

    /**
     * Finds a transcript by its public identifier.
     *
     * @param publicId transcript public identifier
     * @return transcript when found
     */
    Optional<Transcript> findByPublicId(
            String publicId
    );

    /**
     * Checks whether a transcript sequence already exists
     * for a call.
     *
     * @param callId database identifier of the call
     * @param sequenceNumber transcript sequence number
     * @return true when the sequence already exists
     */
    boolean existsByCallIdAndSequenceNumber(
            Long callId,
            Integer sequenceNumber
    );

    /**
     * Retrieves transcripts belonging to a call in
     * sequence order.
     *
     * @param callId database identifier of the call
     * @param pageable pagination information
     * @return paginated transcripts
     */
    Page<Transcript>
    findByCallIdOrderBySequenceNumberAsc(
            Long callId,
            Pageable pageable
    );

    /**
     * Retrieves the latest transcript segment for a call.
     *
     * <p>
     * This is useful when determining the next sequence number.
     * </p>
     *
     * @param callId database identifier of the call
     * @param pageable pagination information
     * @return latest transcript segment
     */
    Page<Transcript>
    findByCallIdOrderBySequenceNumberDesc(
            Long callId,
            Pageable pageable
    );
}
package com.infinitio.aivoiceplatform.transcript.repository;

import com.infinitio.aivoiceplatform.transcript.entity.Transcript;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for persisted transcript segments.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Repository
public interface TranscriptRepository
        extends JpaRepository<Transcript, Long> {

    Optional<Transcript> findByPublicId(String publicId);

    Optional<Transcript> findByPublicIdAndIsDeleted(
            String publicId,
            Integer isDeleted
    );

    boolean existsByCallIdAndSequenceNumber(
            Long callId,
            Integer sequenceNumber
    );

    Page<Transcript> findByCallIdOrderBySequenceNumberAsc(
            Long callId,
            Pageable pageable
    );

    Page<Transcript> findByCallIdOrderBySequenceNumberDesc(
            Long callId,
            Pageable pageable
    );

    /**
     * Returns non-deleted transcript segments for a call,
     * ordered by their sequence number.
     */
    List<Transcript> findByCallIdAndIsDeletedOrderBySequenceNumberAsc(
            Long callId,
            Integer isDeleted
    );

    /**
     * Finds the complete runtime transcript record for a call.
     * Retained for idempotent runtime finalization.
     */
    Optional<Transcript> findFirstByCallIdAndSource(
            Long callId,
            String source
    );

    Optional<Transcript> findFirstByCallIdAndSourceAndIsDeleted(
            Long callId,
            String source,
            Integer isDeleted
    );
}
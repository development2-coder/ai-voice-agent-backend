package com.infinitio.aivoiceplatform.callsession.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.infinitio.aivoiceplatform.callsession.entity.CallSession;

/**
 * Repository for persistent call-session operations.
 *
 * <p>
 * Call-session runtime state is persisted in MySQL using
 * Spring Data JPA.
 * </p>
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Repository
public interface CallSessionRepository
        extends JpaRepository<CallSession, Long> {

    /**
     * Finds a call session by its public call identifier.
     *
     * @param callId public call identifier
     * @return call session when found
     */
    Optional<CallSession> findByCallId(
            String callId
    );

    /**
     * Finds a non-deleted call session by its public call identifier.
     *
     * @param callId public call identifier
     * @param isDeleted deleted flag
     * @return active call session when found
     */
    Optional<CallSession> findByCallIdAndIsDeleted(
            String callId,
            Integer isDeleted
    );

    /**
     * Checks whether a call session exists.
     *
     * @param callId public call identifier
     * @return true when the call session exists
     */
    boolean existsByCallId(
            String callId
    );

    /**
     * Checks whether a non-deleted call session exists.
     *
     * @param callId public call identifier
     * @param isDeleted deleted flag
     * @return true when a matching call session exists
     */
    boolean existsByCallIdAndIsDeleted(
            String callId,
            Integer isDeleted
    );
}
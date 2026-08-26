package com.infinitio.aivoiceplatform.telephony.repository;

import com.infinitio.aivoiceplatform.telephony.entity.TelephonyCallEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repository for telephony call event persistence.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
public interface TelephonyCallEventRepository
        extends JpaRepository<TelephonyCallEvent, Long> {

    /**
     * Finds an event using its public identifier.
     *
     * @param publicId public identifier
     * @return matching event
     */
    Optional<TelephonyCallEvent> findByPublicId(
            String publicId
    );

    /**
     * Checks whether a provider event already exists.
     *
     * @param providerEventId provider event identifier
     * @return true when the event exists
     */
    boolean existsByProviderEventId(
            String providerEventId
    );

    /**
     * Finds events for a call.
     *
     * @param callId call database identifier
     * @param pageable pagination information
     * @return call events
     */
    Page<TelephonyCallEvent>
    findByCallIdOrderByEventAtAsc(
            Long callId,
            Pageable pageable
    );

    /**
     * Finds events for a provider call identifier.
     *
     * @param providerCallId provider call identifier
     * @param pageable pagination information
     * @return call events
     */
    Page<TelephonyCallEvent>
    findByProviderCallIdOrderByEventAtAsc(
            String providerCallId,
            Pageable pageable
    );
}
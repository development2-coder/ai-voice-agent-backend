package com.infinitio.aivoiceplatform.aidialer.repository;

import com.infinitio.aivoiceplatform.aidialer.constant.CallAttemptStatus;
import com.infinitio.aivoiceplatform.aidialer.entity.DialerCall;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DialerCallRepository
        extends JpaRepository<DialerCall, Long> {

    Optional<DialerCall> findByPublicIdAndIsDeleted(
            String publicId,
            Integer isDeleted
    );

    List<DialerCall> findAllByDialerIdAndIsDeleted(
            Long dialerId,
            Integer isDeleted
    );

    List<DialerCall> findAllByCampaignContactIdAndIsDeleted(
            Long campaignContactId,
            Integer isDeleted
    );

    List<DialerCall> findAllByStatusAndIsDeleted(
            CallAttemptStatus status,
            Integer isDeleted
    );

    Optional<DialerCall> findFirstByDialerIdAndStatusAndIsDeletedOrderByCreatedAtAsc(
            Long dialerId,
            CallAttemptStatus status,
            Integer isDeleted
    );

    Optional<DialerCall> findByExotelCallId(
            String exotelCallId
    );
}
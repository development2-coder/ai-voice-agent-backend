package com.infinitio.aivoiceplatform.aidialer.repository;

import com.infinitio.aivoiceplatform.aidialer.constant.DialerStatus;
import com.infinitio.aivoiceplatform.aidialer.entity.AiDialer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for AI Dialer.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Repository
public interface AiDialerRepository
        extends JpaRepository<AiDialer, Long> {

    // =========================================================
    // FIND BY PUBLIC ID
    // =========================================================

    Optional<AiDialer> findByPublicIdAndIsDeleted(
            String publicId,
            Integer isDeleted
    );


    // =========================================================
    // FIND BY CAMPAIGN
    // =========================================================

    List<AiDialer> findAllByCampaignIdAndIsDeleted(
            Long campaignId,
            Integer isDeleted
    );


    // =========================================================
    // FIND BY STATUS
    // =========================================================

    List<AiDialer> findAllByStatusAndIsDeleted(
            DialerStatus status,
            Integer isDeleted
    );


    // =========================================================
    // FIND BY ACTIVE STATUS
    // =========================================================

    List<AiDialer> findAllByIsActiveAndIsDeleted(
            Integer isActive,
            Integer isDeleted
    );


    // =========================================================
    // FIND ALL NON-DELETED
    // =========================================================

    List<AiDialer> findAllByIsDeleted(
            Integer isDeleted
    );


}
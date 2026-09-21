package com.infinitio.aivoiceplatform.aidialer.repository;

import com.infinitio.aivoiceplatform.aidialer.constant.DialerStatus;
import com.infinitio.aivoiceplatform.aidialer.entity.AiDialer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    @Query("""
        SELECT d
        FROM AiDialer d
        JOIN FETCH d.campaign
        JOIN FETCH d.agent
        JOIN FETCH d.flow
        WHERE d.campaign.id = :campaignId
          AND d.isDeleted = :isDeleted
        """)
    List<AiDialer> findAllByCampaignIdAndIsDeleted(
            @Param("campaignId") Long campaignId,
            @Param("isDeleted") Integer isDeleted
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

    @Query("""
    SELECT d.campaign.publicId
    FROM AiDialer d
    WHERE d.id = :dialerId
      AND d.isDeleted = :isDeleted
    """)
    Optional<String> findCampaignPublicIdByDialerId(
            @Param("dialerId") Long dialerId,
            @Param("isDeleted") Integer isDeleted
    );

}
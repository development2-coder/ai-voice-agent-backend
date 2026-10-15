package com.infinitio.aivoiceplatform.callrecording.repository;

import com.infinitio.aivoiceplatform.callrecording.entity.CallRecording;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for call recording persistence and tenant-scoped queries.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Repository
public interface CallRecordingRepository
        extends JpaRepository<CallRecording, Long> {

    Optional<CallRecording> findByPublicId(String publicId);

    Optional<CallRecording> findByPublicIdAndIsDeleted(
            String publicId,
            Integer isDeleted
    );

    boolean existsByFileUrl(String fileUrl);

    Optional<CallRecording> findByFileUrl(String fileUrl);

    List<CallRecording> findByCallId(Long callId);

    List<CallRecording> findByCallIdAndIsDeleted(
            Long callId,
            Integer isDeleted
    );

    List<CallRecording> findByIsDeleted(Integer isDeleted);

    /**
     * Lists recordings belonging to the current tenant.
     *
     * Campaign calls are scoped through their campaign's agent tenant.
     * Direct calls are scoped through the tenant of the call creator.
     */
    @Query("""
            SELECT recording
            FROM CallRecording recording
            JOIN recording.call call
            LEFT JOIN call.campaignContact campaignContact
            LEFT JOIN campaignContact.campaign campaign
            LEFT JOIN campaign.agent agent
            LEFT JOIN User creator ON creator.id = call.createdBy
            WHERE recording.isDeleted = :isDeleted
              AND (
                    (
                        campaignContact IS NOT NULL
                        AND agent.tenant.id = :tenantId
                    )
                    OR
                    (
                        campaignContact IS NULL
                        AND creator.tenant.id = :tenantId
                    )
              )
            ORDER BY recording.createdAt DESC
            """)
    List<CallRecording> findAllAccessibleByTenant(
            @Param("tenantId") Long tenantId,
            @Param("isDeleted") Integer isDeleted
    );

    /**
     * Lists recordings belonging to a particular call, restricted
     * to the specified tenant.
     */
    @Query("""
            SELECT recording
            FROM CallRecording recording
            JOIN recording.call call
            LEFT JOIN call.campaignContact campaignContact
            LEFT JOIN campaignContact.campaign campaign
            LEFT JOIN campaign.agent agent
            LEFT JOIN User creator ON creator.id = call.createdBy
            WHERE call.id = :callId
              AND recording.isDeleted = :isDeleted
              AND (
                    (
                        campaignContact IS NOT NULL
                        AND agent.tenant.id = :tenantId
                    )
                    OR
                    (
                        campaignContact IS NULL
                        AND creator.tenant.id = :tenantId
                    )
              )
            ORDER BY recording.createdAt DESC
            """)
    List<CallRecording> findByCallIdAccessibleByTenant(
            @Param("callId") Long callId,
            @Param("tenantId") Long tenantId,
            @Param("isDeleted") Integer isDeleted
    );
}
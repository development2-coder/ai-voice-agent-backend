package com.infinitio.aivoiceplatform.call.repository;

import com.infinitio.aivoiceplatform.call.entity.Call;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Call persistence operations.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Repository
public interface CallRepository extends JpaRepository<Call, Long> {

    Optional<Call> findByPublicId(String publicId);

    boolean existsByPublicId(String publicId);

    boolean existsByProviderCallId(String providerCallId);

    boolean existsByProviderCallIdAndIdNot(
            String providerCallId,
            Long id
    );

    Optional<Call> findByPublicIdAndIsDeleted(
            String publicId,
            Integer isDeleted
    );

    /**
     * Lists all non-deleted calls. Use for SUPER_ADMIN only.
     */
    List<Call> findAllByIsDeletedOrderByCreatedAtDesc(
            Integer isDeleted
    );

    /**
     * Lists calls whose campaign belongs to the tenant, or whose
     * creating user belongs to the tenant for direct calls.
     */
    @Query("""
            SELECT call
            FROM Call call
            LEFT JOIN call.campaignContact campaignContact
            LEFT JOIN campaignContact.campaign campaign
            LEFT JOIN campaign.agent agent
            LEFT JOIN User creator ON creator.id = call.createdBy
            WHERE call.isDeleted = :isDeleted
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
            ORDER BY call.createdAt DESC
            """)
    List<Call> findAllByTenantIdAndIsDeleted(
            @Param("tenantId") Long tenantId,
            @Param("isDeleted") Integer isDeleted
    );

    /**
     * Checks that a particular call belongs to a tenant.
     * Supports campaign calls and direct calls.
     */
    @Query("""
            SELECT CASE WHEN COUNT(call) > 0 THEN true ELSE false END
            FROM Call call
            LEFT JOIN call.campaignContact campaignContact
            LEFT JOIN campaignContact.campaign campaign
            LEFT JOIN campaign.agent agent
            LEFT JOIN User creator ON creator.id = call.createdBy
            WHERE call.id = :callId
              AND call.isDeleted = :isDeleted
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
            """)
    boolean existsAccessibleByIdAndTenantId(
            @Param("callId") Long callId,
            @Param("tenantId") Long tenantId,
            @Param("isDeleted") Integer isDeleted
    );

    /**
     * Lists calls for a campaign contact without tenant filtering.
     * The service must authorize access to the campaign contact first.
     * Use this for SUPER_ADMIN access after that authorization.
     */
    @Query("""
            SELECT call
            FROM Call call
            WHERE call.campaignContact.id = :campaignContactId
              AND call.isDeleted = :isDeleted
            ORDER BY call.createdAt DESC
            """)
    List<Call> findAllByCampaignContactIdAndIsDeleted(
            @Param("campaignContactId") Long campaignContactId,
            @Param("isDeleted") Integer isDeleted
    );

    /**
     * Lists calls for a campaign contact within a tenant.
     */
    @Query("""
            SELECT call
            FROM Call call
            JOIN call.campaignContact campaignContact
            JOIN campaignContact.campaign campaign
            JOIN campaign.agent agent
            WHERE campaignContact.id = :campaignContactId
              AND agent.tenant.id = :tenantId
              AND call.isDeleted = :isDeleted
            ORDER BY call.createdAt DESC
            """)
    List<Call> findAllByCampaignContactIdAndTenantIdAndIsDeleted(
            @Param("campaignContactId") Long campaignContactId,
            @Param("tenantId") Long tenantId,
            @Param("isDeleted") Integer isDeleted
    );

    @Query("""
            SELECT call
            FROM Call call
            WHERE call.providerCallId = :providerCallId
            """)
    Optional<Call> findByProviderCallId(
            @Param("providerCallId") String providerCallId
    );

    /**
     * Native provider callback lookup retained for existing integrations.
     */
    @Query(
            value = """
                    SELECT id,
                           public_id,
                           campaign_contact_id,
                           provider,
                           provider_call_id,
                           transfer_requested,
                           transfer_destination,
                           from_number,
                           to_number,
                           direction,
                           status,
                           started_at,
                           answered_at,
                           ended_at,
                           duration_seconds,
                           failure_reason,
                           recording_url,
                           transcript_file_path,
                           description,
                           is_active,
                           created_at,
                           created_by,
                           updated_at,
                           updated_by,
                           is_deleted,
                           deleted_at
                    FROM calls
                    WHERE provider_call_id = :providerCallId
                    LIMIT 1
                    """,
            nativeQuery = true
    )
    Optional<Call> findByProviderCallIdNative(
            @Param("providerCallId") String providerCallId
    );

    /**
     * Retrieves non-deleted calls associated with a campaign.
     * Intended for SUPER_ADMIN after campaign access validation.
     *
     * @param campaignId campaign database identifier
     * @param isDeleted deletion status
     * @return calls belonging to the campaign
     */
    @Query("""
        SELECT call
        FROM Call call
        JOIN call.campaignContact campaignContact
        WHERE campaignContact.campaign.id = :campaignId
          AND call.isDeleted = :isDeleted
        ORDER BY call.createdAt DESC
        """)
    List<Call> findAllByCampaignIdAndIsDeleted(
            @Param("campaignId") Long campaignId,
            @Param("isDeleted") Integer isDeleted
    );

    /**
     * Retrieves non-deleted calls belonging to a campaign within a tenant.
     *
     * @param campaignId campaign database identifier
     * @param tenantId tenant database identifier
     * @param isDeleted deletion status
     * @return tenant-scoped campaign calls
     */
    @Query("""
        SELECT call
        FROM Call call
        JOIN call.campaignContact campaignContact
        JOIN campaignContact.campaign campaign
        JOIN campaign.agent agent
        WHERE campaign.id = :campaignId
          AND agent.tenant.id = :tenantId
          AND call.isDeleted = :isDeleted
        ORDER BY call.createdAt DESC
        """)
    List<Call> findAllByCampaignIdAndTenantIdAndIsDeleted(
            @Param("campaignId") Long campaignId,
            @Param("tenantId") Long tenantId,
            @Param("isDeleted") Integer isDeleted
    );
}
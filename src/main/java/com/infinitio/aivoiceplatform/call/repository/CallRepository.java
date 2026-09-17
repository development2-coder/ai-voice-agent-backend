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
public interface CallRepository
        extends JpaRepository<Call, Long> {

    /**
     * Finds a call using its public identifier.
     *
     * @param publicId public identifier
     * @return matching call
     */
    Optional<Call> findByPublicId(
            String publicId
    );

    /**
     * Checks whether a call exists using its public identifier.
     *
     * @param publicId public identifier
     * @return true when call exists
     */
    boolean existsByPublicId(
            String publicId
    );

    /**
     * Checks whether a provider call identifier already exists.
     *
     * @param providerCallId provider call identifier
     * @return true when provider call identifier exists
     */
    boolean existsByProviderCallId(
            String providerCallId
    );

    /**
     * Checks whether a provider call identifier exists
     * for another call.
     *
     * @param providerCallId provider call identifier
     * @param id current call database identifier
     * @return true when another call has the provider call identifier
     */
    boolean existsByProviderCallIdAndIdNot(
            String providerCallId,
            Long id
    );

    /**
     * Finds a non-deleted call using its public identifier.
     *
     * @param publicId public identifier
     * @param isDeleted deleted flag
     * @return matching call
     */
    Optional<Call> findByPublicIdAndIsDeleted(
            String publicId,
            Integer isDeleted
    );

    /**
     * Finds all non-deleted calls.
     *
     * <p>
     * This method is used only for SUPER_ADMIN access because
     * SUPER_ADMIN can view calls belonging to all tenants.
     * </p>
     *
     * @param isDeleted deleted flag
     * @return all matching calls
     */
    List<Call> findAllByIsDeletedOrderByCreatedAtDesc(
            Integer isDeleted
    );

    /**
     * Finds all non-deleted calls belonging to a tenant.
     *
     * <p>
     * Campaign calls are resolved through:
     *
     * <pre>
     * Call
     *  -> CampaignContact
     *  -> Campaign
     *  -> Agent
     *  -> Tenant
     * </pre>
     *
     * <p>
     * Direct Agent calls do not have a CampaignContact.
     * Those calls are resolved through the user who created
     * the call and that user's tenant.
     * </p>
     *
     * @param tenantId tenant database identifier
     * @param isDeleted deleted flag
     * @return tenant-scoped calls
     */
    @Query("""
            SELECT call
            FROM Call call
            LEFT JOIN call.campaignContact campaignContact
            LEFT JOIN campaignContact.campaign campaign
            LEFT JOIN campaign.agent agent
            LEFT JOIN User creator
                ON creator.id = call.createdBy
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
     * Finds all non-deleted calls associated with a campaign contact
     * and belonging to a tenant.
     *
     * @param campaignContactId campaign contact database identifier
     * @param tenantId tenant database identifier
     * @param isDeleted deleted flag
     * @return matching calls
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

    /**
     * Finds a call using its provider call identifier.
     *
     * @param providerCallId provider supplied call identifier
     * @return matching call
     */
    @Query("""
            SELECT call
            FROM Call call
            WHERE call.providerCallId = :providerCallId
            """)
    Optional<Call> findByProviderCallId(
            @Param("providerCallId") String providerCallId
    );

    /**
     * Finds a call using a native database comparison.
     *
     * @param providerCallId provider supplied call identifier
     * @return matching call
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
}
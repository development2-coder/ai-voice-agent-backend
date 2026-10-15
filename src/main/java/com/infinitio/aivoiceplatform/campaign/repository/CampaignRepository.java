package com.infinitio.aivoiceplatform.campaign.repository;

import com.infinitio.aivoiceplatform.campaign.entity.Campaign;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Campaign persistence operations.
 *
 * <p>
 * Tenant relationship:
 *
 * <pre>
 * Campaign
 *     ↓
 * Agent
 *     ↓
 * Tenant
 * </pre>
 *
 * Tenant-scoped queries should be used for ADMIN and USER operations
 * wherever a campaign is retrieved by its public identifier.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
public interface CampaignRepository extends JpaRepository<Campaign, Long> {

    /**
     * Finds a non-deleted Campaign by public ID.
     *
     * <p>
     * Use for global access only, such as SUPER_ADMIN operations
     * and trusted internal workflows that explicitly require it.
     * </p>
     *
     * @param publicId campaign public ID
     * @param isDeleted deleted flag
     * @return campaign when found
     */
    Optional<Campaign> findByPublicIdAndIsDeleted(
            String publicId,
            Integer isDeleted
    );

    /**
     * Finds a non-deleted Campaign by public ID and tenant ID.
     *
     * <p>
     * The tenant restriction is applied by the database query.
     * ADMIN and USER operations should use this method to avoid
     * loading another tenant's campaign by public ID.
     * </p>
     *
     * @param publicId campaign public ID
     * @param tenantId tenant database ID
     * @param isDeleted deleted flag
     * @return campaign when it belongs to the specified tenant
     */
    Optional<Campaign> findByPublicIdAndAgent_Tenant_IdAndIsDeleted(
            String publicId,
            Long tenantId,
            Integer isDeleted
    );

    /**
     * Finds all non-deleted campaigns globally.
     *
     * <p>
     * Used for SUPER_ADMIN access.
     * </p>
     *
     * @param isDeleted deleted flag
     * @return campaigns
     */
    List<Campaign> findByIsDeleted(Integer isDeleted);

    /**
     * Finds all non-deleted campaigns belonging to a tenant.
     *
     * <p>
     * Tenant is resolved through Campaign → Agent → Tenant.
     * </p>
     *
     * @param tenantId tenant database ID
     * @param isDeleted deleted flag
     * @return tenant campaigns
     */
    List<Campaign> findByAgent_Tenant_IdAndIsDeleted(
            Long tenantId,
            Integer isDeleted
    );

    /**
     * Checks whether Campaign code already exists.
     *
     * @param campaignCode campaign code
     * @param isDeleted deleted flag
     * @return true when a matching campaign exists
     */
    boolean existsByCampaignCodeAndIsDeleted(
            String campaignCode,
            Integer isDeleted
    );

    /**
     * Checks whether Campaign name already exists.
     *
     * @param campaignName campaign name
     * @param isDeleted deleted flag
     * @return true when a matching campaign exists
     */
    boolean existsByCampaignNameAndIsDeleted(
            String campaignName,
            Integer isDeleted
    );

    /**
     * Checks whether another Campaign has the same code.
     *
     * @param campaignCode campaign code
     * @param isDeleted deleted flag
     * @param publicId current campaign public ID
     * @return true when another campaign exists
     */
    boolean existsByCampaignCodeAndIsDeletedAndPublicIdNot(
            String campaignCode,
            Integer isDeleted,
            String publicId
    );

    /**
     * Checks whether another Campaign has the same name.
     *
     * @param campaignName campaign name
     * @param isDeleted deleted flag
     * @param publicId current campaign public ID
     * @return true when another campaign exists
     */
    boolean existsByCampaignNameAndIsDeletedAndPublicIdNot(
            String campaignName,
            Integer isDeleted,
            String publicId
    );
}

package com.infinitio.aivoiceplatform.campaign.repository;

import com.infinitio.aivoiceplatform.campaign.entity.Campaign;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repository for Campaign.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
public interface CampaignRepository
        extends JpaRepository<Campaign, Long> {

    /**
     * Finds a non-deleted campaign by public ID.
     *
     * @param publicId campaign public identifier
     * @param isDeleted deleted flag
     * @return campaign
     */
    Optional<Campaign> findByPublicIdAndIsDeleted(
            String publicId,
            Integer isDeleted
    );

    /**
     * Finds campaigns by deleted status.
     *
     * @param isDeleted deleted flag
     * @param pageable pagination information
     * @return campaigns
     */
    Page<Campaign> findByIsDeleted(
            Integer isDeleted,
            Pageable pageable
    );

    /**
     * Checks whether campaign code already exists.
     *
     * @param campaignCode campaign code
     * @param isDeleted deleted flag
     * @return true if exists
     */
    boolean existsByCampaignCodeAndIsDeleted(
            String campaignCode,
            Integer isDeleted
    );

    /**
     * Checks whether campaign name already exists.
     *
     * @param campaignName campaign name
     * @param isDeleted deleted flag
     * @return true if exists
     */
    boolean existsByCampaignNameAndIsDeleted(
            String campaignName,
            Integer isDeleted
    );

    /**
     * Checks whether campaign code exists for another campaign.
     *
     * @param campaignCode campaign code
     * @param isDeleted deleted flag
     * @param publicId current campaign public ID
     * @return true if another campaign exists
     */
    boolean existsByCampaignCodeAndIsDeletedAndPublicIdNot(
            String campaignCode,
            Integer isDeleted,
            String publicId
    );

    /**
     * Checks whether campaign name exists for another campaign.
     *
     * @param campaignName campaign name
     * @param isDeleted deleted flag
     * @param publicId current campaign public ID
     * @return true if another campaign exists
     */
    boolean existsByCampaignNameAndIsDeletedAndPublicIdNot(
            String campaignName,
            Integer isDeleted,
            String publicId
    );
}
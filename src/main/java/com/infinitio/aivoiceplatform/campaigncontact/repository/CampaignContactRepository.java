package com.infinitio.aivoiceplatform.campaigncontact.repository;

import com.infinitio.aivoiceplatform.campaigncontact.entity.CampaignContact;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repository for Campaign Contact.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
public interface CampaignContactRepository
        extends JpaRepository<CampaignContact, Long> {

    /**
     * Finds a campaign contact by public ID.
     *
     * @param publicId contact public identifier
     * @return campaign contact
     */
    Optional<CampaignContact> findByPublicId(
            String publicId
    );

    /**
     * Checks whether a phone number already exists
     * within a campaign.
     *
     * @param campaignId campaign database identifier
     * @param phoneNumber phone number
     * @return true if contact exists
     */
    boolean existsByCampaignIdAndPhoneNumber(
            Long campaignId,
            String phoneNumber
    );

    /**
     * Finds contacts belonging to a campaign.
     *
     * @param campaignId campaign database identifier
     * @param pageable pagination information
     * @return campaign contacts
     */
    Page<CampaignContact> findByCampaignId(
            Long campaignId,
            Pageable pageable
    );

    /**
     * Finds contacts that are ready for dialing.
     *
     * @param campaignId campaign database identifier
     * @param status contact status
     * @param isDeleted deleted flag
     * @param pageable pagination information
     * @return pending contacts
     */
    Page<CampaignContact>
    findByCampaignIdAndStatusAndIsDeletedOrderByPriorityDescIdAsc(
            Long campaignId,
            String status,
            Integer isDeleted,
            Pageable pageable
    );

    /**
     * Finds the next eligible campaign contact for dialing.
     *
     * @param campaignId campaign database identifier
     * @param status contact status
     * @param isDeleted deleted flag
     * @return next eligible campaign contact
     */
    Optional<CampaignContact>
    findFirstByCampaignIdAndStatusAndIsDeletedOrderByPriorityDescIdAsc(
            Long campaignId,
            String status,
            Integer isDeleted
    );

    Optional<CampaignContact>
    findFirstByCampaignIdAndStatusAndIsDeletedAndIsActiveOrderByPriorityDescIdAsc(
            Long campaignId,
            String status,
            Integer isDeleted,
            Integer isActive
    );


}
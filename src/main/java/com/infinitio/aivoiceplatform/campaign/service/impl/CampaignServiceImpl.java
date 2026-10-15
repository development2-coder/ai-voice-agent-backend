package com.infinitio.aivoiceplatform.campaign.service.impl;

import com.infinitio.aivoiceplatform.agent.entity.Agent;
import com.infinitio.aivoiceplatform.agent.validator.AgentValidator;
import com.infinitio.aivoiceplatform.auth.service.TenantAccessService;
import com.infinitio.aivoiceplatform.campaign.constant.CampaignConstants;
import com.infinitio.aivoiceplatform.campaign.dto.request.CreateCampaignRequest;
import com.infinitio.aivoiceplatform.campaign.dto.request.UpdateCampaignRequest;
import com.infinitio.aivoiceplatform.campaign.dto.response.CampaignResponse;
import com.infinitio.aivoiceplatform.campaign.entity.Campaign;
import com.infinitio.aivoiceplatform.campaign.mapper.CampaignMapper;
import com.infinitio.aivoiceplatform.campaign.repository.CampaignRepository;
import com.infinitio.aivoiceplatform.campaign.service.CampaignService;
import com.infinitio.aivoiceplatform.campaign.validator.CampaignValidator;
import com.infinitio.aivoiceplatform.campaigncontact.repository.CampaignContactRepository;
import com.infinitio.aivoiceplatform.common.dto.PageResponse;
import com.infinitio.aivoiceplatform.exception.BadRequestException;
import com.infinitio.aivoiceplatform.exception.ForbiddenException;
import com.infinitio.aivoiceplatform.exception.ResourceNotFoundException;
import com.infinitio.aivoiceplatform.phonenumber.entity.PhoneNumber;
import com.infinitio.aivoiceplatform.phonenumber.validator.PhoneNumberValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service implementation for Campaign.
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
 * <p>
 * SUPER_ADMIN can access campaigns across all tenants.
 * ADMIN and USER can access only campaigns belonging to
 * their current tenant.
 * </p>
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CampaignServiceImpl implements CampaignService {

    private static final Integer NOT_DELETED = 0;

    private final CampaignRepository campaignRepository;

    private final CampaignMapper campaignMapper;

    private final CampaignValidator campaignValidator;

    private final AgentValidator agentValidator;

    private final PhoneNumberValidator phoneNumberValidator;

    private final TenantAccessService tenantAccessService;

    private final CampaignContactRepository campaignContactRepository;


    // =========================================================
    // CREATE
    // =========================================================

    @Override
    public CampaignResponse create(CreateCampaignRequest request) {

        log.info(
                "Creating Campaign. Code : {}, Name : {}",
                request.getCampaignCode(),
                request.getCampaignName()
        );

        campaignValidator.validateForCreate(request);

        /*
         * Resolve Agent.
         */
        Agent agent = agentValidator.validateAndGet(
                request.getAgentPublicId()
        );

        /*
         * CRITICAL TENANT CHECK.
         *
         * Prevent ADMIN/USER from creating a Campaign
         * against another tenant's Agent.
         */
        tenantAccessService.validateAgentAccess(agent);

        /*
         * Resolve Phone Number.
         */
        PhoneNumber phoneNumber = phoneNumberValidator.validateAndGet(
                request.getPhoneNumberPublicId()
        );

        /*
         * Phone Number must belong to the same Agent.
         *
         * This prevents:
         *
         * Agent A + Phone Number belonging to Agent B
         */
        validatePhoneNumberBelongsToAgent(phoneNumber, agent);

        /*
         * Phone number's Agent must also be tenant-accessible.
         */
        tenantAccessService.validateAgentAccess(
                phoneNumber.getAgent()
        );

        Campaign campaign = campaignMapper.toEntity(request);

        campaign.setIsActive(0);

        campaign.setCreatedBy(
                tenantAccessService.getCurrentUserId()
        );

        campaign.setAgent(agent);

        campaign.setPhoneNumber(phoneNumber);

        Campaign savedCampaign = campaignRepository.save(campaign);

        log.info(
                "Campaign created successfully. Public Id : {}",
                savedCampaign.getPublicId()
        );

        return buildCampaignResponse(
                savedCampaign,
                campaignMapper.toResponse(savedCampaign)
        );
    }


    // =========================================================
    // UPDATE
    // =========================================================

    @Override
    public CampaignResponse update(UpdateCampaignRequest request) {

        log.info(
                "Updating Campaign. Public Id : {}",
                request.getPublicId()
        );

        campaignValidator.validateForUpdate(request);

        /*
         * Resolve existing Campaign.
         *
         * CRITICAL TENANT CHECK:
         * Resolve the Campaign inside the current user's
         * tenant scope before modifying it.
         */
        Campaign campaign = getCampaignForCurrentUser(
                request.getPublicId()
        );

        /*
         * CRITICAL:
         *
         * Validate access to the existing Campaign
         * before modifying it.
         */
        tenantAccessService.validateAgentAccess(
                campaign.getAgent()
        );

        /*
         * Resolve new Agent.
         */
        Agent agent = agentValidator.validateAndGet(
                request.getAgentPublicId()
        );

        /*
         * Prevent moving Campaign to another tenant.
         */
        tenantAccessService.validateAgentAccess(agent);

        /*
         * Resolve Phone Number.
         */
        PhoneNumber phoneNumber = phoneNumberValidator.validateAndGet(
                request.getPhoneNumberPublicId()
        );

        /*
         * Phone Number must belong to the selected Agent.
         */
        validatePhoneNumberBelongsToAgent(phoneNumber, agent);

        tenantAccessService.validateAgentAccess(
                phoneNumber.getAgent()
        );

        campaignMapper.updateEntity(request, campaign);

        campaign.setAgent(agent);

        campaign.setPhoneNumber(phoneNumber);

        campaign.setUpdatedBy(
                tenantAccessService.getCurrentUserId()
        );

        Campaign updatedCampaign = campaignRepository.save(campaign);

        log.info(
                "Campaign updated successfully. Public Id : {}",
                updatedCampaign.getPublicId()
        );

        return buildCampaignResponse(
                updatedCampaign,
                campaignMapper.toResponse(updatedCampaign)
        );
    }


    // =========================================================
    // GET BY PUBLIC ID
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public CampaignResponse getByPublicId(String publicId) {

        log.info(
                "Fetching Campaign. Public Id : {}",
                publicId
        );

        Campaign campaign = getCampaignForCurrentUser(publicId);

        /*
         * Prevent cross-tenant data access.
         */
        tenantAccessService.validateAgentAccess(
                campaign.getAgent()
        );

        return buildCampaignResponse(
                campaign,
                campaignMapper.toResponse(campaign)
        );
    }


    // =========================================================
    // GET ALL
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public PageResponse<CampaignResponse> getAll() {

        log.info(
                "Fetching campaigns for current user."
        );

        List<Campaign> campaigns;

        if (tenantAccessService.isSuperAdmin()) {

            /*
             * SUPER_ADMIN can see all tenants.
             */
            campaigns = campaignRepository.findByIsDeleted(
                    NOT_DELETED
            );

        } else {

            /*
             * ADMIN and USER can see only their own tenant.
             */
            Long tenantId = tenantAccessService.getCurrentTenantId();

            if (tenantId == null) {
                log.warn(
                        "Cannot fetch campaigns without tenant context."
                );

                throw new ForbiddenException(
                        "Current user's tenant is unavailable."
                );
            }

            campaigns = campaignRepository
                    .findByAgent_Tenant_IdAndIsDeleted(
                            tenantId,
                            NOT_DELETED
                    );
        }

        List<CampaignResponse> content = campaigns.stream()
                .map(campaign ->
                        buildCampaignResponse(
                                campaign,
                                campaignMapper.toResponse(campaign)
                        )
                )
                .toList();

        int totalElements = content.size();

        return PageResponse
                .<CampaignResponse>builder()
                .content(content)
                .pageNumber(0)
                .pageSize(totalElements)
                .totalPages(totalElements == 0 ? 0 : 1)
                .totalElements(totalElements)
                .first(true)
                .last(true)
                .build();
    }


    // =========================================================
    // DELETE
    // =========================================================

    @Override
    public void delete(String publicId) {

        log.info(
                "Deleting Campaign. Public Id : {}",
                publicId
        );

        Campaign campaign = getCampaignForCurrentUser(publicId);

        /*
         * Prevent cross-tenant deletion.
         */
        tenantAccessService.validateAgentAccess(
                campaign.getAgent()
        );

        campaign.markAsDeleted(
                tenantAccessService.getCurrentUserId()
        );

        campaignRepository.save(campaign);

        log.info(
                "Campaign deleted successfully. Public Id : {}",
                publicId
        );
    }


    // =========================================================
    // ACTIVATE
    // =========================================================

    @Override
    public void activate(String publicId) {

        log.info(
                "Activating Campaign. Public Id : {}",
                publicId
        );

        Campaign campaign = getCampaignForCurrentUser(publicId);

        tenantAccessService.validateAgentAccess(
                campaign.getAgent()
        );

        campaign.activate(
                tenantAccessService.getCurrentUserId()
        );

        campaign.setStatus(
                CampaignConstants.STATUS_ACTIVE
        );

        campaignRepository.save(campaign);

        log.info(
                "Campaign activated successfully. Public Id : {}",
                publicId
        );
    }


    // =========================================================
    // DEACTIVATE
    // =========================================================

    @Override
    public void deactivate(String publicId) {

        log.info(
                "Deactivating Campaign. Public Id : {}",
                publicId
        );

        Campaign campaign = getCampaignForCurrentUser(publicId);

        tenantAccessService.validateAgentAccess(
                campaign.getAgent()
        );

        campaign.deactivate(
                tenantAccessService.getCurrentUserId()
        );

        campaign.setStatus(
                CampaignConstants.STATUS_INACTIVE
        );

        campaignRepository.save(campaign);

        log.info(
                "Campaign deactivated successfully. Public Id : {}",
                publicId
        );
    }


    // =========================================================
    // SCHEDULER LIFECYCLE
    // =========================================================

    @Override
    public void complete(String publicId) {

        log.info(
                "Completing Campaign from scheduler. Public Id : {}",
                publicId
        );

        /*
         * This method is intended for the trusted scheduler.
         *
         * It deliberately does not use the current user's tenant
         * context because scheduled jobs may run without an
         * authenticated HTTP user.
         *
         * The scheduler must obtain this public ID from a trusted
         * campaign scheduling record, not from an untrusted request.
         */
        Campaign campaign = campaignValidator.validateAndGet(
                publicId
        );

        if (CampaignConstants.STATUS_COMPLETED.equalsIgnoreCase(
                campaign.getStatus()
        )) {
            return;
        }

        campaign.setStatus(
                CampaignConstants.STATUS_COMPLETED
        );

        campaign.setIsActive(0);

        campaignRepository.save(campaign);

        log.info(
                "Campaign marked COMPLETED. Public Id : {}",
                publicId
        );
    }


    // =========================================================
    // HELPERS
    // =========================================================

    /**
     * Ensures that a PhoneNumber belongs to the Agent
     * selected for the Campaign.
     *
     * @param phoneNumber phone number
     * @param agent selected agent
     */
    private void validatePhoneNumberBelongsToAgent(
            PhoneNumber phoneNumber,
            Agent agent) {

        if (phoneNumber == null
                || phoneNumber.getAgent() == null
                || agent == null
                || !phoneNumber.getAgent()
                .getId()
                .equals(agent.getId())) {

            throw new BadRequestException(
                    "Phone number must belong to the selected Agent."
            );
        }
    }


    /**
     * Adds current contact count to Campaign response.
     *
     * @param campaign campaign
     * @param response campaign response
     * @return campaign response
     */
    private CampaignResponse buildCampaignResponse(
            Campaign campaign,
            CampaignResponse response) {

        long contactCount = campaignContactRepository
                .countByCampaignIdAndIsDeleted(
                        campaign.getId(),
                        NOT_DELETED
                );

        response.setContactCount(contactCount);

        return response;
    }


    // =========================================================
    // TENANT-SCOPED CAMPAIGN LOOKUP
    // =========================================================

    /**
     * Loads a non-deleted campaign inside the current user's
     * authorized scope.
     *
     * <p>
     * SUPER_ADMIN can load campaigns across all tenants.
     * ADMIN and USER can load campaigns only from their tenant.
     * </p>
     *
     * <p>
     * This lookup applies tenant isolation in the database query
     * instead of loading a campaign globally and checking ownership
     * afterward.
     * </p>
     *
     * @param publicId campaign public identifier
     * @return campaign within the current user's authorized scope
     */
    private Campaign getCampaignForCurrentUser(String publicId) {

        if (publicId == null || publicId.isBlank()) {
            throw new BadRequestException(
                    "Campaign public ID is required."
            );
        }

        String normalizedPublicId = publicId.trim();

        if (tenantAccessService.isSuperAdmin()) {
            return campaignValidator.validateAndGet(
                    normalizedPublicId
            );
        }

        Long tenantId = tenantAccessService.getCurrentTenantId();

        if (tenantId == null) {
            log.warn(
                    "Cannot resolve campaign without tenant context. "
                            + "campaignPublicId={}",
                    normalizedPublicId
            );

            throw new ForbiddenException(
                    "Current user's tenant is unavailable."
            );
        }

        return campaignRepository
                .findByPublicIdAndAgent_Tenant_IdAndIsDeleted(
                        normalizedPublicId,
                        tenantId,
                        NOT_DELETED
                )
                .orElseThrow(() -> {
                    log.warn(
                            "Campaign not found in current tenant. "
                                    + "campaignPublicId={}, tenantId={}",
                            normalizedPublicId,
                            tenantId
                    );

                    return new ResourceNotFoundException(
                            "Campaign not found."
                    );
                });
    }
}
package com.infinitio.aivoiceplatform.campaign.service.impl;

import com.infinitio.aivoiceplatform.agent.entity.Agent;
import com.infinitio.aivoiceplatform.agent.validator.AgentValidator;
import com.infinitio.aivoiceplatform.campaign.constant.CampaignConstants;
import com.infinitio.aivoiceplatform.campaign.dto.request.CreateCampaignRequest;
import com.infinitio.aivoiceplatform.campaign.dto.request.UpdateCampaignRequest;
import com.infinitio.aivoiceplatform.campaign.dto.response.CampaignResponse;
import com.infinitio.aivoiceplatform.campaign.entity.Campaign;
import com.infinitio.aivoiceplatform.campaign.mapper.CampaignMapper;
import com.infinitio.aivoiceplatform.campaign.repository.CampaignRepository;
import com.infinitio.aivoiceplatform.campaign.service.CampaignService;
import com.infinitio.aivoiceplatform.campaign.validator.CampaignValidator;
import com.infinitio.aivoiceplatform.common.dto.PageResponse;
import com.infinitio.aivoiceplatform.phonenumber.entity.PhoneNumber;
import com.infinitio.aivoiceplatform.phonenumber.validator.PhoneNumberValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.infinitio.aivoiceplatform.auth.service.CurrentUserService;
import com.infinitio.aivoiceplatform.campaigncontact.repository.CampaignContactRepository;

import java.util.List;

/**
 * Service implementation for Campaign.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CampaignServiceImpl
        implements CampaignService {

    private final CampaignRepository campaignRepository;

    private final CampaignMapper campaignMapper;

    private final CampaignValidator campaignValidator;

    private final AgentValidator agentValidator;

    private final PhoneNumberValidator phoneNumberValidator;

    private final CurrentUserService currentUserService;

    private final CampaignContactRepository
            campaignContactRepository;

    @Override
    public CampaignResponse create(
            CreateCampaignRequest request) {

        log.info(
                "Creating Campaign. Code : {}, Name : {}",
                request.getCampaignCode(),
                request.getCampaignName()
        );

        campaignValidator.validateForCreate(request);

        Agent agent =
                agentValidator.validateAndGet(
                        request.getAgentPublicId()
                );

        PhoneNumber phoneNumber =
                phoneNumberValidator.validateAndGet(
                        request.getPhoneNumberPublicId()
                );

        Campaign campaign =
                campaignMapper.toEntity(request);

        campaign.setIsActive(0);

        campaign.setCreatedBy(
                currentUserService.getCurrentUserId()
        );

        campaign.setAgent(agent);

        campaign.setPhoneNumber(phoneNumber);

        Campaign savedCampaign =
                campaignRepository.save(campaign);

        log.info(
                "Campaign created successfully. Public Id : {}",
                savedCampaign.getPublicId()
        );

        return buildCampaignResponse(
                savedCampaign,
                campaignMapper.toResponse(
                        savedCampaign
                )
        );
    }

    @Override
    public CampaignResponse update(
            UpdateCampaignRequest request) {

        log.info(
                "Updating Campaign. Public Id : {}",
                request.getPublicId()
        );

        campaignValidator.validateForUpdate(request);

        Campaign campaign =
                campaignValidator.validateAndGet(
                        request.getPublicId()
                );

        Agent agent =
                agentValidator.validateAndGet(
                        request.getAgentPublicId()
                );

        PhoneNumber phoneNumber =
                phoneNumberValidator.validateAndGet(
                        request.getPhoneNumberPublicId()
                );

        campaignMapper.updateEntity(
                request,
                campaign
        );

        campaign.setAgent(agent);

        campaign.setPhoneNumber(phoneNumber);

        Campaign updatedCampaign =
                campaignRepository.save(campaign);

        log.info(
                "Campaign updated successfully. Public Id : {}",
                updatedCampaign.getPublicId()
        );

        return campaignMapper.toResponse(
                updatedCampaign
        );
    }

    @Override
    @Transactional(readOnly = true)
    public CampaignResponse getByPublicId(
            String publicId) {

        log.info(
                "Fetching Campaign. Public Id : {}",
                publicId
        );

        Campaign campaign =
                campaignValidator.validateAndGet(
                        publicId
                );

        return buildCampaignResponse(
                campaign,
                campaignMapper.toResponse(
                        campaign
                )
        );
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<CampaignResponse> getAll() {

        log.info(
                "Fetching all Campaigns"
        );

        List<Campaign> campaigns =
                campaignRepository.findByIsDeleted(
                        0
                );

        List<CampaignResponse> content =
                campaigns.stream()
                        .map(
                                campaign ->
                                        buildCampaignResponse(
                                                campaign,
                                                campaignMapper.toResponse(
                                                        campaign
                                                )
                                        )
                        )
                        .toList();

        int totalElements =
                content.size();

        return PageResponse
                .<CampaignResponse>builder()
                .content(content)
                .pageNumber(0)
                .pageSize(totalElements)
                .totalPages(
                        totalElements == 0
                                ? 0
                                : 1
                )
                .totalElements(totalElements)
                .first(true)
                .last(true)
                .build();
    }

    @Override
    public void delete(String publicId) {

        log.info(
                "Deleting Campaign. Public Id : {}",
                publicId
        );

        Campaign campaign =
                campaignValidator.validateAndGet(
                        publicId
                );

        campaign.markAsDeleted(1L);

        campaignRepository.save(campaign);

        log.info(
                "Campaign deleted successfully. Public Id : {}",
                publicId
        );
    }

    @Override
    public void activate(String publicId) {

        log.info(
                "Activating Campaign. Public Id : {}",
                publicId
        );

        Campaign campaign =
                campaignValidator.validateAndGet(
                        publicId
                );

        campaign.activate(
                currentUserService.getCurrentUserId()
        );

        campaign.setStatus(
                CampaignConstants.STATUS_ACTIVE
        );

        campaignRepository.save(
                campaign
        );

        log.info(
                "Campaign activated successfully. Public Id : {}",
                publicId
        );
    }

    @Override
    public void deactivate(String publicId) {

        log.info(
                "Deactivating Campaign. Public Id : {}",
                publicId
        );

        Campaign campaign =
                campaignValidator.validateAndGet(
                        publicId
                );

        campaign.deactivate(
                currentUserService.getCurrentUserId()
        );

        campaign.setStatus(
                CampaignConstants.STATUS_INACTIVE
        );

        campaignRepository.save(
                campaign
        );

        log.info(
                "Campaign deactivated successfully. Public Id : {}",
                publicId
        );
    }

    @Override
    public void complete(String publicId) {

        log.info(
                "Completing Campaign from scheduler. Public Id : {}",
                publicId
        );

        Campaign campaign =
                campaignValidator.validateAndGet(
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

    /**
     * Adds the current contact count to a campaign response.
     *
     * @param campaign campaign entity
     * @param response campaign response
     * @return campaign response containing contact count
     */
    private CampaignResponse buildCampaignResponse(
            Campaign campaign,
            CampaignResponse response) {

        long contactCount =
                campaignContactRepository
                        .countByCampaignIdAndIsDeleted(
                                campaign.getId(),
                                0
                        );

        response.setContactCount(
                contactCount
        );

        return response;
    }
}
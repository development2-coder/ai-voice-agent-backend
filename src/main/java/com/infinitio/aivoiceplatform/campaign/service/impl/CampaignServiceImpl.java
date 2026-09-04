package com.infinitio.aivoiceplatform.campaign.service.impl;

import com.infinitio.aivoiceplatform.agent.entity.Agent;
import com.infinitio.aivoiceplatform.agent.validator.AgentValidator;
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

        return campaignMapper.toResponse(
                savedCampaign
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

        return campaignMapper.toResponse(
                campaign
        );
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<CampaignResponse> getAll(
            int page,
            int size) {

        log.info(
                "Fetching Campaigns. Page : {}, Size : {}",
                page,
                size
        );

        Page<Campaign> result =
                campaignRepository.findByIsDeleted(
                        0,
                        PageRequest.of(page, size)
                );

        return PageResponse
                .<CampaignResponse>builder()
                .content(
                        result.getContent()
                                .stream()
                                .map(
                                        campaignMapper::toResponse
                                )
                                .toList()
                )
                .pageNumber(result.getNumber())
                .pageSize(result.getSize())
                .totalPages(result.getTotalPages())
                .totalElements(result.getTotalElements())
                .first(result.isFirst())
                .last(result.isLast())
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

        campaign.activate(1L);

        campaignRepository.save(campaign);

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

        campaign.deactivate(1L);

        campaignRepository.save(campaign);

        log.info(
                "Campaign deactivated successfully. Public Id : {}",
                publicId
        );
    }
}
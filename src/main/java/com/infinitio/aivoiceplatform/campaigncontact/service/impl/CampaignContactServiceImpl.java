package com.infinitio.aivoiceplatform.campaigncontact.service.impl;

import com.infinitio.aivoiceplatform.campaign.entity.Campaign;
import com.infinitio.aivoiceplatform.campaign.validator.CampaignValidator;
import com.infinitio.aivoiceplatform.campaigncontact.constant.CampaignContactConstants;
import com.infinitio.aivoiceplatform.campaigncontact.dto.request.CreateCampaignContactRequest;
import com.infinitio.aivoiceplatform.campaigncontact.dto.request.UpdateCampaignContactRequest;
import com.infinitio.aivoiceplatform.campaigncontact.dto.response.CampaignContactExcelUploadResponse;
import com.infinitio.aivoiceplatform.campaigncontact.dto.response.CampaignContactResponse;
import com.infinitio.aivoiceplatform.campaigncontact.entity.CampaignContact;
import com.infinitio.aivoiceplatform.campaigncontact.mapper.CampaignContactMapper;
import com.infinitio.aivoiceplatform.campaigncontact.repository.CampaignContactRepository;
import com.infinitio.aivoiceplatform.campaigncontact.service.CampaignContactExcelService;
import com.infinitio.aivoiceplatform.campaigncontact.service.CampaignContactService;
import com.infinitio.aivoiceplatform.campaigncontact.validator.CampaignContactValidator;
import com.infinitio.aivoiceplatform.common.dto.PageResponse;
import com.infinitio.aivoiceplatform.auth.service.CurrentUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

/**
 * Service implementation for Campaign Contact.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CampaignContactServiceImpl
        implements CampaignContactService {

    private final CampaignContactRepository
            campaignContactRepository;

    private final CampaignContactMapper
            campaignContactMapper;

    private final CampaignContactValidator
            campaignContactValidator;

    private final CampaignValidator campaignValidator;

    private final CampaignContactExcelService
            campaignContactExcelService;

    private final CurrentUserService currentUserService;

    @Override
    public CampaignContactResponse create(
            CreateCampaignContactRequest request) {

        log.info(
                "Creating Campaign Contact. Campaign : {}",
                request != null
                        ? request.getCampaignPublicId()
                        : null
        );

        Campaign campaign =
                campaignValidator.validateAndGet(
                        request.getCampaignPublicId()
                );

        campaignContactValidator.validateForCreate(
                request,
                campaign.getId()
        );

        CampaignContact contact =
                campaignContactMapper.toEntity(
                        request
                );

        contact.setCreatedBy(
                currentUserService.getCurrentUserId()
        );

        contact.setCampaign(
                campaign
        );

        CampaignContact savedContact =
                campaignContactRepository.save(
                        contact
                );

        log.info(
                "Campaign Contact created successfully. "
                        + "Public Id : {}",
                savedContact.getPublicId()
        );

        return campaignContactMapper.toResponse(
                savedContact
        );
    }

    @Override
    public CampaignContactResponse update(
            UpdateCampaignContactRequest request) {

        log.info(
                "Updating Campaign Contact. Public Id : {}",
                request != null
                        ? request.getPublicId()
                        : null
        );

        Campaign campaign =
                campaignValidator.validateAndGet(
                        request.getCampaignPublicId()
                );

        campaignContactValidator.validateForUpdate(
                request,
                campaign.getId()
        );

        CampaignContact contact =
                campaignContactValidator.validateAndGet(
                        request.getPublicId()
                );

        campaignContactMapper.updateEntity(
                request,
                contact
        );

        contact.setCampaign(
                campaign
        );

        CampaignContact updatedContact =
                campaignContactRepository.save(
                        contact
                );

        log.info(
                "Campaign Contact updated successfully. "
                        + "Public Id : {}",
                updatedContact.getPublicId()
        );

        return campaignContactMapper.toResponse(
                updatedContact
        );
    }

    @Override
    @Transactional(readOnly = true)
    public CampaignContactResponse getByPublicId(
            String publicId) {

        log.info(
                "Fetching Campaign Contact. Public Id : {}",
                publicId
        );

        CampaignContact contact =
                campaignContactValidator.validateAndGet(
                        publicId
                );

        return campaignContactMapper.toResponse(
                contact
        );
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<CampaignContactResponse> getAll(
            int page,
            int size) {

        Page<CampaignContact> result =
                campaignContactRepository.findAll(
                        PageRequest.of(
                                page,
                                size
                        )
                );

        return buildPageResponse(
                result
        );
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<CampaignContactResponse> getByCampaign(
            String campaignPublicId,
            int page,
            int size) {

        log.info(
                "Fetching Campaign Contacts. Campaign : {}",
                campaignPublicId
        );

        Campaign campaign =
                campaignValidator.validateAndGet(
                        campaignPublicId
                );

        Page<CampaignContact> result =
                campaignContactRepository.findByCampaignId(
                        campaign.getId(),
                        PageRequest.of(
                                page,
                                size
                        )
                );

        return buildPageResponse(
                result
        );
    }

    @Override
    public CampaignContactExcelUploadResponse uploadExcel(
            String campaignPublicId,
            MultipartFile file) {

        return campaignContactExcelService.upload(
                campaignPublicId,
                file
        );
    }

    private PageResponse<CampaignContactResponse>
    buildPageResponse(
            Page<CampaignContact> result) {

        return PageResponse
                .<CampaignContactResponse>builder()
                .content(
                        result.getContent()
                                .stream()
                                .map(
                                        campaignContactMapper
                                                ::toResponse
                                )
                                .toList()
                )
                .pageNumber(
                        result.getNumber()
                )
                .pageSize(
                        result.getSize()
                )
                .totalPages(
                        result.getTotalPages()
                )
                .totalElements(
                        result.getTotalElements()
                )
                .first(
                        result.isFirst()
                )
                .last(
                        result.isLast()
                )
                .build();
    }

    @Override
    public void delete(
            String publicId) {

        CampaignContact contact =
                campaignContactValidator.validateAndGet(
                        publicId
                );

        contact.markAsDeleted(
                1L
        );

        campaignContactRepository.save(
                contact
        );
    }

    @Override
    public void activate(
            String publicId) {

        CampaignContact contact =
                campaignContactValidator.validateAndGet(
                        publicId
                );

        contact.activate(
                1L
        );

        campaignContactRepository.save(
                contact
        );
    }

    @Override
    public void deactivate(
            String publicId) {

        CampaignContact contact =
                campaignContactValidator.validateAndGet(
                        publicId
                );

        contact.deactivate(
                1L
        );

        campaignContactRepository.save(
                contact
        );
    }

    @Override
    public CampaignContactResponse
    getNextEligibleContact(
            String campaignPublicId) {

        Campaign campaign =
                campaignValidator.validateAndGet(
                        campaignPublicId
                );

        CampaignContact contact =
                campaignContactRepository
                        .findFirstByCampaignIdAndStatusAndIsDeletedAndIsActiveOrderByPriorityDescIdAsc(
                                campaign.getId(),
                                CampaignContactConstants
                                        .STATUS_PENDING,
                                0,
                                1
                        )
                        .orElse(null);

        if (contact == null) {

            return null;
        }

        return campaignContactMapper.toResponse(
                contact
        );
    }

    @Override
    public CampaignContactResponse
    updateDialingStatus(
            String publicId,
            String status) {

        CampaignContact contact =
                campaignContactValidator.validateAndGet(
                        publicId
                );

        contact.setStatus(
                status
        );

        CampaignContact updatedContact =
                campaignContactRepository.save(
                        contact
                );

        return campaignContactMapper.toResponse(
                updatedContact
        );
    }

    @Override
    public CampaignContactResponse markDialing(
            String publicId) {

        CampaignContact contact =
                campaignContactValidator.validateAndGet(
                        publicId
                );

        contact.setStatus(
                CampaignContactConstants
                        .STATUS_DIALING
        );

        contact.setAttemptCount(
                contact.getAttemptCount() == null
                        ? 1
                        : contact.getAttemptCount() + 1
        );

        contact.setLastAttemptAt(
                java.time.LocalDateTime.now()
        );

        CampaignContact updatedContact =
                campaignContactRepository.save(
                        contact
                );

        return campaignContactMapper.toResponse(
                updatedContact
        );
    }
}
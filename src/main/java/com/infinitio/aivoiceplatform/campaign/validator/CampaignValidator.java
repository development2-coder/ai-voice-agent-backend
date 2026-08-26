package com.infinitio.aivoiceplatform.campaign.validator;

import com.infinitio.aivoiceplatform.campaign.constant.CampaignMessages;
import com.infinitio.aivoiceplatform.campaign.dto.request.CreateCampaignRequest;
import com.infinitio.aivoiceplatform.campaign.dto.request.UpdateCampaignRequest;
import com.infinitio.aivoiceplatform.campaign.entity.Campaign;
import com.infinitio.aivoiceplatform.campaign.repository.CampaignRepository;
import com.infinitio.aivoiceplatform.exception.BadRequestException;
import com.infinitio.aivoiceplatform.exception.ConflictException;
import com.infinitio.aivoiceplatform.exception.ResourceNotFoundException;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;

/**
 * Validator for Campaign.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Component
@RequiredArgsConstructor
public class CampaignValidator {

    private static final Integer NOT_DELETED = 0;

    private final CampaignRepository
            campaignRepository;

    /**
     * Validates a campaign creation request.
     *
     * @param request campaign creation request
     */
    public void validateForCreate(
            CreateCampaignRequest request) {

        if (request == null) {

            throw new BadRequestException(
                    CampaignMessages.REQUEST_REQUIRED
            );
        }

        if (campaignRepository
                .existsByCampaignCodeAndIsDeleted(
                        request.getCampaignCode().trim(),
                        NOT_DELETED
                )) {

            throw new ConflictException(
                    CampaignMessages.CODE_ALREADY_EXISTS
            );
        }

        if (campaignRepository
                .existsByCampaignNameAndIsDeleted(
                        request.getCampaignName().trim(),
                        NOT_DELETED
                )) {

            throw new ConflictException(
                    CampaignMessages.NAME_ALREADY_EXISTS
            );
        }
    }

    /**
     * Validates a campaign update request.
     *
     * @param request campaign update request
     */
    public void validateForUpdate(
            UpdateCampaignRequest request) {

        if (request == null) {

            throw new BadRequestException(
                    CampaignMessages.UPDATE_REQUEST_REQUIRED
            );
        }

        Campaign existing =
                validateAndGet(
                        request.getPublicId()
                );

        if (!existing.getCampaignCode()
                .equals(
                        request.getCampaignCode()
                )
                && campaignRepository
                .existsByCampaignCodeAndIsDeletedAndPublicIdNot(
                        request.getCampaignCode().trim(),
                        NOT_DELETED,
                        request.getPublicId()
                )) {

            throw new ConflictException(
                    CampaignMessages.CODE_ALREADY_EXISTS
            );
        }

        if (!existing.getCampaignName()
                .equals(
                        request.getCampaignName()
                )
                && campaignRepository
                .existsByCampaignNameAndIsDeletedAndPublicIdNot(
                        request.getCampaignName().trim(),
                        NOT_DELETED,
                        request.getPublicId()
                )) {

            throw new ConflictException(
                    CampaignMessages.NAME_ALREADY_EXISTS
            );
        }
    }

    /**
     * Finds an active, non-deleted campaign.
     *
     * @param publicId campaign public identifier
     * @return campaign entity
     */
    public Campaign validateAndGet(
            String publicId) {

        if (publicId == null
                || publicId.isBlank()) {

            throw new BadRequestException(
                    CampaignMessages.PUBLIC_ID_REQUIRED
            );
        }

        return campaignRepository
                .findByPublicIdAndIsDeleted(
                        publicId,
                        NOT_DELETED
                )
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                CampaignMessages.NOT_FOUND
                        )
                );
    }
}
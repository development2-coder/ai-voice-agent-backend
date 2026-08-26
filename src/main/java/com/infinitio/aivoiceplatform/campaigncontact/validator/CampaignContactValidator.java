package com.infinitio.aivoiceplatform.campaigncontact.validator;

import com.infinitio.aivoiceplatform.campaigncontact.constant.CampaignContactMessages;
import com.infinitio.aivoiceplatform.campaigncontact.dto.request.CreateCampaignContactRequest;
import com.infinitio.aivoiceplatform.campaigncontact.dto.request.UpdateCampaignContactRequest;
import com.infinitio.aivoiceplatform.campaigncontact.entity.CampaignContact;
import com.infinitio.aivoiceplatform.campaigncontact.repository.CampaignContactRepository;
import com.infinitio.aivoiceplatform.exception.BadRequestException;
import com.infinitio.aivoiceplatform.exception.ConflictException;
import com.infinitio.aivoiceplatform.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

/**
 * Validator for Campaign Contact.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Component
@RequiredArgsConstructor
public class CampaignContactValidator {

    private final CampaignContactRepository
            campaignContactRepository;

    private final ObjectMapper objectMapper;

    /**
     * Validates campaign contact creation.
     *
     * @param request campaign contact creation request
     * @param campaignId campaign database identifier
     */
    public void validateForCreate(
            CreateCampaignContactRequest request,
            Long campaignId) {

        if (request == null) {

            throw new BadRequestException(
                    CampaignContactMessages.REQUEST_REQUIRED
            );
        }

        if (campaignId == null) {

            throw new BadRequestException(
                    CampaignContactMessages.CAMPAIGN_REQUIRED
            );
        }

        if (request.getPhoneNumber() == null
                || request.getPhoneNumber().isBlank()) {

            throw new BadRequestException(
                    CampaignContactMessages.PHONE_NUMBER_REQUIRED
            );
        }

        validateCustomData(
                request.getCustomData()
        );

        if (campaignContactRepository
                .existsByCampaignIdAndPhoneNumber(
                        campaignId,
                        request.getPhoneNumber()
                )) {

            throw new ConflictException(
                    CampaignContactMessages.PHONE_ALREADY_EXISTS
            );
        }
    }

    /**
     * Validates campaign contact update.
     *
     * @param request campaign contact update request
     * @param campaignId campaign database identifier
     */
    public void validateForUpdate(
            UpdateCampaignContactRequest request,
            Long campaignId) {

        if (request == null) {

            throw new BadRequestException(
                    CampaignContactMessages
                            .UPDATE_REQUEST_REQUIRED
            );
        }

        CampaignContact existing =
                validateAndGet(
                        request.getPublicId()
                );

        if (existing.getCampaign() == null
                || !existing
                .getCampaign()
                .getId()
                .equals(campaignId)) {

            throw new ResourceNotFoundException(
                    CampaignContactMessages.NOT_FOUND
            );
        }

        if (request.getPhoneNumber() == null
                || request.getPhoneNumber().isBlank()) {

            throw new BadRequestException(
                    CampaignContactMessages.PHONE_NUMBER_REQUIRED
            );
        }

        validateCustomData(
                request.getCustomData()
        );

        if (!existing
                .getPhoneNumber()
                .equals(
                        request.getPhoneNumber()
                )
                && campaignContactRepository
                .existsByCampaignIdAndPhoneNumber(
                        campaignId,
                        request.getPhoneNumber()
                )) {

            throw new ConflictException(
                    CampaignContactMessages.PHONE_ALREADY_EXISTS
            );
        }
    }

    /**
     * Validates custom campaign data.
     *
     * @param customData JSON string
     */
    private void validateCustomData(
            String customData) {

        if (customData == null
                || customData.isBlank()) {

            return;
        }

        try {

            JsonNode node =
                    objectMapper.readTree(
                            customData
                    );

            if (node == null
                    || !node.isObject()) {

                throw new BadRequestException(
                        CampaignContactMessages
                                .CUSTOM_DATA_INVALID
                );
            }

        } catch (BadRequestException exception) {

            throw exception;

        } catch (Exception exception) {

            throw new BadRequestException(
                    CampaignContactMessages
                            .CUSTOM_DATA_INVALID
            );
        }
    }

    /**
     * Finds a campaign contact by public identifier.
     *
     * @param publicId campaign contact public identifier
     * @return campaign contact
     */
    public CampaignContact validateAndGet(
            String publicId) {

        if (publicId == null
                || publicId.isBlank()) {

            throw new BadRequestException(
                    CampaignContactMessages
                            .PUBLIC_ID_REQUIRED
            );
        }

        return campaignContactRepository
                .findByPublicId(
                        publicId
                )
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                CampaignContactMessages.NOT_FOUND
                        )
                );
    }
}
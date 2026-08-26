package com.infinitio.aivoiceplatform.aidialer.service.impl;

import com.infinitio.aivoiceplatform.aidialer.constant.DialerMessages;
import com.infinitio.aivoiceplatform.aidialer.entity.AiDialer;
import com.infinitio.aivoiceplatform.aidialer.service.DialerCallValidationService;
import com.infinitio.aivoiceplatform.campaigncontact.entity.CampaignContact;
import com.infinitio.aivoiceplatform.exception.BadRequestException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class DialerCallValidationServiceImpl
        implements DialerCallValidationService {

    @Override
    public void validatePublicIds(
            String dialerPublicId,
            String campaignContactPublicId) {

        if (dialerPublicId == null
                || dialerPublicId.isBlank()) {

            throw new BadRequestException(
                    DialerMessages.DIALER_PUBLIC_ID_REQUIRED
            );
        }

        if (campaignContactPublicId == null
                || campaignContactPublicId.isBlank()) {

            throw new BadRequestException(
                    DialerMessages
                            .CAMPAIGN_CONTACT_PUBLIC_ID_REQUIRED
            );
        }
    }

    @Override
    public void validateCampaignRelationship(
            AiDialer dialer,
            CampaignContact campaignContact) {

        if (dialer.getCampaign() == null) {

            throw new BadRequestException(
                    DialerMessages
                            .DIALER_CAMPAIGN_NOT_CONFIGURED
            );
        }

        if (campaignContact.getCampaign() == null) {

            throw new BadRequestException(
                    DialerMessages
                            .CONTACT_CAMPAIGN_NOT_CONFIGURED
            );
        }

        if (!dialer.getCampaign()
                .getId()
                .equals(
                        campaignContact
                                .getCampaign()
                                .getId()
                )) {

            throw new BadRequestException(
                    DialerMessages
                            .CONTACT_NOT_IN_DIALER_CAMPAIGN
            );
        }
    }

    @Override
    public void validatePhoneNumber(
            CampaignContact campaignContact) {

        if (campaignContact.getPhoneNumber() == null
                || campaignContact
                .getPhoneNumber()
                .isBlank()) {

            throw new BadRequestException(
                    DialerMessages.PHONE_NUMBER_REQUIRED
            );
        }
    }
}
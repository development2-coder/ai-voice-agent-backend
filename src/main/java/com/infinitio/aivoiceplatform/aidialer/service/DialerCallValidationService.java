package com.infinitio.aivoiceplatform.aidialer.service;

import com.infinitio.aivoiceplatform.aidialer.entity.AiDialer;
import com.infinitio.aivoiceplatform.campaigncontact.entity.CampaignContact;

public interface DialerCallValidationService {

    void validatePublicIds(
            String dialerPublicId,
            String campaignContactPublicId
    );

    void validateCampaignRelationship(
            AiDialer dialer,
            CampaignContact campaignContact
    );

    void validatePhoneNumber(
            CampaignContact campaignContact
    );
}
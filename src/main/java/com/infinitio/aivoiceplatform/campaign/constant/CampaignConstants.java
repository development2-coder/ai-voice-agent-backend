package com.infinitio.aivoiceplatform.campaign.constant;

/**
 * Constants used by Campaign module.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
public final class CampaignConstants {

    private CampaignConstants() {
    }

    public static final int CAMPAIGN_CODE_MAX_LENGTH = 50;

    public static final int CAMPAIGN_NAME_MAX_LENGTH = 150;

    public static final int CAMPAIGN_TYPE_MAX_LENGTH = 30;

    public static final int STATUS_MAX_LENGTH = 30;

    public static final int DESCRIPTION_MAX_LENGTH = 500;

    public static final String STATUS_DRAFT = "DRAFT";

    public static final String STATUS_ACTIVE = "ACTIVE";

    public static final String STATUS_INACTIVE = "INACTIVE";

    public static final int CUSTOM_DATA_MAX_LENGTH = 10000;
}
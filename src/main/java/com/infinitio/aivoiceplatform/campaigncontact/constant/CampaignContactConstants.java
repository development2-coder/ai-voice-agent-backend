package com.infinitio.aivoiceplatform.campaigncontact.constant;

/**
 * Constants used by Campaign Contact module.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
public final class CampaignContactConstants {

    private CampaignContactConstants() {
    }

    public static final int NAME_MAX_LENGTH = 150;

    public static final int PHONE_NUMBER_MAX_LENGTH = 30;

    public static final int EXTERNAL_REFERENCE_MAX_LENGTH = 150;

    public static final int STATUS_MAX_LENGTH = 30;

    public static final int DESCRIPTION_MAX_LENGTH = 500;

    public static final String STATUS_PENDING =
            "PENDING";

    public static final String STATUS_DIALING =
            "DIALING";

    public static final String STATUS_NO_ANSWER =
            "NO_ANSWER";

    public static final String STATUS_BUSY =
            "BUSY";

    public static final String STATUS_FAILED =
            "FAILED";

    /*
     * Standard Excel column names.
     *
     * Any column other than these standard columns
     * will be stored inside customData.
     */
    public static final String EXCEL_PHONE_NUMBER_HEADER =
            "phone_number";

    public static final String EXCEL_NAME_HEADER =
            "name";

    public static final String EXCEL_EXTERNAL_REFERENCE_HEADER =
            "external_reference";

    public static final String EXCEL_PRIORITY_HEADER =
            "priority";

    public static final String EXCEL_DESCRIPTION_HEADER =
            "description";
}
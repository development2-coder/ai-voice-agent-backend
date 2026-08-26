package com.infinitio.aivoiceplatform.campaigncontact.constant;

/**
 * Messages used by Campaign Contact module.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
public final class CampaignContactMessages {

    private CampaignContactMessages() {
    }

    public static final String CREATED =
            "Campaign contact created successfully.";

    public static final String UPDATED =
            "Campaign contact updated successfully.";

    public static final String DELETED =
            "Campaign contact deleted successfully.";

    public static final String ACTIVATED =
            "Campaign contact activated successfully.";

    public static final String DEACTIVATED =
            "Campaign contact deactivated successfully.";

    public static final String NOT_FOUND =
            "Campaign contact not found.";

    public static final String PHONE_ALREADY_EXISTS =
            "Phone number already exists in this campaign.";

    public static final String REQUEST_REQUIRED =
            "Campaign contact request is required.";

    public static final String PRIORITY_INVALID =
            "Priority must be a valid number.";

    public static final String UPDATE_REQUEST_REQUIRED =
            "Campaign contact update request is required.";

    public static final String PUBLIC_ID_REQUIRED =
            "Campaign contact public ID is required.";

    public static final String CAMPAIGN_REQUIRED =
            "Campaign is required.";

    public static final String PHONE_NUMBER_REQUIRED =
            "Phone number is required.";

    public static final String CUSTOM_DATA_INVALID =
            "Campaign contact custom data must be valid JSON.";

    public static final String EXCEL_FILE_REQUIRED =
            "Excel file is required.";

    public static final String EXCEL_FILE_EMPTY =
            "Excel file is empty.";

    public static final String EXCEL_FILE_INVALID =
            "Invalid Excel file.";

    public static final String EXCEL_PHONE_COLUMN_REQUIRED =
            "Excel file must contain a phone_number column.";

    public static final String EXCEL_DUPLICATE_HEADER =
            "Excel file contains duplicate column headers.";

    public static final String EXCEL_UPLOAD_COMPLETED =
            "Campaign contacts Excel upload completed.";

    public static final String EXCEL_FILE_TYPE_NOT_SUPPORTED =
            "Only XLS and XLSX files are supported.";
}
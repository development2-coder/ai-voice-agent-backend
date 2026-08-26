package com.infinitio.aivoiceplatform.aidialer.constant;

/**
 * Contains messages used by the AI Dialer module.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
public final class DialerMessages {

    private DialerMessages() {
    }

    public static final String DIALER_REQUEST_REQUIRED =
            "AI Dialer request is required.";

    public static final String DIALER_UPDATE_REQUEST_REQUIRED =
            "AI Dialer update request is required.";

    public static final String DIALER_PUBLIC_ID_REQUIRED =
            "AI Dialer public ID is required.";

    public static final String DIALER_NOT_FOUND =
            "AI Dialer not found.";

    public static final String DIALER_NAME_REQUIRED =
            "Dialer name is required.";

    public static final String CAMPAIGN_PUBLIC_ID_REQUIRED =
            "Campaign public ID is required.";

    public static final String AGENT_PUBLIC_ID_REQUIRED =
            "Agent public ID is required.";

    public static final String FLOW_PUBLIC_ID_REQUIRED =
            "Flow public ID is required.";

    public static final String SCHEDULE_END_INVALID =
            "Scheduled end time must be after scheduled start time.";

    public static final String DIALER_ALREADY_RUNNING =
            "Dialer is already running.";

    public static final String DIALER_ALREADY_STOPPED =
            "Dialer is already stopped.";

    public static final String COMPLETED_DIALER_CANNOT_START =
            "Completed dialer cannot be started again.";

    public static final String COMPLETED_DIALER_CANNOT_STOP =
            "Completed dialer cannot be stopped.";

    public static final String DIALER_MUST_BE_RUNNING_TO_PAUSE =
            "Only a running dialer can be paused.";

    public static final String DIALER_MUST_BE_PAUSED_TO_RESUME =
            "Only a paused dialer can be resumed.";

    public static final String DIALER_CREATED =
            "AI Dialer created successfully.";

    public static final String DIALER_UPDATED =
            "AI Dialer updated successfully.";

    public static final String DIALER_DELETED =
            "AI Dialer deleted successfully.";

    public static final String DIALER_STARTED =
            "AI Dialer started successfully.";

    public static final String DIALER_PAUSED =
            "AI Dialer paused successfully.";

    public static final String DIALER_RESUMED =
            "AI Dialer resumed successfully.";

    public static final String DIALER_STOPPED =
            "AI Dialer stopped successfully.";

    public static final String DIALER_CALL_PUBLIC_ID_REQUIRED =
            "Dialer call public ID is required.";

    public static final String CAMPAIGN_CONTACT_PUBLIC_ID_REQUIRED =
            "Campaign contact public ID is required.";

    public static final String DIALER_CALL_NOT_FOUND =
            "Dialer call not found.";

    public static final String DIALER_CALL_STATUS_REQUIRED =
            "Call status is required.";

    public static final String PHONE_NUMBER_REQUIRED =
            "Campaign contact phone number is required.";

    public static final String DIALER_CAMPAIGN_NOT_CONFIGURED =
            "Dialer campaign is not configured.";

    public static final String CONTACT_CAMPAIGN_NOT_CONFIGURED =
            "Campaign contact campaign is not configured.";

    public static final String CONTACT_NOT_IN_DIALER_CAMPAIGN =
            "Campaign contact does not belong to the dialer's campaign.";

    public static final String MAX_ATTEMPTS_REACHED =
            "Maximum call attempts reached for this campaign contact.";

    public static final String EXOTEL_CALL_ID_REQUIRED =
            "Exotel call ID is required.";

    public static final String EXOTEL_CALL_ID_ALREADY_EXISTS =
            "Exotel call ID is already associated with another call.";

    public static final String FLOW_EXECUTION_PUBLIC_ID_REQUIRED =
            "Flow execution public ID is required.";

    public static final String DURATION_NEGATIVE =
            "Duration cannot be negative.";

    public static final String INITIATION_ONLY_QUEUED =
            "Only QUEUED calls can be initiated.";

    public static final String DIALER_REQUIRED_FOR_INITIATION =
            "Dialer is required to initiate the call.";

    public static final String CONTACT_REQUIRED_FOR_INITIATION =
            "Campaign contact is required to initiate the call.";

    public static final String INITIATION_FAILED =
            "Dialer call initiation failed.";

    public static final String
            CAMPAIGN_PHONE_NUMBER_NOT_CONFIGURED =
            "Campaign phone number is not configured.";

}
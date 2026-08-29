package com.infinitio.aivoiceplatform.telephony.constants;

/**
 * Constants used by the telephony module.
 *
 * <p>
 * This class contains provider-independent normalized call
 * lifecycle event names and common telephony values.
 * Provider-specific implementations must map their own
 * provider events to these normalized values.
 * </p>
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
public final class TelephonyConstants {

    /**
     * Private constructor to prevent instantiation.
     */
    private TelephonyConstants() {
        throw new IllegalStateException(
                "Utility class must not be instantiated."
        );
    }

    /*
     * ---------------------------------------------------------
     * PROVIDER
     * ---------------------------------------------------------
     */

    /**
     * Exotel provider code.
     */
    public static final String PROVIDER_EXOTEL =
            "EXOTEL";

    /*
     * ---------------------------------------------------------
     * CALL DIRECTION
     * ---------------------------------------------------------
     */

    /**
     * Outbound call direction.
     */
    public static final String DIRECTION_OUTBOUND =
            "OUTBOUND";

    /**
     * Inbound call direction.
     */
    public static final String DIRECTION_INBOUND =
            "INBOUND";

    /*
     * ---------------------------------------------------------
     * CALL LIFECYCLE EVENTS
     * ---------------------------------------------------------
     */

    /**
     * Call initiated event.
     */
    public static final String EVENT_CALL_INITIATED =
            "INITIATED";

    /**
     * Call ringing event.
     */
    public static final String EVENT_CALL_RINGING =
            "RINGING";

    /**
     * Call answered event.
     */
    public static final String EVENT_CALL_ANSWERED =
            "ANSWERED";

    /**
     * Call completed event.
     */
    public static final String EVENT_CALL_COMPLETED =
            "COMPLETED";

    /**
     * Call ended event.
     */
    public static final String EVENT_CALL_ENDED =
            "ENDED";

    /**
     * Call failed event.
     */
    public static final String EVENT_CALL_FAILED =
            "FAILED";

    /**
     * Call busy event.
     */
    public static final String EVENT_CALL_BUSY =
            "BUSY";

    /**
     * Call no-answer event.
     */
    public static final String EVENT_CALL_NO_ANSWER =
            "NO_ANSWER";

    /**
     * Call cancelled event.
     */
    public static final String EVENT_CALL_CANCELLED =
            "CANCELLED";

    /**
     * Call rejected event.
     */
    public static final String EVENT_CALL_REJECTED =
            "REJECTED";

    /*
     * ---------------------------------------------------------
     * EXOTEL CONFIGURATION
     * ---------------------------------------------------------
     */

    /**
     * Default Exotel provider name.
     */
    public static final String DEFAULT_PROVIDER =
            PROVIDER_EXOTEL;

    /**
     * Exotel API identifier property.
     */
    public static final String EXOTEL_API_KEY =
            "exotel.api-key";

    /**
     * Exotel API token property.
     */
    public static final String EXOTEL_API_TOKEN =
            "exotel.api-token";

    /**
     * Exotel account identifier property.
     */
    public static final String EXOTEL_ACCOUNT_SID =
            "exotel.account-sid";

    /**
     * Exotel base URL property.
     */
    public static final String EXOTEL_BASE_URL =
            "exotel.base-url";

    /**
     * Exotel caller number property.
     */
    public static final String EXOTEL_CALLER_ID =
            "exotel.caller-id";
}
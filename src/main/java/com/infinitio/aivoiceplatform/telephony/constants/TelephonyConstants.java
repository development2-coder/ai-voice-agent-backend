package com.infinitio.aivoiceplatform.telephony.constant;

/**
 * Defines constants used by the telephony module.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
public final class TelephonyConstants {

    private TelephonyConstants() {
    }

    /*
     * ---------------------------------------------------------
     * TELEPHONY PROVIDERS
     * ---------------------------------------------------------
     */

    public static final String PROVIDER_EXOTEL =
            "EXOTEL";

    public static final String PROVIDER_OZONETEL =
            "OZONETEL";

    public static final String PROVIDER_TWILIO =
            "TWILIO";

    /*
     * ---------------------------------------------------------
     * NORMALIZED CALL EVENTS
     * ---------------------------------------------------------
     *
     * These events are provider-independent.
     *
     * Exotel/Twilio/Ozonetel-specific statuses must be
     * converted into these events before they reach the
     * Call, CallSession and AI Dialer modules.
     */

    public static final String EVENT_CALL_INITIATED =
            "call.initiated";

    public static final String EVENT_CALL_RINGING =
            "call.ringing";

    public static final String EVENT_CALL_ANSWERED =
            "call.answered";

    public static final String EVENT_CALL_IN_PROGRESS =
            "call.in-progress";

    public static final String EVENT_CALL_COMPLETED =
            "call.completed";

    public static final String EVENT_CALL_FAILED =
            "call.failed";

    public static final String EVENT_CALL_BUSY =
            "call.busy";

    public static final String EVENT_CALL_NO_ANSWER =
            "call.no-answer";

    public static final String EVENT_CALL_CANCELLED =
            "call.cancelled";

    public static final String EVENT_CALL_REJECTED =
            "call.rejected";

    /*
     * ---------------------------------------------------------
     * EXISTING GENERIC EVENTS
     * ---------------------------------------------------------
     *
     * CALL_ENDED is retained for backward compatibility with
     * existing telephony consumers.
     */

    public static final String EVENT_CALL_ENDED =
            "call.ended";

    public static final String EVENT_DTMF_RECEIVED =
            "dtmf.received";
}
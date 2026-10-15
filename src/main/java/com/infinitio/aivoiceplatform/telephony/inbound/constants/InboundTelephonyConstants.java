package com.infinitio.aivoiceplatform.telephony.inbound.constants;

/**
 * Constants used by the inbound telephony integration.
 *
 * <p>
 * These constants are isolated from the existing outbound
 * Exotel integration so that inbound call handling does not
 * modify or introduce dependencies into the existing outbound
 * call flow.
 * </p>
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
public final class InboundTelephonyConstants {

    /**
     * Private constructor to prevent instantiation.
     */
    private InboundTelephonyConstants() {

        throw new IllegalStateException(
                "Utility class must not be instantiated."
        );
    }

    /*
     * ---------------------------------------------------------
     * WEBSOCKET ENDPOINT
     * ---------------------------------------------------------
     */

    /**
     * WebSocket endpoint used by Exotel for inbound calls.
     */
    public static final String INBOUND_WEBSOCKET_ENDPOINT =
            "/ws/telephony/exotel/inbound";

    /*
     * ---------------------------------------------------------
     * PROVIDER
     * ---------------------------------------------------------
     */

    /**
     * Telephony provider name.
     */
    public static final String PROVIDER_EXOTEL =
            "EXOTEL";

    /*
     * ---------------------------------------------------------
     * CALL DIRECTION
     * ---------------------------------------------------------
     */

    /**
     * Inbound call direction.
     */
    public static final String DIRECTION_INBOUND =
            "INBOUND";

    /**
     * Phone number direction supporting both inbound
     * and outbound calls.
     */
    public static final String DIRECTION_BOTH =
            "BOTH";

    /*
     * ---------------------------------------------------------
     * CALL STATUS
     * ---------------------------------------------------------
     */

    /**
     * Initial status assigned to an inbound call.
     */
    public static final String CALL_STATUS_INITIATED =
            "INITIATED";

    /**
     * Status assigned when the inbound call is connected.
     */
    public static final String CALL_STATUS_IN_PROGRESS =
            "IN_PROGRESS";

    /**
     * Status assigned when the inbound call is completed.
     */
    public static final String CALL_STATUS_COMPLETED =
            "COMPLETED";

    /**
     * Status assigned when the inbound call fails.
     */
    public static final String CALL_STATUS_FAILED =
            "FAILED";

    /*
     * ---------------------------------------------------------
     * FLOW
     * ---------------------------------------------------------
     */

    /**
     * Active flow status.
     */
    public static final String FLOW_STATUS_ACTIVE =
            "ACTIVE";

    /*
     * ---------------------------------------------------------
     * WEBSOCKET SESSION ATTRIBUTES
     * ---------------------------------------------------------
     */

    /**
     * Application Call entity stored in the inbound
     * WebSocket session.
     */
    public static final String ATTRIBUTE_CALL =
            "inbound.call";

    /**
     * Application Call public identifier stored in
     * the inbound WebSocket session.
     */
    public static final String SESSION_CALL_ID =
            "inbound.callId";

    /**
     * Exotel provider call identifier stored in
     * the inbound WebSocket session.
     */
    public static final String SESSION_PROVIDER_CALL_ID =
            "inbound.providerCallId";

    /**
     * Exotel stream identifier stored in the
     * inbound WebSocket session.
     */
    public static final String SESSION_STREAM_ID =
            "inbound.streamId";

    /**
     * Caller phone number stored in the inbound
     * WebSocket session.
     */
    public static final String SESSION_CALLER_NUMBER =
            "inbound.callerNumber";

    /**
     * Called phone number stored in the inbound
     * WebSocket session.
     */
    public static final String SESSION_CALLED_NUMBER =
            "inbound.calledNumber";

    /**
     * Audio sample rate stored in the inbound
     * WebSocket session.
     */
    public static final String SESSION_SAMPLE_RATE =
            "inbound.sampleRate";

    /**
     * Audio encoding stored in the inbound
     * WebSocket session.
     */
    public static final String SESSION_AUDIO_ENCODING =
            "inbound.audioEncoding";

    /**
     * Audio channel count stored in the inbound
     * WebSocket session.
     */
    public static final String SESSION_CHANNELS =
            "inbound.channels";

    /*
     * ---------------------------------------------------------
     * AUDIO
     * ---------------------------------------------------------
     */

    /**
     * Default telephony sample rate.
     */
    public static final int DEFAULT_SAMPLE_RATE =
            8000;

    /**
     * Default telephony channel count.
     */
    public static final int DEFAULT_CHANNELS =
            1;

    /**
     * Default audio sample size.
     */
    public static final int DEFAULT_SAMPLE_SIZE_BITS =
            16;

    /*
     * ---------------------------------------------------------
     * VALIDATION
     * ---------------------------------------------------------
     */

    /**
     * Maximum supported phone number length.
     */
    public static final int MAX_PHONE_NUMBER_LENGTH =
            30;

    /**
     * Maximum provider call identifier length.
     */
    public static final int MAX_PROVIDER_CALL_ID_LENGTH =
            150;

    /**
     * Description stored against an inbound AI Voice Agent call.
     */
    public static final String CALL_DESCRIPTION =
            "Inbound AI Voice Agent call.";
}
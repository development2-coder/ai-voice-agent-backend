package com.infinitio.aivoiceplatform.telephony.constants;

/**
 * Constants used by the Exotel bidirectional WebSocket protocol.
 *
 * <p>
 * These constants represent provider-specific WebSocket event names
 * and JSON field names exchanged between Exotel and the AI voice
 * platform. They must not be used as normalized telephony lifecycle
 * values outside the Exotel integration layer.
 * </p>
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
public final class ExotelWebSocketConstants {

    /**
     * Private constructor to prevent instantiation.
     */
    private ExotelWebSocketConstants() {
        throw new IllegalStateException(
                "Utility class must not be instantiated."
        );
    }

    /*
     * ---------------------------------------------------------
     * EVENT NAMES
     * ---------------------------------------------------------
     */

    /**
     * WebSocket connection event.
     */
    public static final String EVENT_CONNECTED =
            "connected";

    /**
     * Stream start event.
     */
    public static final String EVENT_START =
            "start";

    /**
     * Audio media event.
     */
    public static final String EVENT_MEDIA =
            "media";

    /**
     * DTMF event.
     */
    public static final String EVENT_DTMF =
            "dtmf";

    /**
     * Stream stop event.
     */
    public static final String EVENT_STOP =
            "stop";

    /**
     * Media completion marker event.
     */
    public static final String EVENT_MARK =
            "mark";

    /**
     * Clears media that has been sent but not yet played.
     */
    public static final String EVENT_CLEAR =
            "clear";

    /*
     * ---------------------------------------------------------
     * COMMON JSON FIELDS
     * ---------------------------------------------------------
     */

    /**
     * Event JSON field.
     */
    public static final String FIELD_EVENT =
            "event";

    /**
     * Sequence number JSON field.
     */
    public static final String FIELD_SEQUENCE_NUMBER =
            "sequence_number";

    /**
     * Stream identifier JSON field.
     */
    public static final String FIELD_STREAM_SID =
            "stream_sid";

    /*
     * ---------------------------------------------------------
     * START EVENT FIELDS
     * ---------------------------------------------------------
     */

    /**
     * Start metadata JSON field.
     */
    public static final String FIELD_START =
            "start";

    /**
     * Call identifier JSON field.
     */
    public static final String FIELD_CALL_SID =
            "call_sid";

    /**
     * Account identifier JSON field.
     */
    public static final String FIELD_ACCOUNT_SID =
            "account_sid";

    /**
     * Caller number JSON field.
     */
    public static final String FIELD_FROM =
            "from";

    /**
     * Destination number JSON field.
     */
    public static final String FIELD_TO =
            "to";

    /**
     * Custom parameters JSON field.
     */
    public static final String FIELD_CUSTOM_PARAMETERS =
            "custom_parameters";

    /**
     * Media format JSON field.
     */
    public static final String FIELD_MEDIA_FORMAT =
            "media_format";

    /**
     * Audio encoding JSON field.
     */
    public static final String FIELD_ENCODING =
            "encoding";

    /**
     * Audio sample rate JSON field.
     */
    public static final String FIELD_SAMPLE_RATE =
            "sample_rate";

    /**
     * Audio bitrate JSON field.
     */
    public static final String FIELD_BIT_RATE =
            "bit_rate";

    /*
     * ---------------------------------------------------------
     * MEDIA EVENT FIELDS
     * ---------------------------------------------------------
     */

    /**
     * Media JSON field.
     */
    public static final String FIELD_MEDIA =
            "media";

    /**
     * Media chunk JSON field.
     */
    public static final String FIELD_CHUNK =
            "chunk";

    /**
     * Media timestamp JSON field.
     */
    public static final String FIELD_TIMESTAMP =
            "timestamp";

    /**
     * Base64 audio payload JSON field.
     */
    public static final String FIELD_PAYLOAD =
            "payload";

    /*
     * ---------------------------------------------------------
     * DTMF EVENT FIELDS
     * ---------------------------------------------------------
     */

    /**
     * DTMF JSON field.
     */
    public static final String FIELD_DTMF =
            "dtmf";

    /**
     * DTMF duration JSON field.
     */
    public static final String FIELD_DURATION =
            "duration";

    /**
     * DTMF digit JSON field.
     */
    public static final String FIELD_DIGIT =
            "digit";

    /*
     * ---------------------------------------------------------
     * STOP EVENT FIELDS
     * ---------------------------------------------------------
     */

    /**
     * Stop metadata JSON field.
     */
    public static final String FIELD_STOP =
            "stop";

    /**
     * Stop reason JSON field.
     */
    public static final String FIELD_REASON =
            "reason";

    /*
     * ---------------------------------------------------------
     * MARK EVENT FIELDS
     * ---------------------------------------------------------
     */

    /**
     * Mark JSON field.
     */
    public static final String FIELD_MARK =
            "mark";

    /**
     * Mark name JSON field.
     */
    public static final String FIELD_NAME =
            "name";

    /*
     * ---------------------------------------------------------
     * AUDIO CONFIGURATION
     * ---------------------------------------------------------
     */

    /**
     * Exotel raw signed linear PCM encoding.
     */
    public static final String AUDIO_ENCODING_SL_IN =
            "slin";

    /**
     * Default Exotel telephony sample rate.
     */
    public static final int DEFAULT_SAMPLE_RATE =
            8000;

    /**
     * Default telephony audio channel count.
     */
    public static final int DEFAULT_CHANNELS =
            1;

    /**
     * Audio sample size in bits.
     */
    public static final int AUDIO_SAMPLE_SIZE_BITS =
            16;
}
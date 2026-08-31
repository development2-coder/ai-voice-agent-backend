package com.infinitio.aivoiceplatform.voicegateway.constant;

/**
 * Constants used by the Voice Gateway module.
 *
 * <p>
 * The Voice Gateway is responsible for handling real-time
 * bidirectional media communication between the telephony
 * provider and the Conversation Orchestrator.
 * </p>
 *
 * <p>
 * Provider-specific WebSocket event names are maintained here
 * so that the WebSocket handler does not contain hard-coded
 * protocol strings.
 * </p>
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
public final class VoiceGatewayConstants {

    /**
     * Private constructor.
     */
    private VoiceGatewayConstants() {

        throw new IllegalStateException(
                "Utility class must not be instantiated."
        );
    }

    // =========================================================
    // PROVIDER
    // =========================================================

    /**
     * Exotel provider code.
     */
    public static final String PROVIDER_EXOTEL =
            "EXOTEL";

    // =========================================================
    // WEBSOCKET EVENTS - EXOTEL -> PLATFORM
    // =========================================================

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
     * Mark event.
     */
    public static final String EVENT_MARK =
            "mark";

    // =========================================================
    // WEBSOCKET EVENTS - PLATFORM -> EXOTEL
    // =========================================================

    /**
     * Audio media event sent back to Exotel.
     */
    public static final String OUTBOUND_EVENT_MEDIA =
            "media";

    /**
     * Mark event sent back to Exotel.
     */
    public static final String OUTBOUND_EVENT_MARK =
            "mark";

    /**
     * Clear event sent to Exotel.
     */
    public static final String OUTBOUND_EVENT_CLEAR =
            "clear";

    // =========================================================
    // JSON FIELD NAMES
    // =========================================================

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
     * Stream SID JSON field.
     */
    public static final String FIELD_STREAM_SID =
            "stream_sid";

    /**
     * Start JSON field.
     */
    public static final String FIELD_START =
            "start";

    /**
     * Media JSON field.
     */
    public static final String FIELD_MEDIA =
            "media";

    /**
     * DTMF JSON field.
     */
    public static final String FIELD_DTMF =
            "dtmf";

    /**
     * Stop JSON field.
     */
    public static final String FIELD_STOP =
            "stop";

    /**
     * Mark JSON field.
     */
    public static final String FIELD_MARK =
            "mark";

    /**
     * Payload JSON field.
     */
    public static final String FIELD_PAYLOAD =
            "payload";

    /**
     * Chunk JSON field.
     */
    public static final String FIELD_CHUNK =
            "chunk";

    /**
     * Timestamp JSON field.
     */
    public static final String FIELD_TIMESTAMP =
            "timestamp";

    /**
     * Digit JSON field.
     */
    public static final String FIELD_DIGIT =
            "digit";

    /**
     * Duration JSON field.
     */
    public static final String FIELD_DURATION =
            "duration";

    /**
     * Call SID JSON field.
     */
    public static final String FIELD_CALL_SID =
            "call_sid";

    /**
     * Account SID JSON field.
     */
    public static final String FIELD_ACCOUNT_SID =
            "account_sid";

    /**
     * Stop reason JSON field.
     */
    public static final String FIELD_REASON =
            "reason";

    /**
     * Mark name JSON field.
     */
    public static final String FIELD_MARK_NAME =
            "name";

    // =========================================================
    // STREAM PARAMETERS
    // =========================================================

    /**
     * Exotel audio encoding.
     */
    public static final String AUDIO_ENCODING =
            "slin";

    /**
     * Audio sample rate expected from Exotel.
     */
    public static final int AUDIO_SAMPLE_RATE =
            8000;

    /**
     * Audio channel count.
     */
    public static final int AUDIO_CHANNELS =
            1;

    /**
     * PCM sample size.
     */
    public static final int AUDIO_SAMPLE_SIZE_BITS =
            16;

    /**
     * PCM byte order.
     */
    public static final String AUDIO_BYTE_ORDER =
            "LITTLE_ENDIAN";

    // =========================================================
    // SESSION
    // =========================================================

    /**
     * Maximum allowed number of active gateway sessions.
     *
     * <p>
     * This is only a local safety limit. Production horizontal
     * scaling will require distributed session ownership.
     * </p>
     */
    public static final int MAX_SESSION_AUDIO_BYTES =
            10 * 1024 * 1024;

    /**
     * Default stream timeout in seconds.
     */
    public static final int DEFAULT_STREAM_TIMEOUT_SECONDS =
            30;

    // =========================================================
    // CONTEXT KEYS
    // =========================================================

    /**
     * Call identifier context key.
     */
    public static final String CONTEXT_CALL_ID =
            "callId";

    /**
     * Stream SID context key.
     */
    public static final String CONTEXT_STREAM_SID =
            "streamSid";

    /**
     * Provider call ID context key.
     */
    public static final String CONTEXT_PROVIDER_CALL_ID =
            "providerCallId";

    /**
     * Tenant identifier context key.
     */
    public static final String CONTEXT_TENANT_ID =
            "tenantId";

    /**
     * Agent identifier context key.
     */
    public static final String CONTEXT_AGENT_ID =
            "agentId";

    /**
     * Flow identifier context key.
     */
    public static final String CONTEXT_FLOW_PUBLIC_ID =
            "flowPublicId";

    // =========================================================
    // AUDIO
    // =========================================================

    /**
     * Default audio content type used internally for
     * Exotel linear PCM audio.
     */
    public static final String AUDIO_CONTENT_TYPE =
            "audio/x-l16;rate=8000;channels=1";

    /**
     * Internal audio format identifier.
     */
    public static final String AUDIO_FORMAT =
            "PCM_S16LE";

    // =========================================================
    // RUNTIME ACTIONS
    // =========================================================

    /**
     * Runtime action for speaking audio.
     */
    public static final String ACTION_SPEAK =
            "SPEAK";

    /**
     * Runtime action for listening to caller.
     */
    public static final String ACTION_LISTEN =
            "LISTEN";

    /**
     * Runtime action for ending a call.
     */
    public static final String ACTION_END =
            "END";

    /**
     * Runtime action for transferring a call.
     */
    public static final String ACTION_TRANSFER =
            "TRANSFER";

    // =========================================================
    // SPEAKER TYPES
    // =========================================================

    /**
     * Caller speaker type.
     */
    public static final String SPEAKER_USER =
            "USER";

    /**
     * AI speaker type.
     */
    public static final String SPEAKER_AI =
            "AI";

    // =========================================================
    // LOGGING
    // =========================================================

    /**
     * Logging prefix for Voice Gateway events.
     */
    public static final String LOG_PREFIX =
            "VOICE_GATEWAY";
}
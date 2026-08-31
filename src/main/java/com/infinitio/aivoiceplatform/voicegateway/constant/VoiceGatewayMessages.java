package com.infinitio.aivoiceplatform.voicegateway.constant;

/**
 * Message constants used by the Voice Gateway module.
 *
 * <p>
 * Keeping messages in a dedicated constants class prevents
 * WebSocket handlers and services from containing hard-coded
 * user-facing or operational messages.
 * </p>
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
public final class VoiceGatewayMessages {

    /**
     * Private constructor.
     */
    private VoiceGatewayMessages() {

        throw new IllegalStateException(
                "Utility class must not be instantiated."
        );
    }

    // =========================================================
    // CONNECTION
    // =========================================================

    /**
     * WebSocket connection established.
     */
    public static final String CONNECTION_ESTABLISHED =
            "Voice gateway connection established successfully.";

    /**
     * WebSocket connection closed.
     */
    public static final String CONNECTION_CLOSED =
            "Voice gateway connection closed.";

    /**
     * Unable to establish gateway session.
     */
    public static final String CONNECTION_FAILED =
            "Unable to establish voice gateway session.";

    // =========================================================
    // EVENTS
    // =========================================================

    /**
     * Unsupported WebSocket event.
     */
    public static final String UNSUPPORTED_EVENT =
            "Unsupported voice gateway event.";

    /**
     * Invalid WebSocket event.
     */
    public static final String INVALID_EVENT =
            "Invalid voice gateway event.";

    /**
     * Missing event type.
     */
    public static final String EVENT_REQUIRED =
            "Voice gateway event type is required.";

    /**
     * Invalid start event.
     */
    public static final String INVALID_START_EVENT =
            "Invalid voice gateway start event.";

    /**
     * Invalid media event.
     */
    public static final String INVALID_MEDIA_EVENT =
            "Invalid voice gateway media event.";

    /**
     * Invalid DTMF event.
     */
    public static final String INVALID_DTMF_EVENT =
            "Invalid voice gateway DTMF event.";

    /**
     * Invalid stop event.
     */
    public static final String INVALID_STOP_EVENT =
            "Invalid voice gateway stop event.";

    // =========================================================
    // CALL
    // =========================================================

    /**
     * Call identifier is required.
     */
    public static final String CALL_ID_REQUIRED =
            "Call ID is required for voice gateway processing.";

    /**
     * Call session was not found.
     */
    public static final String CALL_SESSION_NOT_FOUND =
            "Call session was not found.";

    /**
     * Call session is not active.
     */
    public static final String CALL_SESSION_NOT_ACTIVE =
            "Call session is not active.";

    /**
     * Call is already ended.
     */
    public static final String CALL_ALREADY_ENDED =
            "Call has already ended.";

    // =========================================================
    // STREAM
    // =========================================================

    /**
     * Stream identifier is required.
     */
    public static final String STREAM_ID_REQUIRED =
            "Stream ID is required.";

    /**
     * Stream session was not found.
     */
    public static final String STREAM_SESSION_NOT_FOUND =
            "Voice stream session was not found.";

    /**
     * Stream has already been closed.
     */
    public static final String STREAM_ALREADY_CLOSED =
            "Voice stream session has already been closed.";

    /**
     * Stream initialization failed.
     */
    public static final String STREAM_INITIALIZATION_FAILED =
            "Unable to initialize voice stream.";

    /**
     * Stream processing failed.
     */
    public static final String STREAM_PROCESSING_FAILED =
            "Unable to process voice stream.";

    // =========================================================
    // AUDIO
    // =========================================================

    /**
     * Audio payload is required.
     */
    public static final String AUDIO_PAYLOAD_REQUIRED =
            "Audio payload is required.";

    /**
     * Audio payload is invalid.
     */
    public static final String INVALID_AUDIO_PAYLOAD =
            "Invalid audio payload.";

    /**
     * Audio payload decoding failed.
     */
    public static final String AUDIO_DECODE_FAILED =
            "Unable to decode audio payload.";

    /**
     * Audio payload exceeds the configured limit.
     */
    public static final String AUDIO_PAYLOAD_TOO_LARGE =
            "Audio payload exceeds the configured size limit.";

    /**
     * Unsupported audio format.
     */
    public static final String UNSUPPORTED_AUDIO_FORMAT =
            "Unsupported audio format.";

    // =========================================================
    // STT
    // =========================================================

    /**
     * STT processing failed.
     */
    public static final String STT_PROCESSING_FAILED =
            "Unable to process caller audio through speech-to-text.";

    /**
     * STT response was empty.
     */
    public static final String STT_RESPONSE_EMPTY =
            "Speech-to-text returned an empty response.";

    // =========================================================
    // ORCHESTRATOR
    // =========================================================

    /**
     * Conversation orchestrator processing failed.
     */
    public static final String ORCHESTRATOR_PROCESSING_FAILED =
            "Unable to process conversation input.";

    /**
     * Conversation response was empty.
     */
    public static final String ORCHESTRATOR_RESPONSE_EMPTY =
            "Conversation orchestrator returned an empty response.";

    // =========================================================
    // DTMF
    // =========================================================

    /**
     * DTMF digit is required.
     */
    public static final String DTMF_DIGIT_REQUIRED =
            "DTMF digit is required.";

    /**
     * Invalid DTMF digit.
     */
    public static final String INVALID_DTMF =
            "Invalid DTMF digit.";

    // =========================================================
    // BARGE-IN
    // =========================================================

    /**
     * Barge-in received while AI audio is being played.
     */
    public static final String BARGE_IN_RECEIVED =
            "Caller barge-in received.";

    /**
     * Unable to stop currently playing audio.
     */
    public static final String BARGE_IN_CLEAR_FAILED =
            "Unable to clear currently playing audio.";

    // =========================================================
    // RECORDING
    // =========================================================

    /**
     * Recording session started.
     */
    public static final String RECORDING_STARTED =
            "Call recording started.";

    /**
     * Recording session stopped.
     */
    public static final String RECORDING_STOPPED =
            "Call recording stopped.";

    /**
     * Recording storage failed.
     */
    public static final String RECORDING_STORAGE_FAILED =
            "Unable to store call recording.";

    // =========================================================
    // TELEPHONY
    // =========================================================

    /**
     * Telephony provider is unavailable.
     */
    public static final String TELEPHONY_PROVIDER_UNAVAILABLE =
            "Telephony provider is unavailable.";

    /**
     * Invalid provider stream.
     */
    public static final String INVALID_PROVIDER_STREAM =
            "Invalid telephony provider stream.";

    /**
     * Provider authentication failed.
     */
    public static final String PROVIDER_AUTHENTICATION_FAILED =
            "Telephony provider authentication failed.";

    // =========================================================
    // RUNTIME
    // =========================================================

    /**
     * Runtime processing failed.
     */
    public static final String RUNTIME_PROCESSING_FAILED =
            "Voice runtime processing failed.";

    /**
     * Runtime state is unavailable.
     */
    public static final String RUNTIME_STATE_UNAVAILABLE =
            "Voice runtime state is unavailable.";

    /**
     * Unexpected runtime error.
     */
    public static final String UNEXPECTED_RUNTIME_ERROR =
            "Unexpected error occurred during voice runtime processing.";
}
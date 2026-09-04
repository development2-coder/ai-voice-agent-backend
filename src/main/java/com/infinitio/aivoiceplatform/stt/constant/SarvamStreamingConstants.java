package com.infinitio.aivoiceplatform.stt.constant;

/**
 * Constants used by the Sarvam realtime STT streaming provider.
 *
 * <p>
 * These constants represent Sarvam WebSocket protocol values.
 * Runtime configuration such as endpoint, model, encoding,
 * timeout and API credentials must not be placed here.
 * </p>
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
public final class SarvamStreamingConstants {

    /**
     * Private constructor.
     */
    private SarvamStreamingConstants() {

        throw new IllegalStateException(
                "Utility class must not be instantiated."
        );
    }

    // =========================================================
    // PROVIDER EVENTS
    // =========================================================

    /**
     * Audio input event sent to Sarvam.
     */
    public static final String EVENT_AUDIO_INPUT =
            "audio_input";

    /**
     * Sarvam session start event.
     */
    public static final String EVENT_SESSION_BEGIN =
            "session.begin";

    /**
     * Partial transcript event.
     */
    public static final String EVENT_TRANSCRIPT_PARTIAL =
            "transcript.partial";

    /**
     * Final transcript event.
     */
    public static final String EVENT_TRANSCRIPT_FINAL =
            "transcript.final";

    /**
     * Speech start event.
     */
    public static final String EVENT_VAD_SPEECH_START =
            "vad.speech_start";

    /**
     * Speech end event.
     */
    public static final String EVENT_VAD_SPEECH_END =
            "vad.speech_end";

    /**
     * Session end event.
     */
    public static final String EVENT_SESSION_END =
            "session.end";

    /**
     * Provider error event.
     */
    public static final String EVENT_ERROR =
            "error";

    // =========================================================
    // JSON FIELDS
    // =========================================================

    /**
     * Event field.
     */
    public static final String FIELD_EVENT =
            "event";

    /**
     * Audio field.
     */
    public static final String FIELD_AUDIO =
            "audio";

    /**
     * Transcript text field.
     */
    public static final String FIELD_TEXT =
            "text";

    /**
     * Transcript field.
     */
    public static final String FIELD_TRANSCRIPT =
            "transcript";

    /**
     * Language code field.
     */
    public static final String FIELD_LANGUAGE_CODE =
            "language_code";

    // =========================================================
    // SESSION CONTROL
    // =========================================================

    /**
     * Provider streaming end event.
     */
    public static final String EVENT_END =
            "end";

    /**
     * Provider name.
     */
    public static final String PROVIDER_NAME =
            "sarvam";
}